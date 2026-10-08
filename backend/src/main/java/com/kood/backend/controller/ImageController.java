package com.kood.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kood.backend.security.UserDetailsImpl;
import com.kood.backend.service.ImageProcessingService;

import lombok.RequiredArgsConstructor;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageProcessingService imageProcessingService;

    @Value("${media.storage.location}")
    private String mediaStorageLocation;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("file") MultipartFile file) {
        String createdImageName = imageProcessingService.processAndSaveImage(file);
        Map<String, String> response = new HashMap<>();
        response.put("imageName", createdImageName);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/private/{filename}")
    public ResponseEntity<Resource> getMedia(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable String filename) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Path privateDir = Paths.get(mediaStorageLocation, "private").toAbsolutePath().normalize();
            Path file = privateDir.resolve(filename).normalize();
            Resource resource = new UrlResource(Objects.requireNonNull(file.toUri()));

            if (!file.startsWith(privateDir)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            MediaType mediaType = Objects.requireNonNull(
                    MediaTypeFactory.getMediaType(resource)
                            .orElse(MediaType.APPLICATION_OCTET_STREAM));

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/public/{filename}")
    public ResponseEntity<Resource> getPublicMedia(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable String filename) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Path publicDir = Paths.get(mediaStorageLocation, "public").toAbsolutePath().normalize();
            Path file = publicDir.resolve(filename).normalize();
            Resource resource = new UrlResource(Objects.requireNonNull(file.toUri()));

            if (!file.startsWith(publicDir)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            MediaType mediaType = Objects.requireNonNull(
                    MediaTypeFactory.getMediaType(resource)
                            .orElse(MediaType.APPLICATION_OCTET_STREAM));

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateImage(@AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("file") MultipartFile file) {
        try {
            System.err.println("Starting image update for user " + userDetails.getId() + ", file name: "
                    + file.getOriginalFilename() + ", size: " + file.getSize());

            String createdImageName = imageProcessingService.updateImage(Objects.requireNonNull(userDetails.getId()),
                    file);
            Map<String, String> response = new HashMap<>();
            response.put("imageName", createdImageName);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println(("Failed to update image for user " + userDetails.getId()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred during image upload."));
        }
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, String>> resetToDefaultImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String newImageName = imageProcessingService.resetImage(Objects.requireNonNull(userDetails.getId()));
        Map<String, String> response = new HashMap<>();
        response.put("imageName", newImageName);
        response.put("message", "User profile Image has been reset to default");
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteImage(@AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String imageName) {
        imageProcessingService.deleteImage(imageName);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Image has been deleted");
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
}
