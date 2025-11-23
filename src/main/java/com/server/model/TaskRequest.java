package com.server.model;

/**
 * Purpose:file conversion task (DTO)
 * 
 * - Producer (Socket Accept Thread) --> Queue --> Consumer (Worker Threads)
 * 
 * IMMUTABLE CLASS: không thể chỉnh sửa sau khi tạo (Thread-Safe)
 */
public class TaskRequest {

    private final int taskId;
    private final String inputFilePath;

    public TaskRequest(int taskId, String inputFilePath) {
        this.taskId = taskId;
        this.inputFilePath = inputFilePath;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    @Override
    public String toString() {
        return "TaskRequest{" +
                "taskId=" + taskId +
                ", inputFilePath='" + inputFilePath + '\'' +
                '}';
    }
}
