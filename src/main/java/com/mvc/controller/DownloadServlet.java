package com.mvc.controller;

import com.common.DBContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Tải hoặc xem file PDF đã convert.
@WebServlet("/download")
public class DownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String taskParam = request.getParameter("taskId");
        if (taskParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu taskId");
            return;
        }

        try {
            int taskId = Integer.parseInt(taskParam);
            FileDescriptor descriptor = fetchDescriptor(taskId);
            if (descriptor == null || descriptor.outputPath == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy file đã convert");
                return;
            }

            File file = new File(descriptor.outputPath);
            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File không tồn tại trên server");
                return;
            }

            boolean inline = "inline".equalsIgnoreCase(request.getParameter("mode"));
            String encodedName = URLEncoder.encode(descriptor.displayName, StandardCharsets.UTF_8).replaceAll("\\+",
                    "%20");
            String disposition = (inline ? "inline" : "attachment") + "; filename=\"" + encodedName + "\"";

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", disposition);
            response.setContentLengthLong(file.length());

            try (FileInputStream fis = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "taskId không hợp lệ");
        } catch (SQLException e) {
            throw new ServletException("Database error: " + e.getMessage(), e);
        }
    }

    private FileDescriptor fetchDescriptor(int taskId) throws SQLException {
        String sql = "SELECT COALESCE(display_name, original_filename) AS display_name, file_path_output "
                + "FROM tasks WHERE id = ? AND status = 'COMPLETED'";

        try (Connection conn = DBContext.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                FileDescriptor descriptor = new FileDescriptor();
                descriptor.displayName = rs.getString("display_name");
                if (descriptor.displayName == null || descriptor.displayName.isEmpty()) {
                    descriptor.displayName = "converted.pdf";
                } else if (!descriptor.displayName.toLowerCase().endsWith(".pdf")) {
                    descriptor.displayName = descriptor.displayName.replaceAll("(?i)\\.docx$", "") + ".pdf";
                }
                descriptor.outputPath = rs.getString("file_path_output");
                return descriptor;
            }
        }
        return null;
    }

    private static class FileDescriptor {
        String displayName;
        String outputPath;
    }
}
