package com.mvc.controller;

import com.common.DBContext;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * =====================================================
 * StatusServlet - REST API FOR AJAX POLLING
 * =====================================================
 * Purpose: Provides task status information in JSON format
 * 
 * COMMUNICATION PATTERN: Polling (Client-side)
 * - The JSP page uses AJAX to call this servlet every 2 seconds
 * - This servlet queries the database and returns JSON
 * - Client-side JavaScript updates the UI based on status
 * 
 * EXAMPLE RESPONSE:
 * {
 * "status": "COMPLETED",
 * "message": "Conversion successful!",
 * "downloadUrl": "/uploads/document.pdf"
 * }
 * 
 * @author Your Name
 * @course Network Programming - Final Project
 */
@WebServlet("/api/status")
public class StatusServlet extends HttpServlet {

    private final Gson gson = new Gson();

    /**
     * Handle GET request - Return task status as JSON
     * 
     * Query Parameter: taskId
     * Example: /api/status?taskId=123
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set response type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            // Get taskId parameter
            String taskIdParam = request.getParameter("taskId");

            if (taskIdParam == null || taskIdParam.isEmpty()) {
                out.print(gson.toJson(createErrorResponse("Missing taskId parameter")));
                return;
            }

            int taskId = Integer.parseInt(taskIdParam);

            // Query database for task status
            Map<String, Object> taskInfo = getTaskStatus(taskId);

            if (taskInfo == null) {
                out.print(gson.toJson(createErrorResponse("Task not found")));
                return;
            }

            // Return task info as JSON
            out.print(gson.toJson(taskInfo));

        } catch (NumberFormatException e) {
            out.print(gson.toJson(createErrorResponse("Invalid taskId format")));
        } catch (Exception e) {
            System.err.println("[StatusServlet] Error: " + e.getMessage());
            e.printStackTrace();
            out.print(gson.toJson(createErrorResponse("Server error: " + e.getMessage())));
        } finally {
            out.flush();
        }
    }

    /**
     * Query database for task status
     * 
     * @param taskId The task ID to query
     * @return Map containing task information, or null if not found
     */
    private Map<String, Object> getTaskStatus(int taskId) {
        String sql = "SELECT status, file_path_output, error_message, created_at FROM tasks WHERE id = ?";

        try (Connection conn = DBContext.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> result = new HashMap<>();

                String status = rs.getString("status");
                String outputPath = rs.getString("file_path_output");
                String errorMessage = rs.getString("error_message");

                result.put("status", status);
                result.put("taskId", taskId);

                // Build user-friendly message
                String message;
                switch (status) {
                    case "PENDING":
                        message = "Your file is in queue, waiting to be processed...";
                        break;
                    case "PROCESSING":
                        message = "Converting your file, please wait...";
                        break;
                    case "COMPLETED":
                        message = "Conversion completed successfully!";
                        // Extract filename from full path
                        if (outputPath != null) {
                            String fileName = outputPath.substring(outputPath.lastIndexOf(java.io.File.separator) + 1);
                            result.put("downloadUrl", "uploads/" + fileName);
                        }
                        break;
                    case "FAILED":
                        message = "Conversion failed: " + (errorMessage != null ? errorMessage : "Unknown error");
                        break;
                    default:
                        message = "Unknown status: " + status;
                }

                result.put("message", message);
                result.put("success", true);

                return result;
            }

        } catch (SQLException e) {
            System.err.println("[StatusServlet] Database error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Create error response
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", errorMessage);
        return result;
    }
}
