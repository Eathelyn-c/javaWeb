package com.admanagement.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * File upload utility using Jakarta Servlet built-in multipart support
 */
public class FileUploadUtil {
    private static final String UPLOAD_DIRECTORY = "uploads";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_REQUEST_SIZE = 20 * 1024 * 1024; // 20MB
    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_STORAGE_ROOT = System.getProperty("user.dir") + File.separator +
            "src" + File.separator + "main" + File.separator + "webapp" + File.separator + UPLOAD_DIRECTORY;

    private static final String STORAGE_ROOT;
    private static final String PUBLIC_BASE_URL;
    
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    ));
    
    private static final Set<String> ALLOWED_VIDEO_TYPES = new HashSet<>(Arrays.asList(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/webm"
    ));

    static {
        String storageRoot = DEFAULT_STORAGE_ROOT;
        String publicBaseUrl = "";

        try (InputStream input = FileUploadUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);

                String configuredStorage = props.getProperty("upload.storage.path");
                if (configuredStorage != null && !configuredStorage.trim().isEmpty()) {
                    storageRoot = configuredStorage.trim();
                }

                String configuredBaseUrl = props.getProperty("file.public.baseUrl");
                if (configuredBaseUrl != null && !configuredBaseUrl.trim().isEmpty()) {
                    publicBaseUrl = configuredBaseUrl.trim().replaceAll("/+$", "");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        STORAGE_ROOT = storageRoot;
        PUBLIC_BASE_URL = publicBaseUrl;
    }

    /**
     * Check if request contains multipart content
     */
    public static boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/form-data");
    }

    /**
     * Parse multipart request and extract form fields and files using Jakarta Servlet API
     */
    public static Map<String, Object> parseMultipartRequest(HttpServletRequest request) throws Exception {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> formFields = new HashMap<>();
        Map<String, String> uploadedFiles = new HashMap<>();

        try {
            // Use Jakarta Servlet API to get all parts
            Collection<Part> parts = request.getParts();
            
            for (Part part : parts) {
                String fieldName = part.getName();
                
                // Check if this is a form field (no filename) or a file upload (has filename)
                if (part.getSubmittedFileName() == null || part.getSubmittedFileName().isEmpty()) {
                    // It's a form field
                    String value = new String(part.getInputStream().readAllBytes(), "UTF-8");
                    formFields.put(fieldName, value);
                } else {
                    // It's a file upload
                    String fileName = uploadFile(part);
                    if (fileName != null) {
                        uploadedFiles.put(fieldName, fileName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to parse multipart request: " + e.getMessage());
        }

        result.put("formFields", formFields);
        result.put("uploadedFiles", uploadedFiles);
        
        return result;
    }

    /**
     * Upload file from Part object
     */
    private static String uploadFile(Part part) throws Exception {
        String fileName = part.getSubmittedFileName();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        
        // Validate file size
        if (part.getSize() > MAX_FILE_SIZE) {
            return null;
        }
        
        // Get file extension and validate type
        String contentType = part.getContentType();
        if (!isAllowedFileType(contentType)) {
            return null;
        }
        
        // Remove path components from filename for security
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1)
                          .substring(fileName.lastIndexOf('\\') + 1);
        
        // Create unique filename
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueFileName = timestamp + "_" + fileName;
        
        // Create upload directory using configured storage root
        File uploadDir = new File(STORAGE_ROOT);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // Save file
        String fullPath = STORAGE_ROOT + File.separator + uniqueFileName;
        try (InputStream input = part.getInputStream()) {
            Files.copy(input, Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Return full HTTPS URL if configured, otherwise relative path
        if (PUBLIC_BASE_URL != null && !PUBLIC_BASE_URL.isEmpty()) {
            return PUBLIC_BASE_URL + "/" + UPLOAD_DIRECTORY + "/" + uniqueFileName;
        } else {
            return UPLOAD_DIRECTORY + "/" + uniqueFileName;
        }
    }

    /**
     * Check if file type is allowed
     */
    private static boolean isAllowedFileType(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        return ALLOWED_IMAGE_TYPES.contains(contentType) || 
               ALLOWED_VIDEO_TYPES.contains(contentType);
    }

    /**
     * Get image/video info from file path or URL
     */
    public static Map<String, Object> getFileInfo(String filePath) {
        Map<String, Object> info = new HashMap<>();
        
        if (filePath == null || filePath.isEmpty()) {
            return info;
        }
        
        // If it's already a full URL, return basic info
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            info.put("path", filePath);
            info.put("isRemote", true);
            return info;
        }
        
        try {
            // Extract filename from relative path
            String fileName = filePath;
            if (filePath.contains("/")) {
                fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            }
            
            String fullPath = STORAGE_ROOT + File.separator + fileName;
            File file = new File(fullPath);
            
            info.put("exists", file.exists());
            info.put("size", file.length());
            info.put("path", filePath);
            info.put("isRemote", false);
            
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return info;
        }
    }

    /**
     * Delete file (handles both relative paths and full URLs)
     */
    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        // Cannot delete remote URLs
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            return false;
        }
        
        try {
            // Extract filename from relative path
            String fileName = filePath;
            if (filePath.contains("/")) {
                fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            }
            
            String fullPath = STORAGE_ROOT + File.separator + fileName;
            File file = new File(fullPath);
            return file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

