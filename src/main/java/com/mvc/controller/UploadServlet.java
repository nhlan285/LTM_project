package com.mvc.controller;

import com.common.DBContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * =====================================================
 * UploadServlet - WEB CONTROLLER (MODULE A)
 * =====================================================
 * Purpose: Handles file upload from user and sends conversion request to Server
 * 
 * WORKFLOW:
 * 1. User uploads .docx file via web form
 * 2. Servlet saves file to disk (uploads folder)
 * 3. Servlet inserts record to database (status = PENDING)
 * 4. Servlet opens TCP Socket to Conversion Server (Module B)
 * 5. Servlet sends "taskId|filePath" to Server
 * 6. Servlet closes Socket immediately (Fire and Forget)
 * 7. User is redirected to status page to monitor progress
 * 
 * NETWORKING CONCEPT:
 * - This servlet acts as a TCP CLIENT
 * - It connects to ServerMain (which is the TCP SERVER)
 * - Communication is ONE-WAY (no response expected)
 * 
 * @author Your Name
 * @course Network Programming - Final Project
 */
@WebServlet("/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class UploadServlet extends HttpServlet {

    // Configuration
    private static final String UPLOAD_DIR = "uploads";
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9999;

    /**
     * Initialize servlet - Create uploads directory if not exists
     */
    @Override
    public void init() throws ServletException {
        super.init();

        // Get absolute path to uploads directory
        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
        File uploadDir = new File(uploadPath);

        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                System.out.println("[UploadServlet] Uploads directory created: " + uploadPath);
            } else {
                System.err.println("[UploadServlet] Failed to create uploads directory!");
            }
        }
    }

    /**
     * Handle POST request (File upload)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("[UploadServlet] Received file upload request");

        try {
            List<IncomingFile> incomingFiles = collectFiles(request);

            if (incomingFiles.isEmpty()) {
                request.setAttribute("error", "Không có file nào được chọn!");
                request.getRequestDispatcher("index.jsp").forward(request, response);
                return;
            }

            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
            int batchId = createBatchRecord(incomingFiles.size());

            if (batchId == -1) {
                request.setAttribute("error", "Không thể tạo batch mới trong cơ sở dữ liệu!");
                request.getRequestDispatcher("index.jsp").forward(request, response);
                return;
            }

            List<Integer> taskIds = new ArrayList<>();
            int sequence = 0;

            for (IncomingFile file : incomingFiles) {
                String sanitized = sanitizeFileName(file.originalFileName);
                String storedName = generateStoredFileName(sequence, sanitized);
                String filePath = uploadPath + File.separator + storedName;

                file.part.write(filePath);
                System.out.println("[UploadServlet] File saved to: " + filePath);

                int taskId = insertTaskToDatabase(batchId, sequence, file.originalFileName, filePath);

                if (taskId == -1) {
                    request.setAttribute("error", "Không thể ghi dữ liệu task vào database.");
                    request.getRequestDispatcher("index.jsp").forward(request, response);
                    return;
                }

                boolean sent = sendRequestToServer(taskId, filePath);

                if (!sent) {
                    request.setAttribute("error", "Conversion server đang tạm thời không phản hồi. Vui lòng thử lại.");
                    request.getRequestDispatcher("index.jsp").forward(request, response);
                    return;
                }

                taskIds.add(taskId);
                sequence++;
            }

            if (taskIds.size() == 1) {
                response.sendRedirect("status.jsp?taskId=" + taskIds.get(0) + "&batchId=" + batchId);
            } else {
                response.sendRedirect("batch-status.jsp?batchId=" + batchId);
            }

        } catch (Exception e) {
            System.err.println("[UploadServlet] Error: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Upload failed: " + e.getMessage());
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }

    /**
     * Extract filename from multipart request
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return "";
    }

    /**
     * Insert task record to database
     * 
     * @param fileName Original filename
     * @param filePath Full path to saved file
     * @return The auto-generated task ID, or -1 if failed
     */
    private int insertTaskToDatabase(int batchId, int sequenceOrder, String displayName, String filePath) {
        String sql = "INSERT INTO tasks (batch_id, sequence_order, display_name, original_filename, file_path_input, status) "
                + "VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DBContext.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, batchId);
            stmt.setInt(2, sequenceOrder);
            stmt.setString(3, displayName);
            stmt.setString(4, displayName);
            stmt.setString(5, filePath);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the auto-generated ID
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("[UploadServlet] Database error: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    private int createBatchRecord(int totalFiles) {
        String sql = "INSERT INTO upload_batches (total_files, completed_files, status) VALUES (?, 0, 'PENDING')";

        try (Connection conn = DBContext.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, totalFiles);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[UploadServlet] Database error when creating batch: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    private List<IncomingFile> collectFiles(HttpServletRequest request) throws IOException, ServletException {
        Collection<Part> parts = request.getParts();
        List<IncomingFile> files = new ArrayList<>();

        for (Part part : parts) {
            if ((!"files".equals(part.getName()) && !"file".equals(part.getName())) || part.getSize() == 0) {
                continue;
            }

            String fileName = extractFileName(part);
            if (fileName == null || fileName.isEmpty()) {
                continue;
            }

            if (!fileName.toLowerCase().endsWith(".docx")) {
                throw new ServletException("File " + fileName + " không đúng định dạng .docx");
            }

            files.add(new IncomingFile(part, fileName));
        }

        return files;
    }

    private String sanitizeFileName(String original) {
        String base = original == null ? "file.docx" : original;
        base = Paths.get(base).getFileName().toString();
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String generateStoredFileName(int sequence, String sanitizedName) {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        return timestamp + "_" + sequence + "_" + sanitizedName;
    }

    private static class IncomingFile {
        private final Part part;
        private final String originalFileName;

        private IncomingFile(Part part, String originalFileName) {
            this.part = part;
            this.originalFileName = originalFileName;
        }
    }

    /**
     * Send conversion request to Server via TCP Socket
     * 
     * NETWORKING EXPLANATION:
     * 1. Create a Socket connection to localhost:9999
     * 2. Send a single line: "taskId|filePath"
     * 3. Close socket immediately (Fire and Forget)
     * 4. Don't wait for response (Asynchronous)
     * 
     * @param taskId   The database task ID
     * @param filePath Full path to the DOCX file
     * @return true if sent successfully, false otherwise
     */
    private boolean sendRequestToServer(int taskId, String filePath) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            // Format: "taskId|filePath"
            String message = taskId + "|" + filePath;

            // Send to server
            writer.println(message);

            System.out.println("[UploadServlet] Sent to server: " + message);

            return true;

        } catch (IOException e) {
            System.err.println("[UploadServlet] Cannot connect to Conversion Server: " + e.getMessage());
            System.err.println("                 Make sure ServerMain is running on port " + SERVER_PORT);
            return false;
        }
    }
}
