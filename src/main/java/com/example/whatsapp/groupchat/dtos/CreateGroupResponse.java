package com.example.whatsapp.groupchat.dtos;

import lombok.Builder;

@Builder
public record CreateGroupResponse(Long groupId, String groupName, String ownerName) {
}


