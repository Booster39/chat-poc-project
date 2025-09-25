# Spécifications Techniques - Chat PoC

## Architecture WebSocket

### Backend (Spring Boot)
- **Protocole**: STOMP over WebSocket
- **Fallback**: SockJS pour compatibilité navigateurs
- **Endpoint**: `/ws-chat`
- **Broker**: Simple in-memory broker

### Destinations STOMP
- `/app/chat.sendMessage` - Envoi de messages
- `/app/chat.addUser` - Ajout d'utilisateur  
- `/app/chat.typing` - Notification de frappe
- `/topic/public` - Diffusion publique
- `/topic/typing` - Notifications de frappe

### Frontend (Angular)
- **Client**: SockJS + STOMP.js
- **Reconnexion**: Automatique (5s)
- **Heartbeat**: 4000ms in/out
- **State Management**: RxJS BehaviorSubject

## Modèle de données

### ChatMessage
```typescript
{
  id?: string;
  content: string;
  sender: string;
  type: 'CHAT' | 'JOIN' | 'LEAVE' | 'TYPING';
  sessionId: string;
  timestamp: Date;
}
```

## Flux de communication

### Connexion utilisateur
1. Client se connecte à `/ws-chat`
2. Envoi message JOIN via `/app/chat.addUser`
3. Diffusion notification via `/topic/public`
4. Stockage username en session WebSocket

### Envoi de message
1. Client envoie via `/app/chat.sendMessage`
2. Serveur ajoute ID et timestamp
3. Diffusion à tous via `/topic/public`

### Notification de frappe
1. Client envoie via `/app/chat.typing`
2. Diffusion via `/topic/typing`
3. Auto-effacement après 3s

### Déconnexion
1. Détection déconnexion WebSocket
2. Récupération username depuis session
3. Diffusion notification LEAVE

## Sécurité

### CORS
- Origin autorisé: `http://localhost:4200`
- Headers: Tous autorisés
- Credentials: Activés

### Session Management
- Stockage username en session WebSocket
- Nettoyage automatique à la déconnexion
- Pas de persistance serveur

## Performance

### Optimisations
- Heartbeat configuré pour détection rapide
- Messages légers (JSON minimal)
- Reconnexion automatique
- Scroll optimisé côté client

### Limitations PoC
- Un seul canal de chat
- Pas de persistance
- Authentification simplifiée
- Messages en mémoire uniquement

## Extensions futures
- Authentification JWT
- Persistance base de données
- Canaux multiples
- Chiffrement messages
- Upload fichiers
- Notifications push
