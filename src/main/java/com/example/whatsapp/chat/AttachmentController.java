package com.example.whatsapp.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping(value = "/upload-picture",  consumes = {"multipart/form-data"})
    public ResponseEntity<String> uploadPicture(@RequestPart("picture") MultipartFile picture) {
        if (picture.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a picture");
        }

        if (picture.getSize() > attachmentSizeLimit) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File size exceeds the limit");
        }

        try {
            Path filePath = Paths.get(pictureDirectory, Objects.requireNonNull(picture.getOriginalFilename()));
            Files.copy(picture.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = "/picture/" + picture.getOriginalFilename(); // Construct URL for the uploaded picture
            return ResponseEntity.ok("Picture uploaded successfully. File URL: " + fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload picture");
        }
    }

    @PostMapping(value = "/upload-video", consumes = {"multipart/form-data"})
    public ResponseEntity<String> uploadVideo(@RequestPart("video") MultipartFile video) {
        if (video.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a video");
        }

        if (video.getSize() > attachmentSizeLimit) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("Video size exceeds the limit");
        }
        System.out.println("File size " + video.getSize());
        try {
            Path filePath = Paths.get(videoDirectory, Objects.requireNonNull(video.getOriginalFilename()));
            Files.copy(video.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = "/video/" + video.getOriginalFilename(); // Construct URL for the uploaded video
            return ResponseEntity.ok("Video uploaded successfully. File URL: " + fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload video");
        }
    }
}
