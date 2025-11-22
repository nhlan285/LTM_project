# 🎓 Distributed File Converter System

### Network Programming - Final Project

---

## 📋 Project Overview

A **Distributed File Converter System** that converts DOCX files to PDF using advanced Network Programming concepts:

✅ **TCP Sockets** - Communication between Web Server and Conversion Server  
✅ **Multithreading** - Worker threads process conversions in parallel  
✅ **Producer-Consumer Pattern** - Using `BlockingQueue` for task management  
✅ **Database Integration** - MySQL for task tracking  
✅ **Web Application** - JSP/Servlet interface with AJAX polling

---

## 🏗️ System Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                        SYSTEM ARCHITECTURE                     │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  [User Browser]                                                │
│       │                                                        │
│       │ HTTP Upload                                            │
│       ▼                                                        │
│  ┌─────────────────────────┐                                   │
│  │   MODULE A (Web Server) │                                   │
│  │   - JSP/Servlet/Tomcat  │                                   │
│  │   - Handles file upload │                                   │
│  │   - Saves to disk       │                                   │
│  │   - Inserts to DB       │                                   │
│  └───────────┬─────────────┘                                   │
│              │                                                 │
│              │ TCP Socket (Port 9999)                          │
│              │ Protocol: "taskId|filePath"                     │
│              ▼                                                 │
│  ┌─────────────────────────────────────────────────┐           │
│  │   MODULE B (Conversion Server)                  │           │
│  │   ┌──────────────────────────────────────────┐  │           │
│  │   │  ServerSocket (Port 9999)                │  │           │
│  │   │  - Accepts connections                   │  │           │
│  │   │  - Adds tasks to BlockingQueue           │  │           │
│  │   └────────────┬─────────────────────────────┘  │           │
│  │                │                                │           │
│  │                ▼                                │           │
│  │   ┌─────────────────────────┐                   │           │
│  │   │   BlockingQueue<Task>   │                   │           │
│  │   └──────────┬──────────────┘                   │           │
│  │              │                                  │           │
│  │      ┌───────┼────────┐                         │           │
│  │      ▼       ▼        ▼                         │           │
│  │  ┌────────┬────────┬────────┐                   │           │
│  │  │Worker 1│Worker 2│Worker 3│ (ThreadPool)      │           │
│  │  │  use   │  use   │   use  |                   │           │
│  │  │Word to │ Word to│ Word to│                   │           │
│  │  │convert │ convert│ convert│                   │           │
│  │  └───┬────┴────┬───┴────┬───┘                   │           │
│  │      │         │        │                       │           │
│  │      └─────────┼────────┘                       │           │
│  │                ▼                                │           │
│  │         Updates Database                        │           │
│  └─────────────────────────────────────────────────┘           │
│                    │                                           │
│                    ▼                                           │
│           ┌─────────────────┐                                  │
│           │  MySQL Database │                                  │
│           │  - tasks table  │                                  │
│           └─────────────────┘                                  │
│                    ▲                                           │
│                    │ AJAX Polling (Every 2s)                   │
│                    │                                           │
│              [User Browser]                                    │
│               Status Page                                      │
└────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Setup Instructions

### 1️⃣ Prerequisites

- **JDK 11+** (Java Development Kit)
- **MySQL 8.0+** (Database Server)
- **Apache Tomcat 9+** (Web Server)
- **Maven 3.6+** (Build Tool)

### 2️⃣ Database Setup

Run the SQL script to create the database:

```bash
mysql -u root -p < database.sql
```

Or manually:

```sql
CREATE DATABASE file_converter_db;
USE file_converter_db;

CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    original_filename VARCHAR(255),
    file_path_input VARCHAR(500),
    file_path_output VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3️⃣ Update Database Configuration

Edit `src/main/resources/database.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/file_converter_db
db.user=root
db.password=YOUR_MYSQL_PASSWORD
```

### 4️⃣ Build the Project

```bash
mvn clean package
```

This will create `file-converter.war` in the `target` folder.

### 5️⃣ Deploy to Tomcat

1. Copy `target/file-converter.war` to Tomcat's `webapps` folder
2. Start Tomcat:

   ```bash
   # Windows
   catalina.bat run

   # Linux/Mac
   ./catalina.sh run
   ```

### 6️⃣ Start the Conversion Server (Module B)

**Important:** This must be running BEFORE uploading files!

```bash
# Option 1: Using Maven
mvn exec:java -Dexec.mainClass="com.server.core.ServerMain"

