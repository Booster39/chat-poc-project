package com.example.chatpoc.service;

import com.example.chatpoc.model.ChatMessage;
import com.example.chatpoc.model.Message;
import com.example.chatpoc.model.User;
import com.example.chatpoc.model.Conversation;
import com.example.chatpoc.repository.UserRepository;
import com.example.chatpoc.repository.ConversationRepository;
import com.example.chatpoc.repository.MessageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * Sauvegarde un message en base de données
     */
    public Message saveMessage(ChatMessage chatMessage) {
        // Récupérer l'utilisateur expéditeur
        Optional<User> senderOpt = userRepository.findByUsername(chatMessage.getSender());
        if (!senderOpt.isPresent()) {
            throw new RuntimeException("Utilisateur non trouvé: " + chatMessage.getSender());
        }
        User sender = senderOpt.get();

        // Récupérer ou créer une conversation
        Conversation conversation = getOrCreateConversation(sender, chatMessage.getSessionId());

        // Créer et sauvegarder le message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(chatMessage.getContent());
        message.setMessageType(convertToDbMessageType(chatMessage.getType()));
        message.setCreatedAt(LocalDateTime.now());

        return messageRepository.save(message);
    }

    /**
     * Récupère ou crée une conversation pour un utilisateur
     */
    private Conversation getOrCreateConversation(User user, String sessionId) {
        // Chercher une conversation active pour ce client
        Optional<Conversation> existingConv = conversationRepository.findActiveConversationByClient(user);

        if (existingConv.isPresent()) {
            return existingConv.get();
        }

        // Créer une nouvelle conversation
        Conversation conversation = new Conversation();
        conversation.setTitle("Chat avec " + user.getFirstName() + " " + user.getLastName());
        conversation.setClient(user);
        conversation.setStatus(Conversation.ConversationStatus.WAITING);

        // Assigner automatiquement un agent disponible si possible
        assignAvailableAgent(conversation);

        return conversationRepository.save(conversation);
    }

    /**
     * Assigne automatiquement un agent disponible à la conversation
     */
    private void assignAvailableAgent(Conversation conversation) {
        List<User> availableAgents = userRepository.findAvailableAgents();

        if (!availableAgents.isEmpty()) {
            // Trouver l'agent avec le moins de conversations actives
            User bestAgent = null;
            long minConversations = Long.MAX_VALUE;

            for (User agent : availableAgents) {
                long activeConversations = conversationRepository.countActiveConversationsByAgent(agent);
                if (activeConversations < minConversations) {
                    minConversations = activeConversations;
                    bestAgent = agent;
                }
            }

            if (bestAgent != null) {
                conversation.setAgent(bestAgent);
                conversation.setStatus(Conversation.ConversationStatus.ACTIVE);
            }
        }
    }

    /**
     * Charge l'historique des messages d'un utilisateur
     */
    public void loadUserChatHistory(String username, SimpMessagingTemplate messagingTemplate) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return;
        }

        User user = userOpt.get();

        // Récupérer les conversations récentes de l'utilisateur
        List<Conversation> conversations;
        if (user.getRole() == User.UserRole.CLIENT) {
            conversations = conversationRepository.findByClientOrderByStartedAtDesc(user);
        } else if (user.getRole() == User.UserRole.AGENT) {
            conversations = conversationRepository.findByAgentOrderByStartedAtDesc(user);
        } else {
            return;
        }

        // Envoyer les messages récents au client
        for (Conversation conv : conversations) {
            if (conv.getMessages() != null && !conv.getMessages().isEmpty()) {
                // Prendre les 10 derniers messages de chaque conversation
                List<Message> recentMessages = conv.getMessages().stream()
                        .skip(Math.max(0, conv.getMessages().size() - 10))
                        .toList();

                for (Message message : recentMessages) {
                    ChatMessage chatMessage = convertToWebSocketMessage(message);
                    messagingTemplate.convertAndSendToUser(username, "/queue/history", chatMessage);
                }
            }
        }
    }

    /**
     * Crée un nouvel utilisateur
     */
    public User createUser(String username, String email, String password,
                           String firstName, String lastName, User.UserRole role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Nom d'utilisateur déjà utilisé: " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email déjà utilisé: " + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password); // En production, hasher le mot de passe
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    /**
     * Met à jour le statut en ligne d'un utilisateur
     */
    public void updateUserOnlineStatus(String username, boolean isOnline) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsOnline(isOnline);
            userRepository.save(user);
        }
    }

    /**
     * Récupère les conversations en attente
     */
    public List<Conversation> getWaitingConversations() {
        return conversationRepository.findWaitingConversations();
    }

    /**
     * Assigne une conversation à un agent
     */
    public void assignConversationToAgent(Long conversationId, Long agentId) {
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        Optional<User> agentOpt = userRepository.findById(agentId);

        if (convOpt.isPresent() && agentOpt.isPresent()) {
            Conversation conversation = convOpt.get();
            User agent = agentOpt.get();

            if (agent.getRole() == User.UserRole.AGENT) {
                conversation.setAgent(agent);
                conversation.setStatus(Conversation.ConversationStatus.ACTIVE);
                conversationRepository.save(conversation);
            }
        }
    }

    /**
     * Ferme une conversation
     */
    public void closeConversation(Long conversationId) {
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isPresent()) {
            Conversation conversation = convOpt.get();
            conversation.setStatus(Conversation.ConversationStatus.CLOSED);
            conversation.setClosedAt(LocalDateTime.now());
            conversationRepository.save(conversation);
        }
    }

    private ChatMessage convertToWebSocketMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(message.getId().toString());
        chatMessage.setContent(message.getContent());
        chatMessage.setSender(message.getSender().getUsername());
        chatMessage.setType(convertToWebSocketMessageType(message.getMessageType()));
        chatMessage.setTimestamp(message.getCreatedAt());

        if (message.getConversation() != null) {
            chatMessage.setSessionId(message.getConversation().getId().toString());
        }

        return chatMessage;
    }

    private Message.MessageType convertToDbMessageType(ChatMessage.MessageType wsType) {
        switch (wsType) {
            case CHAT: return Message.MessageType.CHAT;
            case JOIN: return Message.MessageType.JOIN;
            case LEAVE: return Message.MessageType.LEAVE;
            case TYPING: return Message.MessageType.SYSTEM;
            default: return Message.MessageType.CHAT;
        }
    }

    private ChatMessage.MessageType convertToWebSocketMessageType(Message.MessageType dbType) {
        switch (dbType) {
            case CHAT: return ChatMessage.MessageType.CHAT;
            case JOIN: return ChatMessage.MessageType.JOIN;
            case LEAVE: return ChatMessage.MessageType.LEAVE;
            case SYSTEM: return ChatMessage.MessageType.TYPING;
            default: return ChatMessage.MessageType.CHAT;
        }
    }
}