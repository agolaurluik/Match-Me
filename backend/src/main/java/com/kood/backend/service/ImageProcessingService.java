package com.kood.backend.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kood.backend.entity.UserEntities.User;
import com.kood.backend.exceptions.FileDeletionException;
import com.kood.backend.exceptions.ImageProcessingException;
import com.kood.backend.exceptions.InvalidImageException;
import com.kood.backend.repository.UserRepository;
import com.kood.backend.service.HelperMethods.ImageConverter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private final ImageConverter imageConverter;
    private final UserService userService;
    private final UserRepository userRepository;

    final Path MEDIA_BASE_PATH = Paths.get("../../media").normalize().toAbsolutePath();

    public String processAndSaveImage(MultipartFile newProfileImage) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("uploaded-image-", ".tmp");
            newProfileImage.transferTo(tempFile);
            File processedImage = imageConverter.processImage(tempFile);
            return processedImage.getName();
        } catch (IllegalArgumentException e) {
            throw new ImageProcessingException(e.getMessage());
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to process or save image due to an I/O error: " + e.getMessage(),
                    e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) { // This tries to delete the tempFile - Method call as an Expression
                    System.err.println("Warning: Could not delete temporary file " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    public String updateImage(Long userId, MultipartFile newProfileImage) {
        if (newProfileImage.isEmpty()) {
            throw new InvalidImageException("New profile picture file is empty.");
        }
        User user = userService.getUserById(userId);
        String oldProfileImageName = user.getProfileImageName();

        File tempFile = null;
        String newProfileImageName = null;

        try {
            tempFile = File.createTempFile("profile-upload-", newProfileImage.getOriginalFilename());
            newProfileImage.transferTo(tempFile);
            File processedNewImageFile = imageConverter.processImage(tempFile);
            newProfileImageName = processedNewImageFile.getName();

            user.setProfileImageName(newProfileImageName);
            userRepository.save(user);
            if (oldProfileImageName != null && !oldProfileImageName.equals(newProfileImageName)) {
                try {
                    deleteImage(oldProfileImageName);
                    System.out.println("Old profile picture '" + oldProfileImageName + "' deleted successfully.");
                } catch (FileDeletionException e) {
                    System.err
                            .println("Warning: Could not delete old profile Image URL: '" + oldProfileImageName + "': "
                                    + e.getMessage());
                }
            }
            return newProfileImageName;
        } catch (IllegalArgumentException e) {
            throw new InvalidImageException("Error processing new profile picture: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to handle new profile picture file: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    System.err.println(
                            "Could not delete temporary uploaded file " + tempFile.getAbsolutePath() + e.getMessage());
                }
            }
        }
    }

    public String resetImage(Long userId) {

        User user = userService.getUserById(userId);
        String oldProfileImageName = user.getProfileImageName();

        if (oldProfileImageName != null && !oldProfileImageName.equals("default-user.jpg")) {
            System.err.println("ImageName for user id: " + userId + " still exists, proceeding to delete old data");
            deleteImage(oldProfileImageName);
        }
        user.setProfileImageName("default-user.jpg");
        userRepository.save(user);
        return user.getProfileImageName();
    }

    public void deleteImage(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new FileDeletionException("Filename cannot be empty.");
        }
        Path privateMediaBasePath = MEDIA_BASE_PATH.resolve("private").normalize();
        Path filePathToDelete = privateMediaBasePath.resolve(filename).normalize();

        if (!filePathToDelete.startsWith(privateMediaBasePath)) {
            throw new FileDeletionException("Invalid file path: " + filename + ". Path traversal attempt detected.");
        }
        if (!Files.exists(filePathToDelete)) {
            return; // Nothing to delete
        }
        if (!Files.isRegularFile(filePathToDelete)) { // Such as .txt / .jpg / .mp4 and not a folder or other special
                                                      // type
            throw new FileDeletionException("Path is not a regular file: " + filename);
        }

        try {
            boolean deleted = Files.deleteIfExists(filePathToDelete);

            if (!deleted) {
                throw new FileDeletionException("Failed to delete image: " + filename);
            }
        } catch (IOException e) {
            throw new FileDeletionException("Error deleting image " + filename + ": " + e.getMessage(), e);
        }
    }
}
