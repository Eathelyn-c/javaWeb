package com.admanagement.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * File upload utility
 */
public class FileUploadUtil {
    private static final String UPLOAD_DIRECTORY = "uploads";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_REQUEST_SIZE = 20 * 1024 * 1024; // 20MB
    
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    ));
    
    private static final Set<String> ALLOWED_VIDEO_TYPES = new HashSet<>(Arrays.asList(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/webm"
    ));

    /**
     * Check if request contains multipart content
     */
    public static boolean isMultipartRequest(HttpServletRequest request) {
        return ServletFileUpload.isMultipartContent(request);
    }

    /**
     * Parse multipart request and extract form fields and files
     */
    public static Map<String, Object> parseMultipartRequest(HttpServletRequest request) throws Exception {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> formFields = new HashMap<>();
        Map<String, String> uploadedFiles = new HashMap<>();

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE);
        upload.setSizeMax(MAX_REQUEST_SIZE);

        List<FileItem> items = upload.parseRequest(request);
        
        for (FileItem item : items) {
            if (item.isFormField()) {
                formFields.put(item.getFieldName(), item.getString("UTF-8"));
            } else {
                String fileName = uploadFile(item);
                if (fileName != null) {
                    uploadedFiles.put(item.getFieldName(), fileName);
                }
            }
        }

        result.put("formFields", formFields);
        result.put("uploadedFiles", uploadedFiles);
        
        return result;
    }

    /**
     * Upload a file and return the saved file path
     */
    private static String uploadFile(FileItem item) throws Exception {
        if (item.getSize() == 0) {
            return null;
        }

        String originalFileName = new File(item.getName()).getName();
        String contentType = item.getContentType();
        
        // Validate file type
        if (!isAllowedFileType(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }

        // Generate unique file name
        String extension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        
        // Create upload directory if not exists
        String uploadPath = getUploadPath();
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Save file
        String filePath = uploadPath + File.separator + uniqueFileName;
        try (InputStream input = item.getInputStream()) {
            Files.copy(input, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        }

        // Return relative path
        return UPLOAD_DIRECTORY + "/" + uniqueFileName;
    }

    /**
     * Check if file type is allowed
     */
    private static boolean isAllowedFileType(String contentType) {
        return ALLOWED_IMAGE_TYPES.contains(contentType) || 
               ALLOWED_VIDEO_TYPES.contains(contentType);
    }

    /**
     * Get file extension
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return "";
    }

    /**
     * Get upload path
     */
    private static String getUploadPath() {
        String webappPath = System.getProperty("user.dir");
        return webappPath + File.separator + "src" + File.separator + "main" + 
               File.separator + "webapp" + File.separator + UPLOAD_DIRECTORY;
    }

    /**
     * Delete file
     */
    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        try {
            String fullPath = System.getProperty("user.dir") + File.separator + 
                            "src" + File.separator + "main" + File.separator + 
                            "webapp" + File.separator + filePath;
            File file = new File(fullPath);
            return file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
