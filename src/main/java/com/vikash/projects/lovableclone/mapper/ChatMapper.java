package com.vikash.projects.lovableclone.mapper;

import com.vikash.projects.lovableclone.dto.chat.ChatResponse;
import com.vikash.projects.lovableclone.entity.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    List<ChatResponse> fromListOfChatMessage(List<ChatMessage> chatMessageList);
}
