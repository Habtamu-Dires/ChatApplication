package com.example.app.chat_reaction;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class ChatReactionId implements Serializable {

    private UUID chatMessage;
    private Long user;

    @Override
    public boolean equals(Object obj) {
       if(this == obj) return true;
       if(obj == null || getClass() != obj.getClass()) return false;
       ChatReactionId that =(ChatReactionId) obj;
       return Objects.equals(user, that.user) &&
              Objects.equals(chatMessage, that.chatMessage);
    }

    @Override
    public int hashCode() {
       return Objects.hash(user, chatMessage);
    }
}
