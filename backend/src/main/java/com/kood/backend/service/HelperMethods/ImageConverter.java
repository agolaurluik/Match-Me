package com.kood.backend.service.HelperMethods;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.coobird.thumbnailator.Thumbnails;

// This can both compress and convert image dimensions (resize) and its result will be saved locally
// This can also force change aspect ratio as well as filetype if need be and even add watermarks
@Component
public class ImageConverter {

    @Value("${media.storage.location}")
    private String mediaStorageLocation;

    public File processImage(File incomingImage) throws IOException {

        final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
        validateFileSize(incomingImage, MAX_FILE_SIZE_BYTES);

        Tika tika = new Tika();
        String mimeType = tika.detect(incomingImage);// Check the file type
        System.out.println("Detected MIME type: " + mimeType);
        if (!mimeType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }
        File publicDir = new File(mediaStorageLocation + "/public");
        File privateDir = new File(mediaStorageLocation + "/private");

        if (!publicDir.exists()) {
            publicDir.mkdirs();
        }
        if (!privateDir.exists()) {
            privateDir.mkdirs();
        }
        String fullPath = mediaStorageLocation + "/private/";
        File outputDir = new File(fullPath);

        String datePrefix = LocalDate.now().toString();
        String uniqueName = datePrefix + "_" + UUID.randomUUID().toString() + ".jpg";
        File output = new File(outputDir, uniqueName);
        try {
            Thumbnails.of(incomingImage)
                    .size(400, 400)
                    .outputQuality(0.80)
                    .toFile(output);// This is the thing that actually creates the image in the output path
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        return output;
    }

    private void validateFileSize(File file, long maxSizeBytes) {
        if (file.length() > maxSizeBytes) {
            throw new IllegalArgumentException("File too large: " + file.length());
        }
    }
}
