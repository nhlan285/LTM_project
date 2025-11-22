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
        background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
        min-height: 100vh;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 20px;
        position: relative;
        overflow-y: auto;
      }

      body::before {
        content: "";
        position: absolute;
        width: 200%;
        height: 200%;
        background: radial-gradient(
          circle,
          rgba(255, 255, 255, 0.1) 1px,
          transparent 1px
        );
        background-size: 50px 50px;
        animation: drift 20s linear infinite;
        pointer-events: none;
      }

      @keyframes drift {
        from {
          transform: translate(0, 0);
        }
        to {
          transform: translate(-50px, -50px);
        }
      }

      .container {
        background: rgba(255, 255, 255, 0.98);
        border-radius: 20px;
        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3),
          0 0 0 1px rgba(255, 255, 255, 0.3);
        padding: 45px;
        max-width: 550px;
        width: 100%;
        position: relative;
        backdrop-filter: blur(10px);
        animation: slideUp 0.5s ease-out;
      }

      @keyframes slideUp {
        from {
          opacity: 0;
          transform: translateY(30px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      h1 {
        color: #2d3748;
        text-align: center;
        margin-bottom: 10px;
        font-size: 32px;
        font-weight: 700;
        letter-spacing: -0.5px;
        background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }

      .subtitle {
        color: #666;
        text-align: center;
        margin-bottom: 30px;
        font-size: 14px;
      }

      .upload-area {
        border: 3px dashed #667eea;
        border-radius: 16px;
        padding: 50px 20px;
        text-align: center;
        background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
        cursor: pointer;
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        margin-bottom: 25px;
        position: relative;
        overflow: hidden;
      }

      .upload-area::before {
        content: "";
        position: absolute;
        top: -50%;
        left: -50%;
        width: 200%;
        height: 200%;
        background: radial-gradient(
          circle,
          rgba(102, 126, 234, 0.1) 0%,
          transparent 70%
        );
        transform: scale(0);
        transition: transform 0.6s;
      }

      .upload-area:hover::before {
        transform: scale(1);
      }

      .upload-area:hover {
        border-color: #764ba2;
        background: linear-gradient(135deg, #f0f2ff 0%, #e8ebff 100%);
        transform: translateY(-2px);
        box-shadow: 0 10px 25px rgba(102, 126, 234, 0.2);
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
        background: linear-gradient(135deg, #cffafe 0%, #a5f3fc 100%);
        border-radius: 12px;
        padding: 18px;
        margin-top: 20px;
        display: none;
        border-left: 4px solid #06b6d4;
        animation: fadeIn 0.3s ease-out;
        max-height: 300px;
        overflow-y: auto;
      }

      .file-info::-webkit-scrollbar {
        width: 8px;
      }

      .file-info::-webkit-scrollbar-track {
        background: rgba(6, 182, 212, 0.1);
        border-radius: 4px;
      }

      .file-info::-webkit-scrollbar-thumb {
        background: #06b6d4;
        border-radius: 4px;
      }

      .file-info::-webkit-scrollbar-thumb:hover {
        background: #0891b2;
      }

      .file-info ul {
        list-style: none;
        padding: 0;
        margin: 8px 0 0 0;
      }

      .file-info li {
        padding: 10px 15px;
        background: rgba(255, 255, 255, 0.7);
        border-radius: 8px;
        margin-bottom: 8px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 10px;
        font-size: 14px;
        color: #0e7490;
        font-weight: 500;
        transition: all 0.2s;
      }

      .file-info li:hover {
        background: rgba(255, 255, 255, 0.9);
        transform: translateX(3px);
      }

      .file-name-display {
        display: flex;
        align-items: center;
        gap: 8px;
        flex: 1;
        overflow: hidden;
      }

      .file-name-display::before {
        content: "📄";
        font-size: 18px;
        flex-shrink: 0;
      }

      .file-name-text {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        color: #0e7490;
        font-weight: 500;
        font-size: 14px;
      }

      .remove-file-btn {
        background: #ef4444;
        color: white;
        border: none;
        border-radius: 50%;
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        font-size: 14px;
        flex-shrink: 0;
        transition: all 0.2s;
      }

      .remove-file-btn:hover {
        background: #dc2626;
        transform: scale(1.1);
      }

      @keyframes fadeIn {
        from {
          opacity: 0;
          transform: translateY(-10px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      .file-name {
        color: #333;
        font-weight: bold;
        word-break: break-all;
      }

      .btn {
        background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
        color: white;
        border: none;
        padding: 16px 32px;
        border-radius: 12px;
        font-size: 17px;
        font-weight: 600;
        cursor: pointer;
        width: 100%;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        overflow: hidden;
        letter-spacing: 0.3px;
        box-shadow: 0 4px 14px rgba(6, 182, 212, 0.3);
      }

      .btn::before {
        content: "";
        position: absolute;
        top: 50%;
        left: 50%;
        width: 0;
        height: 0;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        transform: translate(-50%, -50%);
        transition: width 0.6s, height 0.6s;
      }

      .btn:hover::before {
        width: 300px;
        height: 300px;
      }

      .btn:hover {
        transform: translateY(-3px);
        box-shadow: 0 10px 30px rgba(6, 182, 212, 0.5);
      }

      .btn:active {
        transform: translateY(-1px);
      }

      .btn:disabled {
        background: linear-gradient(135deg, #cbd5e0 0%, #a0aec0 100%);
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
          <div class="upload-text">Click to select DOCX files</div>
          <div style="color: #999; font-size: 12px">or drag and drop here</div>
        </div>

        <input
          type="file"
          id="fileInput"
          name="files"
          accept=".docx"
          multiple
          required
        />

        <div class="file-info" id="fileInfo">
          <div>Selected files:</div>
          <ul id="fileList" style="margin-top: 10px; padding-left: 18px"></ul>
        </div>

        <button type="submit" class="btn" id="submitBtn" disabled>
          🚀 Upload & Convert (0)
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
      const fileList = document.getElementById("fileList");
      const submitBtn = document.getElementById("submitBtn");
      const uploadArea = document.querySelector(".upload-area");

      // Handle file selection
      fileInput.addEventListener("change", function (e) {
        renderSelection(e.target.files);
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

      function renderSelection(files) {
    	  if (!files || files.length === 0) {
    	    fileInfo.style.display = "none";
    	    fileList.innerHTML = "";
    	    submitBtn.disabled = true;
    	    submitBtn.textContent = "🚀 Upload & Convert (0)";
    	    return;
    	  }

    	  const items = [];
    	  // JSP sẽ bỏ qua các đoạn có dấu \ trước $
    	  for (let i = 0; i < files.length; i++) {
    	    const file = files[i];
    	    if (!file.name.toLowerCase().endsWith(".docx")) {
    	      alert("File " + file.name + " không đúng định dạng DOCX!");
    	      fileInput.value = "";
    	      fileList.innerHTML = "";
    	      submitBtn.disabled = true;
    	      submitBtn.textContent = "🚀 Upload & Convert (0)";
    	      return;
    	    }
    	    items.push(`
    	      <li>
    	        <div class="file-name-display">
    	          <span class="file-name-text" title="\${file.name}">\${file.name}</span>
    	        </div>
    	        <button type="button" class="remove-file-btn" onclick="removeFile(\${i})" title="Xóa file">×</button>
    	      </li>
    	    `);
    	  }

    	  fileInfo.style.display = "block";
    	  fileList.innerHTML = items.join("");
    	  submitBtn.disabled = false;
    	  
    	  // Sửa: Thêm \ trước files.length để hiển thị đúng số lượng
    	  submitBtn.textContent = `🚀 Upload & Convert (\${files.length})`;
    	}

      function removeFile(index) {
        const dt = new DataTransfer();
        const filesArray = Array.from(fileInput.files);
        filesArray.forEach((file, i) => {
          if (i !== index) dt.items.add(file);
        });
        fileInput.files = dt.files;
        renderSelection(fileInput.files);
      }
    </script>
  </body>
</html>
