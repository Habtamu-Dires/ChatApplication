package com.example.whatsapp.chat_response;

import com.example.whatsapp.chat.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatResponseService {

    private final ChatResponseRepository chatResponseRepository;

    public List<ChatResponse> getChatResponses(Long chatMessageId) {
        return chatResponseRepository.findByChatMessage(chatMessageId);
    }
}
