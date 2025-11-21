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
            // ==========================================
            // STEP 1: Get uploaded file from request
            // ==========================================
            Part filePart = request.getPart("file");

            if (filePart == null) {
                request.setAttribute("error", "No file uploaded!");
                request.getRequestDispatcher("index.jsp").forward(request, response);
                return;
            }

            String fileName = extractFileName(filePart);

            // Validate file extension
            if (!fileName.toLowerCase().endsWith(".docx")) {
                request.setAttribute("error", "Only .docx files are allowed!");
                request.getRequestDispatcher("index.jsp").forward(request, response);
                return;
            }

            System.out.println("[UploadServlet] File name: " + fileName);

            // ==========================================
            // STEP 2: Save file to disk
            // ==========================================
            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
            String filePath = uploadPath + File.separator + fileName;

            // Save the file
            filePart.write(filePath);
            System.out.println("[UploadServlet] File saved to: " + filePath);

            // ==========================================
            // STEP 3: Insert record to database
            // ==========================================
            int taskId = insertTaskToDatabase(fileName, filePath);

            if (taskId == -1) {
                request.setAttribute("error", "Database error! Please try again.");
                request.getRequestDispatcher("index.jsp").forward(request, response);
                return;
            }

            System.out.println("[UploadServlet] Task created in DB with ID: " + taskId);

            // ==========================================
            // STEP 4: Send request to Conversion Server
            // ==========================================
            boolean sent = sendRequestToServer(taskId, filePath);

            if (!sent) {
                request.setAttribute("error", "Conversion server is not available!");
                request.getRequestDispatcher("index.jsp").forward(request, response);
                return;
            }

            System.out.println("[UploadServlet] Request sent to Conversion Server");

            // ==========================================
            // STEP 5: Redirect to status page
            // ==========================================
            response.sendRedirect("status.jsp?taskId=" + taskId);

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
    private int insertTaskToDatabase(String fileName, String filePath) {
        String sql = "INSERT INTO tasks (original_filename, file_path_input, status) VALUES (?, ?, 'PENDING')";

        try (Connection conn = DBContext.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, fileName);
            stmt.setString(2, filePath);

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
