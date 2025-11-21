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

/**
 * =====================================================
 * ServerMain - CONVERSION SERVER (MODULE B)
 * =====================================================
 * Purpose: The core of the distributed system - Listens for conversion requests
 * 
 * ARCHITECTURE OVERVIEW:
 * ┌─────────────────────────────────────────────────────────┐
 * │ SERVER MAIN │
 * │ ┌──────────────┐ ┌──────────────┐ │
 * │ │ ServerSocket │───▶│ Accept Thread│ (Producer) │
 * │ │ Port 9999 │ └──────┬───────┘ │
 * │ └──────────────┘ │ │
 * │ ▼ │
 * │ ┌─────────────────┐ │
 * │ │ BlockingQueue │ (Shared Queue) │
 * │ └────────┬────────┘ │
 * │ │ │
 * │ ┌───────────────────┼───────────────────┐ │
 * │ ▼ ▼ ▼ │
 * │ ┌──────────┐ ┌──────────┐ ┌──────────┐ │
 * │ │ Worker-1 │ │ Worker-2 │ │ Worker-3 │ │
 * │ └──────────┘ └──────────┘ └──────────┘ │
 * │ (Consumer) (Consumer) (Consumer) │
 * └─────────────────────────────────────────────────────────┘
 * 
 * KEY CONCEPTS FOR YOUR PROFESSOR:
 * 
 * 1. TCP SOCKET:
 * - ServerSocket listens on port 9999
 * - Accepts connections from Web Server (Module A)
 * 
 * 2. PRODUCER-CONSUMER PATTERN:
 * - Producer: Main thread accepts sockets and adds tasks to queue
 * - Consumer: 3 worker threads process tasks from queue
 * - BlockingQueue: Thread-safe queue that handles synchronization
 * 
 * 3. THREAD POOL:
 * - Fixed pool of 3 threads (prevents resource exhaustion)
 * - Threads are reused (efficient for multiple requests)
 * 
 * 4. NON-BLOCKING:
 * - Socket connection is closed immediately after receiving request
 * - Client doesn't wait for conversion to finish
 * 
 * @author Your Name
 * @course Network Programming - Final Project
 */
public class ServerMain {

    // Server Configuration
    private static final int PORT = 9999;
    private static final int WORKER_THREAD_COUNT = 3;

    // Shared resources
    private static BlockingQueue<TaskRequest> taskQueue;
    private static ExecutorService workerPool;

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  FILE CONVERSION SERVER (MODULE B) - STARTING");
        System.out.println("═══════════════════════════════════════════════════");

        // ==========================================
        // STEP 1: Initialize BlockingQueue
        // ==========================================
        // LinkedBlockingQueue: Thread-safe FIFO queue
        // Capacity: Unlimited (can hold infinite tasks in theory)
        taskQueue = new LinkedBlockingQueue<>();
        System.out.println("[Server] BlockingQueue initialized (Capacity: Unlimited)");

        // ==========================================
        // STEP 2: Create Worker Thread Pool
        // ==========================================
        // FixedThreadPool: Creates exactly 3 threads
        // These threads will run WorkerTask.run() in a loop
        workerPool = Executors.newFixedThreadPool(WORKER_THREAD_COUNT);
        System.out.println("[Server] ThreadPool created with " + WORKER_THREAD_COUNT + " workers");

        // Submit 3 WorkerTask instances to the pool
        for (int i = 1; i <= WORKER_THREAD_COUNT; i++) {
            workerPool.submit(new WorkerTask(taskQueue, i));
        }
        System.out.println("[Server] All worker threads started and waiting for tasks");

        // ==========================================
        // STEP 3: Start ServerSocket (Main Thread becomes Producer)
        // ==========================================
        startServer();
    }

    /**
     * Start the TCP server socket and accept connections
     * 
     * NETWORKING EXPLANATION:
     * - ServerSocket.accept() BLOCKS until a client connects
     * - When client connects, it returns a Socket object
     * - We read the request, create a task, and add to queue
     * - Then IMMEDIATELY close the socket (non-blocking for client)
     */
    private static void startServer() {
        // Try-with-resources: Automatically closes ServerSocket
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("[Server] ✅ Server started on port " + PORT);
            System.out.println("[Server] Waiting for connections from Web Server...\n");

            // ==========================================
            // INFINITE LOOP: Accept connections forever
            // ==========================================
            while (true) {
                try {
                    // BLOCKING CALL: Wait for a client to connect
                    Socket clientSocket = serverSocket.accept();

                    // Client connected! Get client info
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    System.out.println("[Server] 📩 Connection received from: " + clientAddress);

                    // Handle this connection (Parse request and add to queue)
                    handleClientRequest(clientSocket);

                } catch (IOException e) {
                    System.err.println("[Server] Error accepting connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.err.println("[Server] ❌ FATAL: Cannot start server on port " + PORT);
            System.err.println("         Reason: " + e.getMessage());
            System.err.println("         Please check if port is already in use.");
            e.printStackTrace();
        } finally {
            // Shutdown worker threads when server stops
            if (workerPool != null) {
                workerPool.shutdown();
                System.out.println("[Server] Worker threads stopped.");
            }
        }
    }

    /**
     * Handle a single client request
     * 
     * PROTOCOL: Client sends a single line in format "taskId|filePath"
     * Example: "123|C:/uploads/document.docx"
     * 
     * @param socket The connected client socket
     */
    private static void handleClientRequest(Socket socket) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            // Read the request line
            String request = reader.readLine();

            if (request == null || request.isEmpty()) {
                System.err.println("[Server] Received empty request, ignoring.");
                return;
            }

            System.out.println("[Server] Received request: " + request);

            // Parse the request: "taskId|filePath"
            String[] parts = request.split("\\|");

            if (parts.length != 2) {
                System.err.println("[Server] Invalid request format (expected 'taskId|filePath')");
                return;
            }

            int taskId = Integer.parseInt(parts[0].trim());
            String filePath = parts[1].trim();

            // Create a TaskRequest object
            TaskRequest task = new TaskRequest(taskId, filePath);

            // ==========================================
            // ADD TASK TO QUEUE (PRODUCER ACTION)
            // ==========================================
            // This is NON-BLOCKING (queue has unlimited capacity)
            // If we used a bounded queue, this could block when full
            taskQueue.put(task);

            System.out.println("[Server] ✅ Task " + taskId + " added to queue (Queue size: "
                    + taskQueue.size() + ")");

        } catch (NumberFormatException e) {
            System.err.println("[Server] Invalid taskId format: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("[Server] Interrupted while adding to queue: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("[Server] Error reading from socket: " + e.getMessage());
        } finally {
            // Close the socket (Client can now proceed without waiting)
            try {
                socket.close();
                System.out.println("[Server] Connection closed.\n");
            } catch (IOException e) {
                System.err.println("[Server] Error closing socket: " + e.getMessage());
            }
        }
    }
}
