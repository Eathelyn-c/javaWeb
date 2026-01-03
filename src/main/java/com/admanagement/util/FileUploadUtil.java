package com.admanagement.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import com.jcraft.jsch.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
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
    
    // Remote server configuration
    private static final boolean REMOTE_UPLOAD_ENABLED;
    private static final String REMOTE_HOST;
    private static final int REMOTE_PORT;
    private static final String REMOTE_USER;
    private static final String REMOTE_PASSWORD;
    private static final String REMOTE_UPLOAD_PATH;
    
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    ));
    
    private static final Set<String> ALLOWED_VIDEO_TYPES = new HashSet<>(Arrays.asList(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/webm"
    ));

    static {
        String storageRoot = DEFAULT_STORAGE_ROOT;
        String publicBaseUrl = "";
        boolean remoteEnabled = false;
        String remoteHost = "";
        int remotePort = 22;
        String remoteUser = "";
        String remotePassword = "";
        String remotePath = "";

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
                
                // Load remote server configuration
                remoteEnabled = Boolean.parseBoolean(props.getProperty("remote.upload.enabled", "false"));
                remoteHost = props.getProperty("remote.server.host", "");
                remotePort = Integer.parseInt(props.getProperty("remote.server.port", "22"));
                remoteUser = props.getProperty("remote.server.user", "");
                remotePassword = props.getProperty("remote.server.password", "");
                remotePath = props.getProperty("remote.upload.path", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        STORAGE_ROOT = storageRoot;
        PUBLIC_BASE_URL = publicBaseUrl;
        REMOTE_UPLOAD_ENABLED = remoteEnabled;
        REMOTE_HOST = remoteHost;
        REMOTE_PORT = remotePort;
        REMOTE_USER = remoteUser;
        REMOTE_PASSWORD = remotePassword;
        REMOTE_UPLOAD_PATH = remotePath;
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
        
        // Upload to remote server if enabled
        if (REMOTE_UPLOAD_ENABLED) {
            try {
                uploadToRemoteServer(fullPath, uniqueFileName);
            } catch (Exception e) {
                System.err.println("Failed to upload to remote server: " + e.getMessage());
                e.printStackTrace();
                // Continue even if remote upload fails
            }
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
    
    /**
     * Upload file to remote server via SCP command
     */
    private static void uploadToRemoteServer(String localFilePath, String fileName) throws Exception {
        try {
            // Use sshpass and scp command for more reliable upload
            String remotePath = REMOTE_UPLOAD_PATH + fileName;
            
            System.out.println("Uploading file to remote server: " + fileName);
            
            // Build scp command
            ProcessBuilder pb = new ProcessBuilder(
                "sshpass", "-p", REMOTE_PASSWORD,
                "scp", 
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=/dev/null",
                "-P", String.valueOf(REMOTE_PORT),
                localFilePath,
                REMOTE_USER + "@" + REMOTE_HOST + ":" + remotePath
            );
            
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Read output
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("SCP output: " + line);
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("Successfully uploaded " + fileName + " to " + REMOTE_HOST);
            } else {
                throw new Exception("SCP command failed with exit code: " + exitCode);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to upload via SCP: " + e.getMessage());
            // Fallback to JSch if sshpass is not available
            uploadViaJSch(localFilePath, fileName);
        }
    }
    
    /**
     * Fallback: Upload file using JSch library
     */
    private static void uploadViaJSch(String localFilePath, String fileName) throws Exception {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;
        
        try {
            // Create SSH session
            session = jsch.getSession(REMOTE_USER, REMOTE_HOST, REMOTE_PORT);
            session.setPassword(REMOTE_PASSWORD);
            
            // Disable strict host key checking and configure session
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "password");
            session.setConfig(config);
            
            // Set timeouts
            session.setTimeout(30000); // 30 seconds session timeout
            
            // Connect
            System.out.println("Connecting to remote server: " + REMOTE_HOST + ":" + REMOTE_PORT);
            session.connect(30000); // 30 seconds connection timeout
            System.out.println("SSH session connected");
            
            // Open SFTP channel
            Channel channel = session.openChannel("sftp");
            channel.connect(10000); // 10 seconds channel timeout
            sftpChannel = (ChannelSftp) channel;
            System.out.println("SFTP channel opened");
            
            // Create remote directory if not exists
            try {
                sftpChannel.cd(REMOTE_UPLOAD_PATH);
                System.out.println("Remote directory exists: " + REMOTE_UPLOAD_PATH);
            } catch (SftpException e) {
                // Directory doesn't exist, create it
                System.out.println("Creating remote directory: " + REMOTE_UPLOAD_PATH);
                createRemoteDirectory(sftpChannel, REMOTE_UPLOAD_PATH);
                sftpChannel.cd(REMOTE_UPLOAD_PATH);
            }
            
            // Upload file
            System.out.println("Uploading file: " + fileName);
            FileInputStream fis = new FileInputStream(localFilePath);
            sftpChannel.put(fis, fileName);
            fis.close();
            
            System.out.println("Successfully uploaded " + fileName + " to " + REMOTE_HOST);
            
        } finally {
            // Close connections
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
    
    /**
     * Create remote directory recursively
     */
    private static void createRemoteDirectory(ChannelSftp sftp, String path) throws SftpException {
        String[] dirs = path.split("/");
        String currentPath = "";
        
        for (String dir : dirs) {
            if (dir.isEmpty()) {
                continue;
            }
            
            currentPath += "/" + dir;
            try {
                sftp.cd(currentPath);
            } catch (SftpException e) {
                sftp.mkdir(currentPath);
                sftp.cd(currentPath);
            }
        }
    }
}
