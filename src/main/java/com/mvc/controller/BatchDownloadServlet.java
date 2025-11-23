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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// Tải toàn bộ file PDF của một batch dưới dạng ZIP.
@WebServlet("/download-all")
public class BatchDownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String batchParam = request.getParameter("batchId");
        if (batchParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu batchId");
            return;
        }

        try {
            int batchId = Integer.parseInt(batchParam);
            List<FileEntry> files = fetchFiles(batchId);

            if (files.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Batch chưa có file hoàn thành");
                return;
            }

            String zipName = "batch-" + batchId + "-converted.zip";
            String encoded = URLEncoder.encode(zipName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encoded + "\"");

            try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
                Set<String> usedNames = new HashSet<>();
                byte[] buffer = new byte[8192];
                for (FileEntry entry : files) {
                    File file = new File(entry.path);
                    if (!file.exists()) {
                        continue;
                    }
                    String entryName = ensureUniqueName(entry.fileName, usedNames);
                    ZipEntry zipEntry = new ZipEntry(entryName);
                    zos.putNextEntry(zipEntry);
                    try (FileInputStream fis = new FileInputStream(file)) {
                        int len;
                        while ((len = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }
                    zos.closeEntry();
                }
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "batchId không hợp lệ");
        } catch (SQLException e) {
            throw new ServletException("DB error: " + e.getMessage(), e);
        }
    }

    private String ensureUniqueName(String baseName, Set<String> used) {
        String candidate = baseName;
        int counter = 1;
        int dot = baseName.lastIndexOf('.');
        String prefix = dot >= 0 ? baseName.substring(0, dot) : baseName;
        String suffix = dot >= 0 ? baseName.substring(dot) : "";

        while (used.contains(candidate)) {
            candidate = prefix + "(" + counter++ + ")" + suffix;
        }

        used.add(candidate);
        return candidate;
    }

    private List<FileEntry> fetchFiles(int batchId) throws SQLException {
        String sql = "SELECT COALESCE(display_name, original_filename) AS name, file_path_output "
                + "FROM tasks WHERE batch_id = ? AND status = 'COMPLETED' AND file_path_output IS NOT NULL";

        List<FileEntry> files = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, batchId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                if (name == null) {
                    name = "document.pdf";
                } else if (!name.toLowerCase().endsWith(".pdf")) {
                    name = name.replaceAll("(?i)\\.docx$", "") + ".pdf";
                }
                FileEntry entry = new FileEntry();
                entry.fileName = name;
                entry.path = rs.getString("file_path_output");
                if (entry.path != null) {
                    files.add(entry);
                }
            }
        }
        return files;
    }

    private static class FileEntry {
        String fileName;
        String path;
    }
}
