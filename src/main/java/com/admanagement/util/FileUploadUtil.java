package com.admanagement.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import com.jcraft.jsch.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
        
        // Check if remote server is actually localhost
        boolean isRemoteLocalhost = isLocalhost(REMOTE_HOST);
        
        // Upload to remote server or save locally
        if (REMOTE_UPLOAD_ENABLED && !isRemoteLocalhost) {
            try {
                // Create temporary file for upload
                File tempFile = File.createTempFile("upload_", "_" + uniqueFileName);
                try (InputStream input = part.getInputStream()) {
                    Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                
                // Upload to remote server
                uploadToRemoteServer(tempFile.getAbsolutePath(), uniqueFileName);
                
                // Delete temporary file
                tempFile.delete();
                
                System.out.println("File uploaded directly to remote server: " + uniqueFileName);
            } catch (Exception e) {
                System.err.println("Failed to upload to remote server: " + e.getMessage());
                e.printStackTrace();
                throw new Exception("Remote upload failed: " + e.getMessage());
            }
        } else {
            // Save to local storage when:
            // 1. Remote upload is disabled, OR
            // 2. Remote server is actually localhost/same machine
            if (isRemoteLocalhost) {
                System.out.println("Remote server is localhost, saving directly to upload path: " + REMOTE_UPLOAD_PATH);
            } else {
                System.out.println("Remote upload disabled, saving to local storage: " + STORAGE_ROOT);
            }
            
            // Use remote upload path if it's localhost, otherwise use local storage
            String targetPath = isRemoteLocalhost ? REMOTE_UPLOAD_PATH : STORAGE_ROOT;
            String fullPath = targetPath + uniqueFileName;
            File uploadDir = new File(targetPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
                System.out.println("Created upload directory: " + targetPath);
            }
            
            try (InputStream input = part.getInputStream()) {
                Files.copy(input, Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File saved locally: " + fullPath);
            } catch (Exception e) {
                System.err.println("Failed to save file locally: " + e.getMessage());
                e.printStackTrace();
                throw new Exception("Local file save failed: " + e.getMessage());
            }
        }
        
        // Return full URL
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
     * Check if a hostname refers to localhost/current machine
     */
    private static boolean isLocalhost(String host) {
        if (host == null || host.isEmpty()) {
            return false;
        }
        
        // Check common localhost names
        if (host.equals("localhost") || host.equals("127.0.0.1") || host.equals("::1")) {
            return true;
        }
        
        // Check if host IP matches any local network interface
        try {
            java.net.InetAddress remoteAddr = java.net.InetAddress.getByName(host);
            
            // Check if it's loopback
            if (remoteAddr.isLoopbackAddress()) {
                return true;
            }
            
            // Check if it matches any local interface
            java.net.NetworkInterface netInterface;
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                netInterface = interfaces.nextElement();
                java.util.Enumeration<java.net.InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();
                    if (addr.equals(remoteAddr)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking if host is localhost: " + e.getMessage());
        }
        
        return false;
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
     * Try to upload file using system scp command
     * This is more reliable than JSch for some server configurations
     */
    private static boolean trySystemScpUpload(String localFilePath, String fileName) {
        try {
            System.out.println("Attempting upload via system scp command...");
            
            // Build scp command with expect for password authentication
            // expect script to automate password input
            String remoteDest = REMOTE_USER + "@" + REMOTE_HOST + ":" + REMOTE_UPLOAD_PATH + fileName;
            
            // Create expect script to handle password prompt
            String expectScript = String.format(
                "#!/usr/bin/expect -f\n" +
                "set timeout 30\n" +
                "spawn scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null %s %s\n" +
                "expect {\n" +
                "    \"*password:*\" {\n" +
                "        send \"%s\\r\"\n" +
                "        expect eof\n" +
                "    }\n" +
                "    timeout {\n" +
                "        puts \"Timeout waiting for password prompt\"\n" +
                "        exit 1\n" +
                "    }\n" +
                "    eof {\n" +
                "        puts \"Connection closed unexpectedly\"\n" +
                "        exit 1\n" +
                "    }\n" +
                "}\n",
                localFilePath,
                remoteDest,
                REMOTE_PASSWORD
            );
            
            // Write expect script to temporary file
            File expectFile = File.createTempFile("scp_upload_", ".exp");
            expectFile.deleteOnExit();
            try (FileWriter writer = new FileWriter(expectFile)) {
                writer.write(expectScript);
            }
            expectFile.setExecutable(true);
            
            System.out.println("Executing: expect script for scp " + localFilePath + " -> " + remoteDest);
            
            // Execute expect script
            ProcessBuilder pb = new ProcessBuilder("expect", expectFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // Read output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("scp output: " + line);
                }
            }
            
            // Wait for completion (max 30 seconds)
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            
            if (!finished) {
                System.err.println("✗ scp command timed out");
                process.destroyForcibly();
                return false;
            }
            
            int exitCode = process.exitValue();
            
            if (exitCode == 0) {
                System.out.println("✓✓✓ Successfully uploaded via scp: " + fileName + " ✓✓✓");
                return true;
            } else {
                System.err.println("✗ scp command failed with exit code: " + exitCode);
                return false;
            }
            
        } catch (IOException e) {
            // scp command not available or other IO error
            System.out.println("scp command not available: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error executing scp: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Upload file to remote server via SFTP using system scp command
     * This is more reliable than JSch for some server configurations
     */
    private static void uploadToRemoteServer(String localFilePath, String fileName) throws Exception {
        System.out.println("=== Starting remote upload ===");
        System.out.println("File: " + fileName);
        System.out.println("Local file: " + localFilePath);
        System.out.println("Remote host: " + REMOTE_HOST + ":" + REMOTE_PORT);
        System.out.println("Remote user: " + REMOTE_USER);
        System.out.println("Remote path: " + REMOTE_UPLOAD_PATH);
        
        // Try system scp command first (more reliable)
        if (trySystemScpUpload(localFilePath, fileName)) {
            System.out.println("✓ Upload completed successfully via SCP");
            return;
        }
        
        // If scp fails, throw exception with clear error message
        System.err.println("✗ System scp command failed");
        throw new Exception("SCP upload failed - please check remote server SSH configuration and credentials");
        
        // NOTE: JSch fallback disabled due to connection issues with this server
        // The JSch library fails with "connection is closed by foreign host" error
        // If needed in future, uncomment the JSch code below:
        /*
        System.out.println("System scp not available, trying JSch...");
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;
        
        try {
            // Create SSH session
            System.out.println("Creating SSH session...");
            session = jsch.getSession(REMOTE_USER, REMOTE_HOST, REMOTE_PORT);
            session.setPassword(REMOTE_PASSWORD);
            
            // Configure session for maximum compatibility
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "password");
            config.put("userauth.gssapi-with-mic", "no");
            // Support OpenSSH 7.4 key exchange algorithms
            config.put("kex", "diffie-hellman-group-exchange-sha256,diffie-hellman-group-exchange-sha1,diffie-hellman-group14-sha1,diffie-hellman-group1-sha1,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521");
            config.put("server_host_key", "ssh-ed25519,ecdsa-sha2-nistp256,ssh-rsa,ssh-dss");
            config.put("cipher.s2c", "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc");
            config.put("cipher.c2s", "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc");
            config.put("CheckCiphers", "aes128-ctr");
            session.setConfig(config);
            
            // Set longer timeouts
            session.setTimeout(90000); // 90 seconds session timeout
            session.setServerAliveInterval(15000); // Send keepalive every 15s
            
            // Connect with retry
            System.out.println("Connecting to remote server...");
            session.connect(90000); // 90 seconds connection timeout
            System.out.println("✓ SSH session connected successfully");
            
            // Open SFTP channel
            System.out.println("Opening SFTP channel...");
            Channel channel = session.openChannel("sftp");
            channel.connect(30000); // 30 seconds channel timeout
            sftpChannel = (ChannelSftp) channel;
            System.out.println("✓ SFTP channel opened successfully");
            
            // Create remote directory if not exists
            try {
                System.out.println("Checking remote directory: " + REMOTE_UPLOAD_PATH);
                sftpChannel.cd(REMOTE_UPLOAD_PATH);
                System.out.println("✓ Remote directory exists");
            } catch (SftpException e) {
                // Directory doesn't exist, create it
                System.out.println("Remote directory not found, creating: " + REMOTE_UPLOAD_PATH);
                createRemoteDirectory(sftpChannel, REMOTE_UPLOAD_PATH);
                sftpChannel.cd(REMOTE_UPLOAD_PATH);
                System.out.println("✓ Remote directory created successfully");
            }
            
            // Upload file
            System.out.println("Starting file upload: " + fileName);
            FileInputStream fis = new FileInputStream(localFilePath);
            sftpChannel.put(fis, fileName);
            fis.close();
            
            System.out.println("✓✓✓ Successfully uploaded " + fileName + " to " + REMOTE_HOST + " ✓✓✓");
            
        } catch (Exception e) {
            System.err.println("✗✗✗ Upload failed ✗✗✗");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            // Close connections
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
                System.out.println("SFTP channel disconnected");
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
                System.out.println("SSH session disconnected");
            }
        }
        */
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
