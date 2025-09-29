package com.example.chatpoc.repository;

import com.example.chatpoc.model.Conversation;
import com.example.chatpoc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByClientOrderByStartedAtDesc(User client);

    List<Conversation> findByAgentOrderByStartedAtDesc(User agent);

    List<Conversation> findByStatus(Conversation.ConversationStatus status);

    @Query("SELECT c FROM Conversation c WHERE c.status = 'WAITING' ORDER BY c.startedAt ASC")
    List<Conversation> findWaitingConversations();

    @Query("SELECT c FROM Conversation c WHERE c.agent = :agent AND c.status = 'ACTIVE'")
    List<Conversation> findActiveConversationsByAgent(@Param("agent") User agent);

    @Query("SELECT c FROM Conversation c WHERE c.client = :client AND c.status IN ('WAITING', 'ACTIVE')")
    Optional<Conversation> findActiveConversationByClient(@Param("client") User client);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.agent = :agent AND c.status = 'ACTIVE'")
    long countActiveConversationsByAgent(@Param("agent") User agent);
}
