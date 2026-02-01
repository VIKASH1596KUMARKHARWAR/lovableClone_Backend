package com.vikash.projects.lovableclone.service;


import com.vikash.projects.lovableclone.dto.chat.ChatResponse;

import java.util.List;

public interface ChatService {

    List<ChatResponse> getProjectChatHistory(Long projectId);
}
