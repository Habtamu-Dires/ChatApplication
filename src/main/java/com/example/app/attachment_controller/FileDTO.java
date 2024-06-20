package com.example.app.attachment_controller;

import lombok.Builder;

@Builder
public record FileDTO(String fileName, String fileUrl) {
}
