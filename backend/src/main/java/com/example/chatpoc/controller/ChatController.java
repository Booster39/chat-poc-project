package com.example.chatpoc.controller;

import com.example.chatpoc.model.ChatMessage;
import com.example.chatpoc.model.Message;
import com.example.chatpoc.model.User;
import com.example.chatpoc.model.Conversation;
import com.example.chatpoc.repository.UserRepository;
import com.example.chatpoc.repository.ConversationRepository;
import com.example.chatpoc.repository.MessageRepository;
import com.example.chatpoc.service.ChatService;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        try {
            // Sauvegarder le message en base de données
            Message savedMessage = chatService.saveMessage(chatMessage);

            // Convertir en ChatMessage pour WebSocket
            ChatMessage response = convertToWebSocketMessage(savedMessage);

            System.out.println("Message sauvegardé et envoyé: " + response.getContent() +
                    " de: " + response.getSender());

            return response;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du message: " + e.getMessage());
            // Renvoyer le message original en cas d'erreur
            chatMessage.setId(UUID.randomUUID().toString());
            return chatMessage;
        }
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Ajouter le nom d'utilisateur à la session WebSocket
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

            // Mettre à jour le statut en ligne de l'utilisateur
            Optional<User> userOpt = userRepository.findByUsername(chatMessage.getSender());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setIsOnline(true);
                userRepository.save(user);
            }

            // Créer le message de système
            chatMessage.setId(UUID.randomUUID().toString());
            chatMessage.setType(ChatMessage.MessageType.JOIN);
            chatMessage.setContent(chatMessage.getSender() + " a rejoint le chat");

            System.out.println("Utilisateur connecté: " + chatMessage.getSender());

            return chatMessage;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
            chatMessage.setId(UUID.randomUUID().toString());
            return chatMessage;
        }
    }

    @MessageMapping("/chat.typing")
    public void userTyping(@Payload ChatMessage chatMessage) {
        try {
            // Notifier que l'utilisateur est en train de taper
            chatMessage.setType(ChatMessage.MessageType.TYPING);
            messagingTemplate.convertAndSend("/topic/typing", chatMessage);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification de frappe: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.loadHistory")
    public void loadChatHistory(@Payload ChatMessage request,
                                SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            if (username != null) {
                // Charger l'historique des messages pour cet utilisateur
                chatService.loadUserChatHistory(username, messagingTemplate);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'historique: " + e.getMessage());
        }
    }

    private ChatMessage convertToWebSocketMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(message.getId().toString());
        chatMessage.setContent(message.getContent());
        chatMessage.setSender(message.getSender().getUsername());
        chatMessage.setType(convertMessageType(message.getMessageType()));
        chatMessage.setTimestamp(message.getCreatedAt());

        if (message.getConversation() != null) {
            chatMessage.setSessionId(message.getConversation().getId().toString());
        }

        return chatMessage;
    }

    private ChatMessage.MessageType convertMessageType(Message.MessageType dbType) {
        switch (dbType) {
            case CHAT: return ChatMessage.MessageType.CHAT;
            case JOIN: return ChatMessage.MessageType.JOIN;
            case LEAVE: return ChatMessage.MessageType.LEAVE;
            case SYSTEM: return ChatMessage.MessageType.TYPING;
            default: return ChatMessage.MessageType.CHAT;
        }
    }
}