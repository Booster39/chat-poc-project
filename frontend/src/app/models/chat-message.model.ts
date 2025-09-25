export interface ChatMessage {
  id?: string;
  content: string;
  sender: string;
  type: 'CHAT' | 'JOIN' | 'LEAVE' | 'TYPING';
  sessionId: string;
  timestamp: Date;
}
