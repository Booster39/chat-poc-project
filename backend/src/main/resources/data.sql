-- Insertion des utilisateurs de test
-- Mot de passe pour tous: "password123" (hashé avec BCrypt)

INSERT INTO users (username, email, password, first_name, last_name, role, status, is_online) VALUES
-- Administrateur
('admin', 'admin@chatpoc.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFSPnLQfs/xtOb/QgOhj1jv', 'Admin', 'System', 'ADMIN', 'ACTIVE', true),

-- Agents
('marie.agent', 'marie@chatpoc.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFSPnLQfs/xtOb/QgOhj1jv', 'Marie', 'Agent', 'AGENT', 'ACTIVE', true),
('pierre.agent', 'pierre@chatpoc.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFSPnLQfs/xtOb/QgOhj1jv', 'Pierre', 'Martin', 'AGENT', 'ACTIVE', false),
('sophie.agent', 'sophie@chatpoc.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFSPnLQfs/xtOb/QgOhj1jv', 'Sophie', 'Durant', 'AGENT', 'ACTIVE', true),

-- Clients
('client1', 'client1@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFSPnLQfs/xtOb/QgOhj1jv', 'Jean', 'Dupont', 'CLIENT', 'ACTIVE', false),
('client2', 'client2@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFSPnLQfs/xtOb/QgOhj1jv', 'Anne', 'Martin', 'CLIENT', 'ACTIVE', true),
('client3', 'client3@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFSPnLQfs/xtOb/QgOhj1jv', 'Paul', 'Bernard', 'CLIENT', 'ACTIVE', false);

-- Insertion des conversations de test
INSERT INTO conversations (title, client_id, agent_id, status, started_at) VALUES
-- Conversation active
('Problème de connexion', 5, 2, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '2' HOUR),

-- Conversation en attente
('Question sur facturation', 6, NULL, 'WAITING', CURRENT_TIMESTAMP - INTERVAL '30' MINUTE),

-- Conversation fermée
('Support technique terminé', 7, 3, 'CLOSED', CURRENT_TIMESTAMP - INTERVAL '1' DAY);

-- Insertion des messages de test
INSERT INTO messages (conversation_id, sender_id, content, message_type, created_at) VALUES
-- Messages pour la conversation 1 (Active)
(1, 5, 'Bonjour, j''ai un problème de connexion à mon compte', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
(1, 2, 'Bonjour Jean, je vais vous aider. Pouvez-vous me décrire le problème ?', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '2' HOUR + INTERVAL '5' MINUTE),
(1, 5, 'Je n''arrive pas à me connecter depuis ce matin', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '2' HOUR + INTERVAL '7' MINUTE),
(1, 2, 'Avez-vous essayé de réinitialiser votre mot de passe ?', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '2' HOUR + INTERVAL '10' MINUTE),
(1, 5, 'Non, comment faire ?', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '2' HOUR + INTERVAL '12' MINUTE),

-- Messages pour la conversation 2 (En attente)
(2, 6, 'Bonjour, j''ai une question sur ma facture du mois dernier', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '30' MINUTE),
(2, 6, 'Il y a des frais que je ne comprends pas', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '28' MINUTE),

-- Messages pour la conversation 3 (Fermée)
(3, 7, 'Bonjour, mon ordinateur ne démarre plus', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '1' DAY),
(3, 3, 'Bonjour Paul, nous allons diagnostiquer le problème', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '2' MINUTE),
(3, 3, 'Pouvez-vous vérifier que tous les câbles sont bien branchés ?', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '5' MINUTE),
(3, 7, 'Oui, tout est branché correctement', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '10' MINUTE),
(3, 3, 'Essayez de maintenir le bouton power enfoncé 10 secondes puis rallumez', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '15' MINUTE),
(3, 7, 'Ça marche ! Merci beaucoup !', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '20' MINUTE),
(3, 3, 'Parfait ! N''hésitez pas à nous recontacter si besoin', 'CHAT', CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '22' MINUTE);