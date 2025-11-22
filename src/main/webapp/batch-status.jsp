<%@ page contentType="text/html;charset=UTF-8" language="java"
isELIgnored="true" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>Batch Status</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <style>
      body {
        font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
        background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
        margin: 0;
        padding: 20px;
        min-height: 100vh;
        position: relative;
        overflow-x: hidden;
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

      .wrapper {
        max-width: 1100px;
        margin: 0 auto;
        background: rgba(255, 255, 255, 0.98);
        border-radius: 20px;
        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3),
          0 0 0 1px rgba(255, 255, 255, 0.3);
        padding: 35px;
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
        margin: 0 0 10px;
        color: #2d3748;
        font-size: 32px;
        font-weight: 700;
        letter-spacing: -0.5px;
        background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }

      .summary {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        margin-bottom: 20px;
      }

      .summary div {
        flex: 1;
        min-width: 180px;
        padding: 20px;
        border-radius: 14px;
        background: linear-gradient(135deg, #ecfeff 0%, #cffafe 100%);
        text-align: center;
        border: 1px solid rgba(6, 182, 212, 0.2);
        transition: all 0.3s ease;
        box-shadow: 0 2px 8px rgba(6, 182, 212, 0.15);
      }

      .summary div:hover {
        transform: translateY(-3px);
        box-shadow: 0 8px 24px rgba(6, 182, 212, 0.3);
        border-color: rgba(6, 182, 212, 0.4);
      }

      .status-table {
        width: 100%;
        border-collapse: collapse;
      }

      .status-table th,
      .status-table td {
        padding: 16px;
        border-bottom: 1px solid rgba(6, 182, 212, 0.1);
      }

      .status-table th {
        background: linear-gradient(135deg, #ecfeff 0%, #cffafe 100%);
        font-weight: 600;
        color: #0e7490;
        text-transform: uppercase;
        font-size: 12px;
        letter-spacing: 0.5px;
      }

      .badge {
        padding: 6px 14px;
        border-radius: 999px;
        font-size: 11px;
        text-transform: uppercase;
        font-weight: 600;
        letter-spacing: 0.5px;
        display: inline-block;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
      }

      .badge.PENDING {
        background: #fff3cd;
        color: #856404;
      }

      .badge.PROCESSING {
        background: #e3f2fd;
        color: #1565c0;
      }

      .badge.COMPLETED {
        background: #c8e6c9;
        color: #1b5e20;
      }

      .badge.FAILED {
        background: #ffcdd2;
        color: #b71c1c;
      }

      .actions button,
      .actions a {
        background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
        color: #fff;
        border: none;
        padding: 14px 28px;
        border-radius: 10px;
        cursor: pointer;
        text-decoration: none;
        margin-right: 12px;
        margin-bottom: 0;
        font-weight: 600;
        font-size: 15px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        display: inline-flex;
        align-items: center;
        gap: 8px;
        position: relative;
        overflow: hidden;
        box-shadow: 0 4px 12px rgba(6, 182, 212, 0.3);
        height: 48px;
      }

      .actions button::before,
      .actions a::before {
        content: "";
        position: absolute;
        top: 50%;
        left: 50%;
        width: 0;
        height: 0;
        background: rgba(255, 255, 255, 0.25);
        border-radius: 50%;
        transform: translate(-50%, -50 ());
        transition: width 0.5s, height 0.5s;
      }

      .actions button:hover::before,
      .actions a:hover::before {
        width: 200px;
        height: 200px;
      }

      .actions button:hover,
      .actions a:hover {
        transform: translateY(-3px);
        box-shadow: 0 8px 20px rgba(6, 182, 212, 0.5);
      }

      .action-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        border-radius: 8px;
        font-size: 13px;
        font-weight: 600;
        text-decoration: none;
        transition: all 0.3s;
        border: none;
        cursor: pointer;
        margin-right: 6px;
      }

      .action-btn-download {
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
      }

      .action-btn-download:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(16, 185, 129, 0.5);
      }

      .action-btn-preview {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
      }

      .action-btn-preview:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(245, 158, 11, 0.5);
      }

      .action-btn-detail {
        background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(6, 182, 212, 0.3);
      }

      .action-btn-detail:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(6, 182, 212, 0.5);
      }

      #downloadAll {
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
      }

      #downloadAll:hover:not(:disabled) {
        box-shadow: 0 8px 20px rgba(16, 185, 129, 0.5);
      }

      .actions button:disabled {
        opacity: 0.4;
        cursor: not-allowed;
      }

      .preview-frame {
        width: 100%;
        height: 600px;
        border: none;
      }

      .modal {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.6);
        display: none;
        align-items: center;
        justify-content: center;
      }

      .modal-content {
        background: #fff;
        width: 80%;
        max-width: 900px;
        border-radius: 12px;
        padding: 20px;
        position: relative;
      }

      .close-btn {
        position: absolute;
        top: 15px;
        right: 15px;
        border: none;
        background: transparent;
        font-size: 22px;
        cursor: pointer;
      }

      .table-row {
        cursor: pointer;
      }

      .table-row:hover {
        background: linear-gradient(135deg, #ecfeff 0%, #cffafe 100%);
        transform: translateX(4px);
      }

      .error-note {
        color: #b71c1c;
        font-size: 12px;
      }
    </style>
  </head>
  <body>
    <div class="wrapper">
      <h1>📁 Danh sách file vừa upload</h1>
      <p>Batch ID: <strong id="batchLabel"></strong></p>

      <div class="summary">
        <div>
          <div>Tổng số file</div>
          <div id="totalCount" style="font-size: 24px; font-weight: bold">
            0
          </div>
        </div>
        <div>
          <div>Đã hoàn thành</div>
          <div id="completedCount" style="font-size: 24px; font-weight: bold">
            0
          </div>
        </div>
        <div>
          <div>Trạng thái chung</div>
          <div id="batchStatus" class="badge PENDING">PENDING</div>
        </div>
      </div>

      <div class="actions" style="margin-bottom: 15px">
        <a href="index.jsp">⬅️ Upload thêm</a>
        <button id="downloadAll" disabled>Tải tất cả PDF</button>
      </div>

      <table class="status-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Tên file</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody id="tableBody"></tbody>
      </table>
    </div>

    <div class="modal" id="previewModal">
      <div class="modal-content">
        <button class="close-btn" onclick="togglePreview(false)">✖</button>
        <iframe id="previewFrame" class="preview-frame"></iframe>
      </div>
    </div>

    <script>
      const urlParams = new URLSearchParams(window.location.search);
      const batchId = urlParams.get("batchId");
      const tableBody = document.getElementById("tableBody");
      const batchLabel = document.getElementById("batchLabel");
      const totalCount = document.getElementById("totalCount");
      const completedCount = document.getElementById("completedCount");
      const batchStatus = document.getElementById("batchStatus");
      const downloadAllBtn = document.getElementById("downloadAll");
      const previewModal = document.getElementById("previewModal");
      const previewFrame = document.getElementById("previewFrame");

      if (!batchId) {
        alert("Thiếu batchId!");
      } else {
        batchLabel.textContent = batchId;
        poll();
      }

      downloadAllBtn.addEventListener("click", () => {
        window.location.href = `download-all?batchId=${batchId}`;
      });

      function poll() {
        fetch(`api/batch-status?batchId=${batchId}`)
          .then((res) => res.json())
          .then((data) => {
            if (!data.success) {
              alert(data.message || "Không thể tải trạng thái batch");
              return;
            }
            render(data);
            setTimeout(poll, 3000);
          })
          .catch((err) => {
            console.error(err);
            setTimeout(poll, 3000);
          });
      }

      function render(data) {
        const { batch } = data;
        const tasks = data.tasks || [];
        totalCount.textContent = batch.total;
        completedCount.textContent = batch.completed;
        batchStatus.textContent = batch.status;
        batchStatus.className = `badge ${batch.status}`;
        downloadAllBtn.disabled = tasks.every((t) => !t.downloadUrl);

        tableBody.innerHTML = tasks
          .map((task, index) => {
            const buttons = [];
            if (task.downloadUrl) {
              buttons.push(
                `<a href="${task.downloadUrl}" target="_blank" class="action-btn action-btn-download" onclick="event.stopPropagation();"><span>⬇️</span> Tải về</a>`
              );
              buttons.push(
                `<button class="action-btn action-btn-preview" onclick="event.stopPropagation(); openPreview('${task.previewUrl}'); return false;"><span>👁️</span> Xem</button>`
              );
            }
            buttons.push(
              `<a href="status.jsp?taskId=${task.id}&batchId=${batch.id}" class="action-btn action-btn-detail" onclick="event.stopPropagation();"><span>ℹ️</span> Chi tiết</a>`
            );

            const error = task.error
              ? `<div class="error-note">${task.error}</div>`
              : "";

            return `<tr class="table-row" onclick="gotoDetail(${task.id})">
              <td>${index + 1}</td>
              <td>${task.displayName || "File"}</td>
              <td><span class="badge ${task.status}">${
              task.status
            }</span>${error}</td>
              <td>${buttons.join(" ")}</td>
            </tr>`;
          })
          .join("");
      }

      function gotoDetail(taskId) {
        window.location.href = `status.jsp?taskId=${taskId}&batchId=${batchId}`;
      }

      function openPreview(url) {
        previewFrame.src = url;
        togglePreview(true);
      }

      function togglePreview(show) {
        previewModal.style.display = show ? "flex" : "none";
        if (!show) {
          previewFrame.src = "";
        }
      }
    </script>
  </body>
</html>
