package com.spa.backend.rest;

import com.spa.backend.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/v1/api/images")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("=== UPLOAD IMAGE REQUEST ===");
        log.info("File name: {}", file.getOriginalFilename());
        log.info("File size: {} bytes", file.getSize());
        log.info("Content type: {}", file.getContentType());
        
        try {
            Map result = cloudinaryService.uploadImage(file);
            log.info("Image uploaded successfully: {}", result.get("public_id"));
            log.debug("Full upload result: {}", result);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error uploading image: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error al subir la imagen: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteImage(@RequestParam("public_id") String publicId) {
        log.info("=== DELETE IMAGE REQUEST ===");
        log.info("Public ID to delete: {}", publicId);
        
        try {
            Map result = cloudinaryService.deleteImage(publicId);
            log.info("Image deleted successfully: {}", publicId);
            log.debug("Delete result: {}", result);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error deleting image {}: {}", publicId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error al eliminar la imagen: " + e.getMessage());
        }
    }
}
