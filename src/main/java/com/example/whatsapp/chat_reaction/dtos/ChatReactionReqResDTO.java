package com.example.whatsapp.chat_reaction.dtos;

import lombok.*;
import org.hibernate.annotations.SecondaryRow;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SecondaryRow
@Builder
public class ChatReactionReqResDTO{
    private UUID chatMessageId;
    private String username;
    private String emoji;
    private String status;
}
