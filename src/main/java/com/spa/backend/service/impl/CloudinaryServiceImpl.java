package com.spa.backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spa.backend.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryServiceImpl.class);

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Map uploadImage(MultipartFile file) throws IOException {
        log.debug("Uploading to Cloudinary: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            log.info("Cloudinary upload successful. Public ID: {}, URL: {}", result.get("public_id"), result.get("secure_url"));
            return result;
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Map deleteImage(String publicId) throws IOException {
        log.debug("Deleting from Cloudinary: {}", publicId);
        
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Cloudinary delete result for {}: {}", publicId, result.get("result"));
            return result;
        } catch (IOException e) {
            log.error("Cloudinary delete failed for {}: {}", publicId, e.getMessage(), e);
            throw e;
        }
    }
}
