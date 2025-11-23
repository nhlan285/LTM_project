package com.server.core;

import com.common.DBContext;
import com.server.converter.DocxToPdfService;
import com.server.model.TaskRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

public class WorkerTask implements Runnable {

    private final BlockingQueue<TaskRequest> taskQueue;
    private final DocxToPdfService converter;
    private final int workerId;

    public WorkerTask(BlockingQueue<TaskRequest> taskQueue, int workerId) {
        this.taskQueue = taskQueue;
        this.workerId = workerId;
        this.converter = new DocxToPdfService();
    }

    @Override
    public void run() {
        System.out.println("[Worker-" + workerId + "] Ready.");
        while (true) {
            TaskRequest task = null;
            try {
                task = taskQueue.take(); // Blocking call
                System.out.println("[Worker-" + workerId + "] Processing task: " + task.getTaskId());

                processTask(task);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable t) {
                t.printStackTrace();
                if (task != null) {
                    updateTaskStatus(task.getTaskId(), "FAILED", t.getMessage(), null);
                }
            }
        }
    }

    private void processTask(TaskRequest task) {
        int taskId = task.getTaskId();
        String inputPath = task.getInputFilePath();
        String outputPath = inputPath.replace(".docx", ".pdf");

        try {
            updateTaskStatus(taskId, "PROCESSING", null, null);

            // SỬA QUAN TRỌNG: Truyền workerId vào để tạo môi trường riêng
            converter.convertDocxToPdf(inputPath, outputPath, workerId);

            updateTaskStatus(taskId, "COMPLETED", null, outputPath);
            System.out.println("[Worker-" + workerId + "] ✅ Task " + taskId + " Done!");

        } catch (Exception e) {
            System.err.println("[Worker-" + workerId + "] ❌ Failed: " + e.getMessage());
            updateTaskStatus(taskId, "FAILED", e.getMessage(), null);
        }
    }

    // ... (Giữ nguyên phần updateTaskStatus và updateBatchSummary như code cũ của bạn) ...
    private void updateTaskStatus(int taskId, String status, String errorMessage, String outputPath) {
        String updateTaskSql = "UPDATE tasks SET status = ?, error_message = ?, file_path_output = ? WHERE id = ?";
        String fetchBatchSql = "SELECT batch_id FROM tasks WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            Integer batchId = null;

            try (PreparedStatement updateStmt = conn.prepareStatement(updateTaskSql);
                 PreparedStatement batchStmt = conn.prepareStatement(fetchBatchSql)) {

                updateStmt.setString(1, status);
                updateStmt.setString(2, errorMessage);
                updateStmt.setString(3, outputPath);
                updateStmt.setInt(4, taskId);
                updateStmt.executeUpdate();

                batchStmt.setInt(1, taskId);
                try (ResultSet rs = batchStmt.executeQuery()) {
                    if (rs.next()) {
                        batchId = rs.getObject("batch_id", Integer.class);
                    }
                }
            }

            if (batchId != null) {
                updateBatchSummary(conn, batchId);
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    
    // ... Copy method updateBatchSummary từ code cũ vào đây ...
    private void updateBatchSummary(Connection conn, int batchId) throws SQLException {
        // (Giữ nguyên logic của bạn để tiết kiệm độ dài câu trả lời)
        String totalSql = "SELECT total_files FROM upload_batches WHERE id = ? FOR UPDATE";
        String countSql = "SELECT status, COUNT(*) AS cnt FROM tasks WHERE batch_id = ? GROUP BY status";
        String updateSql = "UPDATE upload_batches SET completed_files = ?, status = ? WHERE id = ?";

        int totalFiles = 0;
        try (PreparedStatement totalStmt = conn.prepareStatement(totalSql)) {
            totalStmt.setInt(1, batchId);
            try (ResultSet rs = totalStmt.executeQuery()) {
                if (rs.next()) totalFiles = rs.getInt("total_files");
            }
        }
        if (totalFiles == 0) return;

        int completed = 0, failed = 0, processing = 0, pending = 0;
        try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            countStmt.setInt(1, batchId);
            try (ResultSet rs = countStmt.executeQuery()) {
                while (rs.next()) {
                    String s = rs.getString("status");
                    int c = rs.getInt("cnt");
                    if ("COMPLETED".equals(s)) completed = c;
                    else if ("FAILED".equals(s)) failed = c;
                    else if ("PROCESSING".equals(s)) processing = c;
                    else pending = c;
                }
            }
        }

        String batchStatus = "PENDING";
        if (failed > 0) batchStatus = "FAILED";
        else if (completed >= totalFiles) batchStatus = "COMPLETED";
        else if (processing > 0) batchStatus = "PROCESSING";

        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setInt(1, completed);
            updateStmt.setString(2, batchStatus);
            updateStmt.setInt(3, batchId);
            updateStmt.executeUpdate();
        }
    }
}