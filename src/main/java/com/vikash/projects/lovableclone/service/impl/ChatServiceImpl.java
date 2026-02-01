package com.vikash.projects.lovableclone.service.impl;

import com.vikash.projects.lovableclone.dto.chat.ChatResponse;
import com.vikash.projects.lovableclone.entity.ChatMessage;
import com.vikash.projects.lovableclone.entity.ChatSession;
import com.vikash.projects.lovableclone.entity.ChatSessionId;
import com.vikash.projects.lovableclone.mapper.ChatMapper;
import com.vikash.projects.lovableclone.repository.ChatMessageRepository;
import com.vikash.projects.lovableclone.repository.ChatSessionRepository;
import com.vikash.projects.lovableclone.security.AuthUtil;
import com.vikash.projects.lovableclone.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final AuthUtil authUtil;
    private final ChatMapper chatMapper;

    @Override
    public List<ChatResponse> getProjectChatHistory(Long projectId) {
        Long userId = authUtil.getCurrentUserId();

        ChatSession chatSession = chatSessionRepository.getReferenceById(
                new ChatSessionId(projectId, userId)
        );

        List<ChatMessage> chatMessageList = chatMessageRepository.findByChatSession(chatSession);

        return chatMapper.fromListOfChatMessage(chatMessageList);
    }
}
