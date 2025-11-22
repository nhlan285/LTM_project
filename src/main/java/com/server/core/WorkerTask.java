package com.server.core;

import com.common.DBContext;
import com.server.converter.DocxToPdfService;
import com.server.model.TaskRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

/**
 * =====================================================
 * WorkerTask - CONSUMER (WORKER THREAD)
 * =====================================================
 * Purpose: Continuously takes tasks from BlockingQueue and processes them
 * 
 * THREADING PATTERN: Consumer in Producer-Consumer Pattern
 * 
 * EXECUTION FLOW:
 * 1. Take task from BlockingQueue (BLOCKS if queue is empty)
 * 2. Update DB status to "PROCESSING"
 * 3. Convert DOCX to PDF
 * 4. Update DB status to "COMPLETED" or "FAILED"
 * 
 * This class implements Runnable so it can be executed by ThreadPool
 * 
 * @author Your Name
 * @course Network Programming - Final Project
 */
public class WorkerTask implements Runnable {

    // Shared queue between Producer (ServerMain) and Consumers (WorkerTasks)
    private final BlockingQueue<TaskRequest> taskQueue;

    // Converter service (Stateless, can be shared)
    private final DocxToPdfService converter;

    // Worker identification (for logging)
    private final int workerId;

    /**
     * Constructor
     * 
     * @param taskQueue The shared BlockingQueue
     * @param workerId  Unique ID for this worker thread (for debugging)
     */
    public WorkerTask(BlockingQueue<TaskRequest> taskQueue, int workerId) {
        this.taskQueue = taskQueue;
        this.converter = new DocxToPdfService();
        this.workerId = workerId;
    }

    /**
     * Main execution method - Runs in a separate thread
     * 
     * THREADING EXPLANATION:
     * - This method runs in an INFINITE LOOP
     * - It BLOCKS on taskQueue.take() when no tasks are available
     * - When a task arrives, it wakes up and processes it
     * - Then goes back to waiting for the next task
     */
    @Override
    public void run() {
        System.out.println("[Worker-" + workerId + "] Started and waiting for tasks...");

        // Infinite loop - This thread never dies
        while (true) {
            TaskRequest task = null;
            try {
                // ==========================================
                // BLOCKING CALL: Wait for task from queue
                // ==========================================
                // If queue is empty, this thread will SLEEP here
                // It will automatically WAKE UP when a task is added
                task = taskQueue.take();

                System.out.println("[Worker-" + workerId + "] Picked up task: " + task);

                // Process the task
                processTask(task);

            } catch (InterruptedException e) {
                // This happens when thread is manually stopped
                System.err.println("[Worker-" + workerId + "] Interrupted: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restore interrupt status
                break; // Exit the loop
            } catch (Throwable t) {
                // Handle any unexpected errors
                System.err.println("[Worker-" + workerId + "] Error processing task: " + t.getMessage());
                t.printStackTrace();

                // If we know which task failed, mark it as FAILED in DB
                if (task != null) {
                    updateTaskStatus(task.getTaskId(), "FAILED", t.getMessage(), null);
                }
            }
        }

        System.out.println("[Worker-" + workerId + "] Stopped.");
    }

    /**
     * Process a single task: Convert file and update database
     * 
     * @param task The task to process
     */
    private void processTask(TaskRequest task) {
        int taskId = task.getTaskId();
        String inputPath = task.getInputFilePath();

        // Generate output file path (replace .docx with .pdf)
        String outputPath = inputPath.replace(".docx", ".pdf");

        try {
            // STEP 1: Update DB to "PROCESSING"
            updateTaskStatus(taskId, "PROCESSING", null, null);

            // STEP 2: Perform the conversion (This takes time!)
            converter.convertDocxToPdf(inputPath, outputPath);

            // STEP 3: Update DB to "COMPLETED"
            updateTaskStatus(taskId, "COMPLETED", null, outputPath);

            System.out.println("[Worker-" + workerId + "] ✅ Task " + taskId + " completed successfully!");

        } catch (Exception e) {
            // STEP 4: If conversion fails, update DB to "FAILED"
            System.err.println("[Worker-" + workerId + "] ❌ Task " + taskId + " failed: " + e.getMessage());
            updateTaskStatus(taskId, "FAILED", e.getMessage(), null);
        }
    }

    /**
     * Update task status in database
     * 
     * DATABASE DESIGN:
     * - Each worker thread gets its own Connection (Thread-Safe)
     * - Connection is opened and closed for each update
     * - For production, use Connection Pooling (HikariCP, etc.)
     * 
     * @param taskId       The task ID to update
     * @param status       New status (PROCESSING, COMPLETED, FAILED)
     * @param errorMessage Error message if failed (null otherwise)
     * @param outputPath   Path to converted PDF (null if not completed)
     */
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
                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected == 0) {
                    conn.rollback();
                    System.err.println("[Worker-" + workerId + "] Warning: Task " + taskId + " not found in DB");
                    return;
                }

                batchStmt.setInt(1, taskId);
                try (ResultSet rs = batchStmt.executeQuery()) {
                    if (rs.next()) {
                        batchId = rs.getInt("batch_id");
                        if (rs.wasNull()) {
                            batchId = null;
                        }
                    }
                }
            }

            if (batchId != null) {
                updateBatchSummary(conn, batchId);
            }

            conn.commit();
            System.out.println("[Worker-" + workerId + "] DB updated: Task " + taskId + " -> " + status);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[Worker-" + workerId + "] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            System.err.println("[Worker-" + workerId + "] DB error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("[Worker-" + workerId + "] Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    private void updateBatchSummary(Connection conn, int batchId) throws SQLException {
        String totalSql = "SELECT total_files FROM upload_batches WHERE id = ? FOR UPDATE";
        String countSql = "SELECT status, COUNT(*) AS cnt FROM tasks WHERE batch_id = ? GROUP BY status";
        String updateSql = "UPDATE upload_batches SET completed_files = ?, status = ? WHERE id = ?";

        int totalFiles = 0;
        try (PreparedStatement totalStmt = conn.prepareStatement(totalSql)) {
            totalStmt.setInt(1, batchId);
            try (ResultSet rs = totalStmt.executeQuery()) {
                if (rs.next()) {
                    totalFiles = rs.getInt("total_files");
                }
            }
        }

        if (totalFiles == 0) {
            return;
        }

        int completed = 0;
        int failed = 0;
        int processing = 0;
        int pending = 0;

        try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            countStmt.setInt(1, batchId);
            try (ResultSet rs = countStmt.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int cnt = rs.getInt("cnt");
                    switch (status) {
                        case "COMPLETED":
                            completed = cnt;
                            break;
                        case "FAILED":
                            failed = cnt;
                            break;
                        case "PROCESSING":
                            processing = cnt;
                            break;
                        case "PENDING":
                            pending = cnt;
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        String batchStatus;
        if (failed > 0) {
            batchStatus = "FAILED";
        } else if (completed >= totalFiles && totalFiles > 0) {
            batchStatus = "COMPLETED";
        } else if (processing > 0) {
            batchStatus = "PROCESSING";
        } else if (pending > 0) {
            batchStatus = "PENDING";
        } else {
            batchStatus = "PENDING";
        }

        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setInt(1, completed);
            updateStmt.setString(2, batchStatus);
            updateStmt.setInt(3, batchId);
            updateStmt.executeUpdate();
        }
    }
}
