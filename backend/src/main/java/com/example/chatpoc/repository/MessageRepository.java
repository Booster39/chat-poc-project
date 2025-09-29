package com.example.chatpoc.repository;

import com.example.chatpoc.model.Conversation;
import com.example.chatpoc.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);

    Page<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation AND m.createdAt > :since ORDER BY m.createdAt ASC")
    List<Message> findRecentMessagesByConversation(@Param("conversation") Conversation conversation,
                                                   @Param("since") LocalDateTime since);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastMessageByConversationId(@Param("conversationId") Long conversationId);

    long countByConversation(Conversation conversation);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = :conversation AND m.createdAt > :since")
    long countRecentMessagesByConversation(@Param("conversation") Conversation conversation,
                                           @Param("since") LocalDateTime since);
}