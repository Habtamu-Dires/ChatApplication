package com.example.app.groupchat.dtos;

public record UpdateGroupNameDTO(String ownerName,
                                 String oldGroupName,
                                 String newGroupName) { }
