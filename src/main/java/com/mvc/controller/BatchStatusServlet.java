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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// REST endpoint trả về toàn bộ trạng thái của một batch upload.
@WebServlet("/api/batch-status")
public class BatchStatusServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String batchParam = request.getParameter("batchId");
        PrintWriter out = response.getWriter();

        if (batchParam == null) {
            out.print(gson.toJson(error("Thiếu batchId")));
            out.flush();
            return;
        }

        try {
            int batchId = Integer.parseInt(batchParam);
            Map<String, Object> payload = buildPayload(batchId);
            if (payload == null) {
                out.print(gson.toJson(error("Batch không tồn tại")));
            } else {
                out.print(gson.toJson(payload));
            }
        } catch (NumberFormatException e) {
            out.print(gson.toJson(error("batchId không hợp lệ")));
        } catch (SQLException e) {
            out.print(gson.toJson(error("Không thể đọc dữ liệu: " + e.getMessage())));
        } finally {
            out.flush();
        }
    }

    private Map<String, Object> buildPayload(int batchId) throws SQLException {
        String batchSql = "SELECT id, total_files, completed_files, status, created_at FROM upload_batches WHERE id = ?";
        String tasksSql = "SELECT id, display_name, original_filename, status, file_path_output, error_message, updated_at "
                + "FROM tasks WHERE batch_id = ? ORDER BY sequence_order ASC, id ASC";

        try (Connection conn = DBContext.getConnection();
                PreparedStatement batchStmt = conn.prepareStatement(batchSql);
                PreparedStatement taskStmt = conn.prepareStatement(tasksSql)) {

            batchStmt.setInt(1, batchId);
            ResultSet batchRs = batchStmt.executeQuery();

            if (!batchRs.next()) {
                return null;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);

            Map<String, Object> batch = new HashMap<>();
            batch.put("id", batchRs.getInt("id"));
            batch.put("total", batchRs.getInt("total_files"));
            batch.put("completed", batchRs.getInt("completed_files"));
            batch.put("status", batchRs.getString("status"));
            batch.put("createdAt", batchRs.getTimestamp("created_at"));

            taskStmt.setInt(1, batchId);
            ResultSet taskRs = taskStmt.executeQuery();

            List<Map<String, Object>> tasks = new ArrayList<>();
            while (taskRs.next()) {
                Map<String, Object> task = new HashMap<>();
                int taskId = taskRs.getInt("id");
                String status = taskRs.getString("status");
                String displayName = taskRs.getString("display_name");

                task.put("id", taskId);
                task.put("status", status);
                task.put("displayName", displayName != null ? displayName : taskRs.getString("original_filename"));
                task.put("error", taskRs.getString("error_message"));
                task.put("updatedAt", taskRs.getTimestamp("updated_at"));

                String outputPath = taskRs.getString("file_path_output");
                if (outputPath != null && !outputPath.isEmpty() && "COMPLETED".equals(status)) {
                    task.put("downloadUrl", "download?taskId=" + taskId);
                    task.put("previewUrl", "download?mode=inline&taskId=" + taskId);
                }

                tasks.add(task);
            }

            result.put("batch", batch);
            result.put("tasks", tasks);
            return result;
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", message);
        return map;
    }
}
