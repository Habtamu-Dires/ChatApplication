package com.example.whatsapp.attachment_controller;

import lombok.Builder;

@Builder
public record FileDTO(String fileName, String fileUrl) {
}
