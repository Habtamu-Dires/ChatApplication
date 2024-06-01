package com.example.whatsapp.groupchat.dtos;

public record UpdateGroupNameDTO(String ownerName,
                                 String oldGroupName,
                                 String newGroupName) { }
