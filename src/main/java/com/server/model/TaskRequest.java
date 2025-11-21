package com.server.model;

/**
 * =====================================================
 * TaskRequest - DATA TRANSFER OBJECT (DTO)
 * =====================================================
 * Purpose: Represents a file conversion task
 * 
 * This object is passed through the BlockingQueue from:
 * - Producer (Socket Accept Thread) --> Queue --> Consumer (Worker Threads)
 * 
 * IMMUTABLE CLASS: Once created, cannot be modified (Thread-Safe)
 * 
 * @author Your Name
 * @course Network Programming - Final Project
 */
public class TaskRequest {

    private final int taskId;
    private final String inputFilePath;

    /**
     * Constructor
     * 
     * @param taskId        The database ID of the task
     * @param inputFilePath Full path to the DOCX file to convert
     */
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
