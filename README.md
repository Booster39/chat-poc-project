# Chat Service Client - Preuve de Concept (PoC)

## Description
Cette preuve de concept démontre la faisabilité d'un système de chat en temps réel pour le service client, utilisant WebSocket avec Spring Boot et Angular.

## Installation et démarrage

### Prérequis
- Java 17+
- Node.js 18+
- Maven 3.6+
- Angular CLI

### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
Le serveur démarre sur http://localhost:8080

### Frontend
```bash
cd frontend
npm install
ng serve
```
L'application démarre sur http://localhost:4200

## Fonctionnalités
- Communication bidirectionnelle en temps réel
- Notifications système (connexion/déconnexion)
- Indicateur de frappe en direct
- Interface responsive
- Gestion des erreurs de connexion
- Auto-scroll vers les nouveaux messages

## Architecture
- **Backend**: Spring Boot 3.2.0 + WebSocket + STOMP
- **Frontend**: Angular 17 + SockJS + TypeScript
- **Communication**: WebSocket avec fallback SockJS

## Tests
1. Ouvrir http://localhost:4200
2. Saisir un nom d'utilisateur
3. Ouvrir plusieurs onglets pour tester multi-utilisateurs
4. Échanger des messages en temps réel

## Statut
 **Preuve de concept validée** - Prêt pour développement complet
