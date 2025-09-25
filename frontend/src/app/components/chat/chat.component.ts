import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { WebSocketService } from '../../services/websocket.service';
import { ChatMessage } from '../../models/chat-message.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, OnDestroy {
  @ViewChild('messageInput') messageInput!: ElementRef;
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  messages: ChatMessage[] = [];
  currentMessage: string = '';
  username: string = '';
  isConnected: boolean = false;
  typingStatus: string = '';
  
  private messageSubscription!: Subscription;
  private typingSubscription!: Subscription;

  constructor(private webSocketService: WebSocketService) {}

  ngOnInit(): void {
    this.promptForUsername();
    this.subscribeToMessages();
    this.subscribeToTyping();
  }

  ngOnDestroy(): void {
    if (this.messageSubscription) this.messageSubscription.unsubscribe();
    if (this.typingSubscription) this.typingSubscription.unsubscribe();
    this.webSocketService.disconnect();
  }

  private promptForUsername(): void {
    this.username = prompt('Entrez votre nom d\'utilisateur:') || 'Utilisateur' + Math.floor(Math.random() * 1000);
    this.connectToChat();
  }

  private connectToChat(): void {
    this.webSocketService.connect();
    setTimeout(() => {
      if (this.webSocketService.isConnected()) {
        this.webSocketService.addUser(this.username);
        this.isConnected = true;
      }
    }, 1000);
  }

  private subscribeToMessages(): void {
    this.messageSubscription = this.webSocketService.getMessages().subscribe(
      (messages: ChatMessage[]) => {
        this.messages = messages;
        this.scrollToBottom();
      }
    );
  }

  private subscribeToTyping(): void {
    this.typingSubscription = this.webSocketService.getTypingStatus().subscribe(
      (status: string) => {
        this.typingStatus = status;
      }
    );
  }

  onSendMessage(): void {
    if (this.currentMessage.trim() && this.isConnected) {
      const message: ChatMessage = {
        content: this.currentMessage,
        sender: this.username,
        type: 'CHAT',
        sessionId: '',
        timestamp: new Date()
      };

      this.webSocketService.sendMessage(message);
      this.currentMessage = '';
    }
  }

  onMessageInputChange(): void {
    if (this.isConnected) {
      this.webSocketService.sendTypingNotification(this.username);
    }
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.onSendMessage();
    }
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      try {
        this.messagesContainer.nativeElement.scrollTop = 
          this.messagesContainer.nativeElement.scrollHeight;
      } catch(err) {
        console.error('Erreur lors du scroll:', err);
      }
    }, 100);
  }

  getMessageClass(message: ChatMessage): string {
    switch (message.type) {
      case 'JOIN': return 'join-message';
      case 'LEAVE': return 'leave-message';
      case 'CHAT': return message.sender === this.username ? 'own-message' : 'other-message';
      default: return '';
    }
  }

  formatTime(timestamp: Date): string {
    return new Date(timestamp).toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
