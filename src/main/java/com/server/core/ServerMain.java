package com.server.core;

import com.server.model.TaskRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerMain {

    private static final int PORT = 9999;
    
    // Số lượng worker
    private static final int WORKER_THREAD_COUNT = 3;

    // Shared resources
    private static BlockingQueue<TaskRequest> taskQueue;
    private static ExecutorService workerPool;

    public static void main(String[] args) {
        System.out.println("===================================================");
        System.out.println("  FILE CONVERSION SERVER (MODULE B) - STARTING");
        System.out.println("===================================================");

        // queue
        taskQueue = new LinkedBlockingQueue<>();
        System.out.println("[Server] BlockingQueue initialized.");

        // pool
        workerPool = Executors.newFixedThreadPool(WORKER_THREAD_COUNT);
        System.out.println("[Server] ThreadPool created with " + WORKER_THREAD_COUNT + " workers.");

        // submit WorkerTask
        for (int i = 1; i <= WORKER_THREAD_COUNT; i++) {
            workerPool.submit(new WorkerTask(taskQueue, i));
        }

        startServer();
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Server] ✅ Server started on port " + PORT);
            System.out.println("[Server] Waiting for connections...\n");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClientRequest(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("[Server] Cannot start server: " + e.getMessage());
        } finally {
            if (workerPool != null) workerPool.shutdown();
        }
    }

    private static void handleClientRequest(Socket socket) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            String request = reader.readLine();
            if (request != null && !request.isEmpty()) {
                System.out.println("[Server] Received: " + request);
                String[] parts = request.split("\\|");

                if (parts.length == 2) {
                    int taskId = Integer.parseInt(parts[0].trim());
                    String filePath = parts[1].trim();
                    TaskRequest task = new TaskRequest(taskId, filePath);
                    
                    taskQueue.put(task); // Add to queue
                    System.out.println("[Server] ✅ Task " + taskId + " queued. Size: " + taskQueue.size());
                }
            }
        } catch (Exception e) {
            System.err.println("[Server] Error handling request: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}