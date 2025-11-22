# RUN_GUIDE - Hướng dẫn chạy nhanh (Windows / PowerShell)

Tệp này tách riêng hướng dẫn chạy nhanh để bạn dùng trong buổi demo hoặc chạy local.
Các bước dưới đây giả định bạn đang chạy trên Windows và đang dùng PowerShell.

---

## 1) Yêu cầu trước

- **JDK 21 LTS** đã cài và đặt `JAVA_HOME` (project đã upgrade lên Java 21)
- **Maven 3.9+** có trong `PATH` (đã cài tại `C:\Users\HP\.maven\apache-maven-3.9.9`)
- MySQL đang chạy
- Apache Tomcat (để deploy `.war`) - port mặc định 8080

> **Lưu ý:** Project đã được nâng cấp lên Java 21 LTS. Đảm bảo bạn đang dùng JDK 21 hoặc cao hơn.---

## 2) Thiết lập database

Mở PowerShell và chạy (điền `root`/mật khẩu nếu cần):

```powershell
mysql -u root -p < database.sql
```

Nếu bạn muốn chạy từng lệnh trong MySQL client:

```powershell
mysql -u root -p
# sau đó trong mysql> paste nội dung của database.sql hoặc chạy SOURCE path/to/database.sql
```

Sau khi tạo xong, chỉnh `src/main/resources/database.properties` nếu cần (đổi `db.user` / `db.password`).

---

## 3) Build project (Tạo WAR)

Mở PowerShell tại thư mục gốc dự án (nơi có `pom.xml`) và chạy:

```powershell
mvn clean package
```

Kết quả: `target\file-converter.war` sẽ được tạo.

---

## 4) Triển khai web app lên Tomcat

- Sao chép `target\file-converter.war` vào `TOMCAT_HOME\webapps\`.
- Khởi động Tomcat (nếu chưa chạy): `TOMCAT_HOME\bin\startup.bat` hoặc dùng Tomcat service.
- Mở trình duyệt: `http://localhost:8080/file-converter/`

```powershell
# Ví dụ Tomcat ở D:\Code\Java\apache-tomcat-9.0.112
Copy-Item -Force .\target\file-converter.war "D:\Code\Java\apache-tomcat-9.0.112\webapps\file-converter.war"

$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
$env:JRE_HOME = $env:JAVA_HOME
$env:CATALINA_HOME = "D:\Code\Java\apache-tomcat-9.0.112"
& "$env:CATALINA_HOME\bin\startup.bat"

Start-Process "http://localhost:8080/file-converter/"
```

---

## 5) Chạy Conversion Server (Module B)

Conversion Server phải chạy trước khi upload file. Có 2 cách:

A) Dùng script (đã tạo `start-server.bat`):

```powershell
# Tại thư mục dự án
start-server.bat
```

B) Dùng Maven để chạy trực tiếp main class:

```powershell
mvn exec:java -Dexec.mainClass="com.server.core.ServerMain"
```

Bạn sẽ thấy console hiển thị:

```
[Server] BlockingQueue initialized
[Server] ThreadPool created with 3 workers
[Worker-1] Started and waiting for tasks...
[Server] ✅ Server started on port 9999
```

---

## 6) Upload file từ web UI

- Vào `http://localhost:8080/file-converter/`
- Có thể chọn **nhiều** file `.docx` cùng lúc (Ctrl/Shift hoặc drag & drop)
- Trang status sẽ redirect sang `status.jsp` với `taskId`
- Trang dùng AJAX polling để kiểm tra `api/status?taskId=...`

```powershell
Start-Process "http://localhost:8080/file-converter/"
```

### Upload nhiều file trong một batch

- Sau khi chọn nhiều file và bấm Upload, bạn sẽ được chuyển đến `batch-status.jsp?batchId=...`
- Trang này hiển thị danh sách toàn bộ file, trạng thái từng file và nút `Download`, `Preview`, hoặc `Chi tiết`
- Có nút `Download All` để tải toàn bộ PDF dưới dạng ZIP một lần
- Click vào từng hàng sẽ mở trang `status.jsp` tương ứng (có nút quay lại danh sách)

---

## 7) Kiểm tra trực tiếp trong DB