# Option 2: Using compiled JAR
java -cp target/classes;target/dependency/* com.server.core.ServerMain
```

You should see:

```
═══════════════════════════════════════════════════
  FILE CONVERSION SERVER (MODULE B) - STARTING
═══════════════════════════════════════════════════
[Server] BlockingQueue initialized
[Server] ThreadPool created with 3 workers
[Worker-1] Started and waiting for tasks...
[Worker-2] Started and waiting for tasks...
[Worker-3] Started and waiting for tasks...
[Server] ✅ Server started on port 9999
```

### 7️⃣ Access the Application

Open your browser and go to:

```
http://localhost:8080/file-converter/
```

---

## 📁 Project Structure

```
LTM/
├── pom.xml                                    # Maven configuration
├── database.sql                               # Database schema
├── README.md                                  # This file
│
├── src/main/
│   ├── java/com/
│   │   ├── common/
│   │   │   └── DBContext.java                 # Database connection utility
│   │   │
│   │   ├── server/                            # MODULE B (Conversion Server)
│   │   │   ├── core/
│   │   │   │   ├── ServerMain.java            # TCP Server + ThreadPool
│   │   │   │   └── WorkerTask.java            # Consumer thread
│   │   │   ├── converter/
│   │   │   │   └── DocxToPdfService.java      # DOCX→PDF conversion
│   │   │   └── model/
│   │   │       └── TaskRequest.java           # DTO for queue
│   │   │
│   │   └── mvc/                               # MODULE A (Web Server)
│   │       └── controller/
│   │           ├── UploadServlet.java         # File upload handler
│   │           └── StatusServlet.java         # JSON API for status
│   │
│   ├── resources/
│   │   └── database.properties                # DB configuration
│   │
│   └── webapp/
│       ├── WEB-INF/
│       │   └── web.xml                        # Web app configuration
│       ├── index.jsp                          # Upload page
│       ├── batch-status.jsp                   # Status monitoring for batch view
│       ├── status.jsp                         # Status monitoring page
│       └── error.jsp                          # Error page
```

---

## 🔬 Key Technical Concepts (For Explanation to Professor)

### 1. **TCP Socket Communication**

**Location:** `UploadServlet.java` (Client) ↔ `ServerMain.java` (Server)

```java
// CLIENT SIDE (UploadServlet.java)
Socket socket = new Socket("localhost", 9999);
PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
writer.println(taskId + "|" + filePath);
socket.close(); // Fire and forget

// SERVER SIDE (ServerMain.java)
ServerSocket serverSocket = new ServerSocket(9999);
while (true) {
    Socket client = serverSocket.accept();
    handleClientRequest(client);
}
```

**Explanation:** Web server sends conversion requests via TCP. Server accepts connections without blocking the client.

---

### 2. **Producer-Consumer Pattern**

**Location:** `ServerMain.java` + `WorkerTask.java`

```java
// SHARED QUEUE
BlockingQueue<TaskRequest> taskQueue = new LinkedBlockingQueue<>();

// PRODUCER (ServerMain - accepts connections)
taskQueue.put(task); // Non-blocking if queue is unbounded

// CONSUMER (WorkerTask - processes tasks)
TaskRequest task = taskQueue.take(); // BLOCKS if queue is empty
processTask(task);
```

**Explanation:**

- **Producer:** Main thread accepts socket connections and adds tasks to queue
- **Consumer:** 3 worker threads continuously take tasks and process them
- **BlockingQueue:** Thread-safe queue that handles synchronization automatically

---

### 3. **Thread Pool Management**

**Location:** `ServerMain.java`

```java
ExecutorService workerPool = Executors.newFixedThreadPool(3);
for (int i = 1; i <= 3; i++) {
    workerPool.submit(new WorkerTask(taskQueue, i));
}
```

**Explanation:**

- Creates exactly 3 threads (prevents resource exhaustion)
- Threads are reused (efficient for multiple requests)
- Each thread runs in infinite loop, waiting for tasks

---

### 4. **Asynchronous Status Checking**

**Location:** `status.jsp` (AJAX) ↔ `StatusServlet.java` (API)

```javascript
// AJAX Polling (Every 2 seconds)
function pollStatus() {
  fetch("api/status?taskId=" + taskId)
    .then((response) => response.json())
    .then((data) => {
      updateUI(data);
      if (data.status !== "COMPLETED" && data.status !== "FAILED") {
        setTimeout(pollStatus, 2000);
      }
    });
}
```

**Explanation:** Client doesn't wait for conversion to finish. Instead, it periodically checks the database for status updates.

---

## 🎯 Testing the System

### Test Case 1: Single File Upload

1. Go to `http://localhost:8080/file-converter/`
2. Upload a `.docx` file
3. You should see:
   - Status: **PENDING** → **PROCESSING** → **COMPLETED**
   - Download button appears when done

### Test Case 2: Multiple Concurrent Uploads

1. Open 5 browser tabs
2. Upload 5 different `.docx` files simultaneously
3. Observe in Server console:
   - All 5 tasks are added to queue
   - 3 workers process them in parallel
   - Remaining 2 wait in queue

**Expected Output (Server Console):**

```
[Server] 📩 Connection received from: 127.0.0.1
[Server] Task 1 added to queue (Queue size: 1)
[Worker-1] Picked up task: TaskRequest{taskId=1, ...}
[Server] 📩 Connection received from: 127.0.0.1
[Server] Task 2 added to queue (Queue size: 1)
[Worker-2] Picked up task: TaskRequest{taskId=2, ...}
...
[Worker-1] ✅ Task 1 completed successfully!
```

### Test Case 3: Server Not Running

1. Stop the Conversion Server
2. Try uploading a file
3. You should see error: "Conversion server is not available!"

---

## 📊 Database Queries for Monitoring

```sql
-- View all tasks
SELECT id, original_filename, status, created_at
FROM tasks
ORDER BY created_at DESC;

-- Count tasks by status
SELECT status, COUNT(*) as count
FROM tasks
GROUP BY status;

-- Find failed tasks
SELECT id, original_filename, error_message
FROM tasks
WHERE status = 'FAILED';
```

---

## 🛠️ Troubleshooting

### Problem: "Cannot connect to database"

**Solution:**

- Check MySQL is running: `systemctl status mysql` (Linux) or Services (Windows)
- Verify credentials in `database.properties`
- Test connection: Run `DBContext.main()` in IDE

### Problem: "Port 9999 already in use"

**Solution:**

- Check if another process is using port: `netstat -ano | findstr :9999` (Windows)
- Kill the process or change port in both `ServerMain.java` and `UploadServlet.java`

### Problem: "File upload but status stays PENDING"

**Solution:**

- Ensure Conversion Server (Module B) is running
- Check server console for errors
- Verify file path in database: `SELECT file_path_input FROM tasks WHERE id = X;`

---

## 📚 Technologies Used

| Technology          | Purpose                           |
| ------------------- | --------------------------------- |
| **Java 11**         | Core programming language         |
| **JSP/Servlet**     | Web application (Module A)        |
| **Apache Tomcat**   | Servlet container                 |
| **MySQL**           | Database for task tracking        |
| **MS WORD**         | Read & Convert DOCX to PDF        |
| **TCP Sockets**     | Inter-process communication       |
| **ExecutorService** | Thread pool management            |
| **BlockingQueue**   | Producer-consumer synchronization |
| **AJAX**            | Asynchronous status updates       |

---

## 👨‍💻 Author

**Nguyễn Nhật Dũng Lân**  
**Phan Thanh Trường**  
**Lâm Trung Hiếu**  

**Course:** Network Programming  
**Project:** Distributed File Converter System

---

## 🙏 Acknowledgments

- Professor: Pham Minh Tuan - Network Programming Course
- Java Concurrency in Practice

---
