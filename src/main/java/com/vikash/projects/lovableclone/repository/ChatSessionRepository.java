package com.vikash.projects.lovableclone.repository;

import com.vikash.projects.lovableclone.entity.ChatSession;
import com.vikash.projects.lovableclone.entity.ChatSessionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, ChatSessionId> {
}
