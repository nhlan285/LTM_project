<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Conversion Status</title>
    <style>
      * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
      }

      body {
        font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        min-height: 100vh;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 20px;
      }

      .container {
        background: white;
        border-radius: 15px;
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
        padding: 40px;
        max-width: 600px;
        width: 100%;
      }

      h1 {
        color: #333;
        text-align: center;
        margin-bottom: 30px;
        font-size: 28px;
      }

      .status-card {
        background: #f8f9ff;
        border-radius: 10px;
        padding: 30px;
        text-align: center;
        margin-bottom: 20px;
      }

      .status-icon {
        font-size: 60px;
        margin-bottom: 20px;
      }

      .status-text {
        font-size: 18px;
        color: #333;
        margin-bottom: 10px;
        font-weight: bold;
      }

      .status-message {
        color: #666;
        font-size: 14px;
        margin-bottom: 20px;
      }

      .task-id {
        background: #e8f4f8;
        padding: 10px;
        border-radius: 5px;
        font-family: "Courier New", monospace;
        color: #555;
        margin-top: 15px;
      }

      /* Loading animation */
      .loader {
        border: 5px solid #f3f3f3;
        border-top: 5px solid #667eea;
        border-radius: 50%;
        width: 50px;
        height: 50px;
        animation: spin 1s linear infinite;
        margin: 20px auto;
      }

      @keyframes spin {
        0% {
          transform: rotate(0deg);
        }
        100% {
          transform: rotate(360deg);
        }
      }

      .btn {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        border: none;
        padding: 15px 30px;
        border-radius: 8px;
        font-size: 16px;
        font-weight: bold;
        cursor: pointer;
        text-decoration: none;
        display: inline-block;
        transition: transform 0.2s;
        margin: 5px;
      }

      .btn:hover {
        transform: translateY(-2px);
        box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
      }

      .btn-success {
        background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
      }

      .btn-secondary {
        background: linear-gradient(135deg, #9e9e9e 0%, #757575 100%);
      }

      .hidden {
        display: none;
      }

      .progress-bar {
        width: 100%;
        height: 8px;
        background: #e0e0e0;
        border-radius: 10px;
        overflow: hidden;
        margin-top: 15px;
      }

      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, #667eea, #764ba2);
        width: 0%;
        transition: width 0.5s;
      }

      .details {
        background: #fff3cd;
        border-left: 4px solid #ffc107;
        padding: 15px;
        border-radius: 5px;
        margin-top: 20px;
        font-size: 14px;
        text-align: left;
      }

      .details strong {
        color: #856404;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <h1>📊 Conversion Status</h1>

      <div class="status-card">
        <div class="status-icon" id="statusIcon">⏳</div>
        <div class="status-text" id="statusText">Checking status...</div>
        <div class="status-message" id="statusMessage">
          Please wait while we check your file conversion status
        </div>

        <div class="loader" id="loader"></div>

        <div class="progress-bar" id="progressBar" style="display: none">
          <div class="progress-fill" id="progressFill"></div>
        </div>

        <div class="task-id">
          Task ID:
          <strong id="taskIdDisplay"
            ><%= request.getParameter("taskId") %></strong
          >
        </div>
      </div>

      <div style="text-align: center">
        <a href="index.jsp" class="btn btn-secondary">⬅️ Upload Another</a>
        <a href="#" class="btn btn-success hidden" id="downloadBtn"
          >📥 Download PDF</a
        >
      </div>

      <div class="details">
        <strong>💡 Technical Details:</strong><br />
        This page uses AJAX Polling to check conversion status every 2
        seconds.<br />
        The conversion is processed by a separate server using:<br />
        • TCP Socket Communication<br />
        • Producer-Consumer Pattern<br />
        • ThreadPool (3 worker threads)
      </div>
    </div>

    <script>
      // Get task ID from URL parameter
      const urlParams = new URLSearchParams(window.location.search);
      const taskId = urlParams.get("taskId");

      // UI Elements
      const statusIcon = document.getElementById("statusIcon");
      const statusText = document.getElementById("statusText");
      const statusMessage = document.getElementById("statusMessage");
      const loader = document.getElementById("loader");
      const downloadBtn = document.getElementById("downloadBtn");
      const progressBar = document.getElementById("progressBar");
      const progressFill = document.getElementById("progressFill");

      // Status icons mapping
      const statusIcons = {
        PENDING: "⏳",
        PROCESSING: "⚙️",
        COMPLETED: "✅",
        FAILED: "❌",
      };

      // Check if taskId exists
      if (!taskId) {
        statusIcon.textContent = "❌";
        statusText.textContent = "Error";
        statusMessage.textContent = "No task ID provided!";
        loader.style.display = "none";
      } else {
        // Start polling
        pollStatus();
      }

      /**
       * Poll the server for status updates
       * NETWORKING CONCEPT: AJAX Polling (Alternative to WebSocket)
       */
      function pollStatus() {
        fetch("api/status?taskId=" + taskId)
          .then((response) => response.json())
          .then((data) => {
            if (data.success) {
              updateUI(data);

              // Continue polling if not finished
              if (data.status === "PENDING" || data.status === "PROCESSING") {
                setTimeout(pollStatus, 2000); // Poll every 2 seconds
              } else {
                loader.style.display = "none";
              }
            } else {
              showError(data.message);
            }
          })
          .catch((error) => {
            console.error("Error:", error);
            showError("Failed to connect to server");
          });
      }

      /**
       * Update UI based on status
       */
      function updateUI(data) {
        const status = data.status;

        // Update icon
        statusIcon.textContent = statusIcons[status] || "❓";

        // Update text
        statusText.textContent = status;

        // Update message
        statusMessage.textContent = data.message;

        // Update progress bar
        progressBar.style.display = "block";
        let progress = 0;

        switch (status) {
          case "PENDING":
            progress = 25;
            break;
          case "PROCESSING":
            progress = 50;
            break;
          case "COMPLETED":
            progress = 100;
            loader.style.display = "none";

            // Show download button
            if (data.downloadUrl) {
              downloadBtn.href = data.downloadUrl;
              downloadBtn.classList.remove("hidden");
            }
            break;
          case "FAILED":
            progress = 100;
            progressFill.style.background = "#f44336";
            loader.style.display = "none";
            break;
        }

        progressFill.style.width = progress + "%";
      }

      /**
       * Show error message
       */
      function showError(message) {
        statusIcon.textContent = "❌";
        statusText.textContent = "Error";
        statusMessage.textContent = message;
        loader.style.display = "none";
      }
    </script>
  </body>
</html>
