package com.vikash.projects.lovableclone.dto.chat;

import com.vikash.projects.lovableclone.entity.ChatEvent;
import com.vikash.projects.lovableclone.entity.ChatSession;
import com.vikash.projects.lovableclone.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        Long id,
        ChatSession chatSession,
        MessageRole role,
        List<ChatEvent> events,
        String content,
        Integer tokensUsed,
        Instant createdAt

) {
}
