package com.example.app.attachment_controller;

import com.example.app.api_response.ApiResponse;
import com.example.app.exception.InvalidRequestException;
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

    @PostMapping(value = "/upload-file", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<FileDTO>> uploadFile(@RequestPart("file") MultipartFile file){
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,null, "Please Select a file")
            );
        }
        String fileType;
        fileType = file.getContentType();
        if(fileType == null){
            throw new InvalidRequestException("Could not determine file type");
        }

        if(fileType.startsWith("image/")){
            return uploadPicture(file);
        } else if(fileType.startsWith("video/")){
            return uploadVideo(file);
        } else{
            throw new InvalidRequestException("Unable to determine the file type");
        }

    }

    public ResponseEntity<ApiResponse<FileDTO>> uploadPicture(MultipartFile picture) {

        if (picture.getSize() > attachmentSizeLimit) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ApiResponse<>(
                    false, null,"Picture size exceeds the limit"
                    )
            );
        }
        //check if the path exists
        if(!Files.exists(Paths.get(pictureDirectory))){
            //create the directory if it doesn't exist
            try {
                Files.createDirectories(Paths.get(pictureDirectory));

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new ApiResponse<>(false, null,
                                "Unable to create the directory " + e.getMessage())
                );
            }
        }

        try {
            Path filePath = Paths.get(pictureDirectory, Objects.requireNonNull(picture.getOriginalFilename()));
            Files.copy(picture.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String s = Files.probeContentType(filePath);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    new FileDTO(picture.getOriginalFilename(),filePath.toString()),
                    "Picture uploaded successfully. "));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, null, "Failed to upload picture")
            );
        }
    }

    public ResponseEntity<ApiResponse<FileDTO>> uploadVideo(MultipartFile video) {

        if (video.getSize() > attachmentSizeLimit) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ApiResponse<>(
                            false, null,"Video size exceeds the limit"
                    )
            );
        }

        //check if the path exists
        if(!Files.exists(Paths.get(videoDirectory))){
            //create the directory if it doesn't exist
            try {
                Files.createDirectories(Paths.get(videoDirectory));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new ApiResponse<>(false, null,
                                "Unable to create the directory " + e.getMessage())
                );
            }
        }
        try {
            Path filePath = Paths.get(videoDirectory, Objects.requireNonNull(video.getOriginalFilename()));
            Files.copy(video.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    new FileDTO(video.getOriginalFilename(), filePath.toString()),
                    "Video uploaded successfully. "));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, null, "Failed to upload the video")
            );
        }
    }

    @DeleteMapping("/delete-file")
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @RequestParam("file-path") String filePath){

        Path path = Paths.get(filePath);

        if(!Files.exists(path)){
            return ResponseEntity.notFound().build();
        }

        try {
            Files.delete(path);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "",
                    "File Deleted Successfully"
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false,
                            "",
                            "Failed to delete the file " + e.getMessage()));
        }

    }
}



