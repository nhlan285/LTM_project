<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>DOCX to PDF Converter - Upload</title>
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
        max-width: 500px;
        width: 100%;
      }

      h1 {
        color: #333;
        text-align: center;
        margin-bottom: 10px;
        font-size: 28px;
      }

      .subtitle {
        color: #666;
        text-align: center;
        margin-bottom: 30px;
        font-size: 14px;
      }

      .upload-area {
        border: 2px dashed #667eea;
        border-radius: 10px;
        padding: 40px 20px;
        text-align: center;
        background: #f8f9ff;
        cursor: pointer;
        transition: all 0.3s;
        margin-bottom: 20px;
      }

      .upload-area:hover {
        border-color: #764ba2;
        background: #f0f2ff;
      }

      .upload-icon {
        font-size: 50px;
        color: #667eea;
        margin-bottom: 15px;
      }

      .upload-text {
        color: #666;
        margin-bottom: 10px;
      }

      input[type="file"] {
        display: none;
      }

      .file-info {
        background: #e8f4f8;
        border-radius: 8px;
        padding: 15px;
        margin-top: 15px;
        display: none;
      }

      .file-name {
        color: #333;
        font-weight: bold;
        word-break: break-all;
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
        width: 100%;
        transition: transform 0.2s;
      }

      .btn:hover {
        transform: translateY(-2px);
        box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
      }

      .btn:disabled {
        background: #ccc;
        cursor: not-allowed;
        transform: none;
      }

      .error {
        background: #ffe6e6;
        color: #d32f2f;
        padding: 15px;
        border-radius: 8px;
        margin-bottom: 20px;
        border-left: 4px solid #d32f2f;
      }

      .info-box {
        background: #e3f2fd;
        border-left: 4px solid #2196f3;
        padding: 15px;
        border-radius: 5px;
        margin-top: 20px;
        font-size: 14px;
        color: #1565c0;
      }

      .info-box ul {
        margin-left: 20px;
        margin-top: 10px;
      }

      .info-box li {
        margin-bottom: 5px;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <h1>📄 DOCX to PDF Converter</h1>
      <p class="subtitle">Network Programming - Final Project</p>

      <% if (request.getAttribute("error") != null) { %>
      <div class="error">❌ <%= request.getAttribute("error") %></div>
      <% } %>

      <form
        id="uploadForm"
        action="upload"
        method="post"
        enctype="multipart/form-data"
      >
        <div
          class="upload-area"
          onclick="document.getElementById('fileInput').click()"
        >
          <div class="upload-icon">📁</div>
          <div class="upload-text">Click to select a DOCX file</div>
          <div style="color: #999; font-size: 12px">or drag and drop here</div>
        </div>

        <input type="file" id="fileInput" name="file" accept=".docx" required />

        <div class="file-info" id="fileInfo">
          <div>Selected file:</div>
          <div class="file-name" id="fileName"></div>
        </div>

        <button type="submit" class="btn" id="submitBtn" disabled>
          🚀 Upload & Convert
        </button>
      </form>

      <div class="info-box">
        <strong>ℹ️ How it works:</strong>
        <ul>
          <li>Upload your DOCX file</li>
          <li>File is sent to conversion server</li>
          <li>Server processes using multi-threading</li>
          <li>Download your PDF when ready</li>
        </ul>
      </div>
    </div>

    <script>
      const fileInput = document.getElementById("fileInput");
      const fileInfo = document.getElementById("fileInfo");
      const fileName = document.getElementById("fileName");
      const submitBtn = document.getElementById("submitBtn");
      const uploadArea = document.querySelector(".upload-area");

      // Handle file selection
      fileInput.addEventListener("change", function (e) {
        const file = e.target.files[0];
        if (file) {
          fileName.textContent = file.name;
          fileInfo.style.display = "block";
          submitBtn.disabled = false;
        }
      });

      // Handle drag and drop
      uploadArea.addEventListener("dragover", function (e) {
        e.preventDefault();
        uploadArea.style.borderColor = "#764ba2";
        uploadArea.style.background = "#f0f2ff";
      });

      uploadArea.addEventListener("dragleave", function (e) {
        uploadArea.style.borderColor = "#667eea";
        uploadArea.style.background = "#f8f9ff";
      });

      uploadArea.addEventListener("drop", function (e) {
        e.preventDefault();
        uploadArea.style.borderColor = "#667eea";
        uploadArea.style.background = "#f8f9ff";

        const files = e.dataTransfer.files;
        if (files.length > 0) {
          fileInput.files = files;
          const event = new Event("change");
          fileInput.dispatchEvent(event);
        }
      });

      // Prevent default form submit to show loading
      document
        .getElementById("uploadForm")
        .addEventListener("submit", function (e) {
          submitBtn.textContent = "⏳ Uploading...";
          submitBtn.disabled = true;
        });
    </script>
  </body>
</html>
