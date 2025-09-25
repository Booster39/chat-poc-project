import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { ChatMessage } from '../models/chat-message.model';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient!: Client; // Utilisation de ! pour indiquer qu'elle sera initialisée
  private messageSubject: BehaviorSubject<ChatMessage[]> = new BehaviorSubject<ChatMessage[]>([]);
  private typingSubject: BehaviorSubject<string> = new BehaviorSubject<string>('');
  private connected: boolean = false;

  constructor() {
    this.initializeWebSocketConnection();
  }

  private initializeWebSocketConnection(): void {
    const socket = new SockJS('http://localhost:8080/ws-chat');
    
    this.stompClient = new Client({
      webSocketFactory: () => socket as any,
      debug: (str) => console.log('STOMP: ' + str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Connecté: ' + frame);
      this.connected = true;
      this.subscribeToMessages();
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Erreur STOMP: ' + frame.headers['message']);
      console.error('Détails: ' + frame.body);
    };

    this.stompClient.onDisconnect = (frame) => {
      console.log('Déconnecté: ' + frame);
      this.connected = false;
    };
  }

  public connect(): void {
    if (this.stompClient && !this.connected) {
      this.stompClient.activate();
    }
  }

  public disconnect(): void {
    if (this.stompClient && this.connected) {
      this.stompClient.deactivate();
      this.connected = false;
    }
  }

  private subscribeToMessages(): void {
    if (!this.stompClient) return;

    // Abonnement aux messages publics
    this.stompClient.subscribe('/topic/public', (message) => {
      try {
        const chatMessage: ChatMessage = JSON.parse(message.body);
        const currentMessages = this.messageSubject.value;
        this.messageSubject.next([...currentMessages, chatMessage]);
      } catch (error) {
        console.error('Erreur lors du parsing du message:', error);
      }
    });

    // Abonnement aux notifications de frappe
    this.stompClient.subscribe('/topic/typing', (message) => {
      try {
        const chatMessage: ChatMessage = JSON.parse(message.body);
        this.typingSubject.next(`${chatMessage.sender} est en train d'écrire...`);
        
        // Effacer la notification après 3 secondes
        setTimeout(() => {
          this.typingSubject.next('');
        }, 3000);
      } catch (error) {
        console.error('Erreur lors du parsing de la notification de frappe:', error);
      }
    });
  }

  public sendMessage(message: ChatMessage): void {
    if (this.stompClient && this.connected) {
      try {
        this.stompClient.publish({
          destination: '/app/chat.sendMessage',
          body: JSON.stringify(message)
        });
      } catch (error) {
        console.error('Erreur lors de l\'envoi du message:', error);
      }
    }
  }

  public addUser(username: string): void {
    if (this.stompClient && this.connected) {
      const message: ChatMessage = {
        sender: username,
        type: 'JOIN',
        content: '',
        sessionId: '',
        timestamp: new Date()
      };
      
      try {
        this.stompClient.publish({
          destination: '/app/chat.addUser',
          body: JSON.stringify(message)
        });
      } catch (error) {
        console.error('Erreur lors de l\'ajout de l\'utilisateur:', error);
      }
    }
  }

  public sendTypingNotification(username: string): void {
    if (this.stompClient && this.connected) {
      const message: ChatMessage = {
        sender: username,
        type: 'TYPING',
        content: '',
        sessionId: '',
        timestamp: new Date()
      };
      
      try {
        this.stompClient.publish({
          destination: '/app/chat.typing',
          body: JSON.stringify(message)
        });
      } catch (error) {
        console.error('Erreur lors de l\'envoi de la notification de frappe:', error);
      }
    }
  }

  public getMessages(): Observable<ChatMessage[]> {
    return this.messageSubject.asObservable();
  }

  public getTypingStatus(): Observable<string> {
    return this.typingSubject.asObservable();
  }

  public isConnected(): boolean {
    return this.connected && this.stompClient && this.stompClient.connected;
  }

  // Méthode pour nettoyer les ressources
  public cleanup(): void {
    if (this.stompClient) {
      this.disconnect();
    }
    this.messageSubject.complete();
    this.typingSubject.complete();
  }
}