Mở MySQL và chạy:

```sql
SELECT id, original_filename, status, file_path_output, error_message, created_at
FROM tasks ORDER BY created_at DESC LIMIT 20;
```

---

## 8) Một vài lệnh chẩn đoán (PowerShell)

- Kiểm tra port 9999 đang được listen bởi process nào:

```powershell
netstat -ano | findstr ":9999"
```

- Nếu port bị chiếm, tìm PID và kill (cẩn thận):

```powershell
tasklist /FI "PID eq <PID_FROM_NETSTAT>"
# Kill
taskkill /PID <PID_FROM_NETSTAT> /F
```

---

## 9) Thông báo lỗi thường gặp

- "Cannot connect to Conversion Server": Server chưa chạy hoặc firewall chặn port 9999.
- "Cannot connect to database": Kiểm tra MySQL đang chạy và `database.properties` đúng.
- File upload thành công nhưng status vẫn `PENDING`: Kiểm tra Server console xem task có được thêm vào queue hay không.

---

## 10) Stop Server

Trong cửa sổ PowerShell chạy `start-server.bat`, nhấn `Ctrl+C` để dừng server.

```powershell
& "$env:CATALINA_HOME\bin\shutdown.bat"
```

---

## 11) Đường dẫn tệp quan trọng trong project

- `database.sql` - schema DB
- `src/main/resources/database.properties` - cấu hình DB
- `start-server.bat` - script chạy Conversion Server
- `build.bat` - build project (Windows)
- `target\file-converter.war` - file deploy lên Tomcat

---

## 12) Chuỗi lệnh mẫu “chạy lại từ đầu”

Cụm lệnh dưới đây tổng hợp lại toàn bộ thao tác đã thực hiện trong phiên làm việc gần nhất (PowerShell). Bạn có thể copy/paste từng khối theo thứ tự để dựng lại toàn bộ pipeline.

```powershell
# 1) Mở dự án và bật Conversion Server
cd E:\.vscode\Ki_5\LTM
$env:PATH = "C:\Users\HP\.maven\apache-maven-3.9.9\bin;$env:PATH"
mvn exec:java -Dexec.mainClass="com.server.core.ServerMain"
```

```powershell
# 2) Đợi 5 giây rồi xác nhận port 9999 đã listen
Start-Sleep -Seconds 5; netstat -ano | findstr ":9999"
```

```powershell
# 3) Copy lại WAR mới build vào Tomcat (ví dụ Tomcat đặt tại D:\Code\Java\apache-tomcat-9.0.112)
Copy-Item -Force .\target\file-converter.war "D:\Code\Java\apache-tomcat-9.0.112\webapps\file-converter.war"
```

```powershell
# 4) Khai báo biến môi trường và khởi động Tomcat
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
$env:JRE_HOME = $env:JAVA_HOME
$env:CATALINA_HOME = "D:\Code\Java\apache-tomcat-9.0.112"
& "$env:CATALINA_HOME\bin\startup.bat"
```

```powershell
# 5) Smoke-test web UI để chắc chắn Tomcat + app đã sẵn sàng
try {
	$resp = Invoke-WebRequest -Uri "http://localhost:8080/file-converter/" -UseBasicParsing
	Write-Host "HTTP" $resp.StatusCode
} catch {
	Write-Error $_
}
```

Khi cần dừng hệ thống, dùng `Ctrl+C` tại cửa sổ Conversion Server và chạy `& "$env:CATALINA_HOME\bin\shutdown.bat"` cho Tomcat.

---

## 13) API/Endpoint mới hỗ trợ batch

- `POST /upload`: giờ chấp nhận trường `files` (multiple). Tự động tạo `batchId` để gom task.
- `GET /batch-status.jsp?batchId=...`: giao diện tổng hợp trạng thái.
- `GET /api/batch-status?batchId=...`: REST JSON trả về danh sách task + link download.
- `GET /download?taskId=...`: tải file PDF đã convert (thêm `mode=inline` để xem trước).
- `GET /download-all?batchId=...`: gom tất cả file `COMPLETED` thành một ZIP.

Conversion Server hiện chạy **1 worker thread** để bảo đảm xử lý lần lượt theo thứ tự upload (FIFO).
