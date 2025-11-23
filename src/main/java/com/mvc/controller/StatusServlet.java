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
 * AJAX POLLING
 * Mục đích: Cung cấp thông tin trạng thái của tác vụ dưới dạng JSON
 * 
 * MÔ HÌNH GIAO TIẾP: Polling (Client-side)
 * - Trang JSP dùng AJAX gọi servlet này mỗi 2 giây
 * - Servlet truy vấn cơ sở dữ liệu và trả về JSON
 * - JavaScript phía client cập nhật giao diện dựa trên trạng thái
 * 
 * VÍ DỤ PHẢN HỒI:
 * {
 *   "status": "COMPLETED",
 *   "message": "Chuyển đổi thành công!",
 *   "downloadUrl": "/uploads/document.pdf"
 * }
 */

@WebServlet("/api/status")
public class StatusServlet extends HttpServlet {

    private final Gson gson = new Gson();

//      /api/status?taskId=123
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            String taskIdParam = request.getParameter("taskId");

            if (taskIdParam == null || taskIdParam.isEmpty()) {
                out.print(gson.toJson(createErrorResponse("Missing taskId parameter")));
                return;
            }

            int taskId = Integer.parseInt(taskIdParam);

            Map<String, Object> taskInfo = getTaskStatus(taskId);

            if (taskInfo == null) {
                out.print(gson.toJson(createErrorResponse("Task not found")));
                return;
            }

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


    private Map<String, Object> getTaskStatus(int taskId) {
        String sql = "SELECT status, file_path_output, error_message, created_at, batch_id, display_name, original_filename "
                + "FROM tasks WHERE id = ?";

        try (Connection conn = DBContext.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> result = new HashMap<>();

                String status = rs.getString("status");
                String outputPath = rs.getString("file_path_output");
                String errorMessage = rs.getString("error_message");
                Integer batchId = rs.getInt("batch_id");
                if (rs.wasNull()) {
                    batchId = null;
                }

                String displayName = rs.getString("display_name");
                if (displayName == null || displayName.isEmpty()) {
                    displayName = rs.getString("original_filename");
                }

                result.put("status", status);
                result.put("taskId", taskId);
                result.put("displayName", displayName);
                if (batchId != null) {
                    result.put("batchId", batchId);
                }

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
                            result.put("downloadUrl", "download?taskId=" + taskId);
                            result.put("previewUrl", "download?mode=inline&taskId=" + taskId);
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

    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", errorMessage);
        return result;
    }
}
