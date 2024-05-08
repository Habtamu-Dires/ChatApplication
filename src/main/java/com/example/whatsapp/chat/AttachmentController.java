package com.example.whatsapp.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/attachments")
public class AttachmentController {

    @Value("${attachment.picture.directory}")
    private String pictureDirectory;

    @Value("${attachment.video.directory}")
    private String videoDirectory;

    @Value("${attachment.size-limit}")
    private long attachmentSizeLimit;

    @PostMapping("/uploadPicture")
    public ResponseEntity<String> uploadPicture(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file");
        }

        if (file.getSize() > attachmentSizeLimit) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File size exceeds the limit");
        }

        try {
            Path filePath = Paths.get(pictureDirectory, Objects.requireNonNull(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = "/picture/" + file.getOriginalFilename(); // Construct URL for the uploaded picture
            return ResponseEntity.ok("Picture uploaded successfully. File URL: " + fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload picture");
        }
    }

    @PostMapping("/uploadVideo")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file");
        }

        if (file.getSize() > attachmentSizeLimit) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File size exceeds the limit");
        }

        try {
            Path filePath = Paths.get(videoDirectory, Objects.requireNonNull(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = "/video/" + file.getOriginalFilename(); // Construct URL for the uploaded video
            return ResponseEntity.ok("Video uploaded successfully. File URL: " + fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload video");
        }
    }
}
