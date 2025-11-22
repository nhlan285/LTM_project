# Hướng dẫn chạy project trên Eclipse IDE

## Yêu cầu trước khi bắt đầu

- Eclipse IDE for Enterprise Java and Web Developers
- JDK 21 đã cài đặt
- Apache Tomcat 9.0
- MySQL đang chạy (qua XAMPP)
- Maven đã tích hợp trong Eclipse

---

## Bước 1: Import Project vào Eclipse

1. Mở Eclipse IDE
2. `File` → `Import...`
3. Chọn `Maven` → `Existing Maven Projects`
4. Click `Next`
5. `Root Directory`: Browse đến thư mục mà bạn clone project về
6. Đảm bảo `pom.xml` được check ✓
7. Click `Finish`
8. Chuột phải vào project vừa được import, chọn `Properties`
9. Chọn `Project Facets`, đảm bảo Java 21 và `Dynamic Web Module` đã được tick

Eclipse sẽ tự động import và build project. Chờ quá trình "Building workspace" hoàn tất.

---

## Bước 2: Cấu hình JDK 21 trong Project

1. Click chuột phải vào project `distributed-file-converter`
2. `Properties` → `Java Build Path`
3. Tab `Libraries`
4. Nếu thấy JRE System Library không phải JDK 21:
   - Double-click vào `JRE System Library`
   - Chọn `Alternate JRE` → Click `Installed JREs...`
   - Click `Add...` → `Standard VM` → `Next`
   - `JRE home`: Browse đến thư mục JDK 21 (ví dụ: `C:\Program Files\Java\jdk-21`)
   - `JRE name`: Đặt tên là `JDK-21`
   - Click `Finish` → Check vào JDK-21 → `Apply and Close`
   - Chọn `JDK-21` làm Alternate JRE → `Finish`
5. Click `Apply and Close`

---

## Bước 3: Cấu hình Tomcat Server trong Eclipse

### 3.1. Thêm Tomcat Runtime

1. `Window` → `Preferences`
2. `Server` → `Runtime Environments`
3. Click `Add...`
4. Chọn phiên bản Tomcat của bạn:
   - **Tomcat 9.x**: `Apache Tomcat v9.0`
5. Click `Next`
6. `Tomcat installation directory`: Browse đến thư mục Tomcat của bạn
7. `JRE`: Chọn `JDK-21` (vừa cấu hình ở bước 2)
8. Click `Finish` → `Apply and Close`

### 3.2. Tạo Server Instance

1. Mở view `Servers` (nếu chưa có: `Window` → `Show View` → `Servers`)
2. Click chuột phải trong vùng trống → `New` → `Server`
3. Chọn Tomcat version → Click `Next`
4. Trong phần `Available`, chọn `distributed-file-converter`
5. Click `Add >` để thêm vào `Configured`
6. Click `Finish`

---

## Bước 4: Import Database Schema

1. Mở MySQL qua XAMPP Control Panel hoặc MySQL Workbench
2. Mở PowerShell hoặc Command Prompt, tại thư mục chứa file `database.sql`:
   ```powershell
   mysql -u root -p < database.sql
   ```
3. Nhập password MySQL (mặc định XAMPP là để trống, nhấn Enter)
4. Database `file_converter_db` sẽ được tạo

(Hoặc sử dụng Apache + MySQL của XAMPP, import database từ file)

### Kiểm tra database properties

Mở file `src/main/resources/database.properties` và đảm bảo thông tin đúng:

```properties
db.url=jdbc:mysql://localhost:3306/file_converter_db
db.user=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver
```

Nếu MySQL của bạn có password, sửa `db.password`.

---

Để test, chạy file `test-db.bat`

## Bước 5: Tạo Run Configuration cho Conversion Server

### 5.1. Tạo Java Application Run Configuration

1. Trong Eclipse, tìm file `ServerMain.java` trong:
   - `src/main/java` → `com.server.core` → `ServerMain.java`
2. Click chuột phải vào file → `Run As` → `Java Application`

**Hoặc tạo Run Configuration thủ công:**

1. `Run` → `Run Configurations...`
2. Click chuột phải vào `Java Application` → `New Configuration`
3. **Name**: `Conversion Server`
4. **Project**: `distributed-file-converter`
5. **Main class**: `com.server.core.ServerMain`
6. Tab `JRE`: Chọn `JDK-21`
7. Click `Apply` → `Run`

### 5.2. Kiểm tra Server đã chạy

Console của Eclipse sẽ hiển thị:

```
═══════════════════════════════════════════════════
  FILE CONVERSION SERVER (MODULE B) - STARTING
═══════════════════════════════════════════════════
[Server] BlockingQueue initialized (Capacity: Unlimited)
[Server] ThreadPool created with 3 workers
[Server] All worker threads started and waiting for tasks
[Worker-1] Started and waiting for tasks...
[Worker-2] Started and waiting for tasks...
[Worker-3] Started and waiting for tasks...
[Server] ✓ Server started on port 9999
[Server] Waiting for connections from Web Server...
```

**⚠️ QUAN TRỌNG:** Giữ cửa sổ chương tình chạy. KHÔNG tắt Conversion Server khi đang dùng web app.

---

## Bước 6: Deploy và chạy Web Application trên Tomcat

### 6.1. Deploy Project

1. Trong tab `Servers`, tìm Tomcat server vừa tạo
2. Click chuột phải vào server → `Add and Remove...`
3. Chọn `distributed-file-converter` từ `Available` → Click `Add >`
4. Click `Finish`

### 6.2. Start Tomcat Server

1. Click chuột phải vào Tomcat server → `Start`
2. Hoặc click nút ▶️ (Start) trong toolbar của tab Servers
3. Chờ console hiển thị: `Server startup in [xxx] milliseconds`

### 6.3. Truy cập Web Application

Mở trình duyệt và truy cập:

```
http://localhost:8080/distributed-file-converter/
```

---

## Bước 7: Test Upload File

1. Chuẩn bị 1 file DOCX để test
2. Truy cập `http://localhost:8080/distributed-file-converter/`
3. Click vào vùng "Click to select a DOCX file" hoặc kéo thả file
4. Click `Upload & Convert`
5. Trang sẽ redirect sang `status.jsp` và tự động cập nhật trạng thái
6. Khi xong, file PDF sẽ tự động download

### Kiểm tra trong Console

- **Web App Console (Tomcat)**: Hiển thị log upload và kết nối database
- **Conversion Server Console**: Hiển thị log nhận task và convert file

---

## Troubleshooting - Khắc phục lỗi thường gặp

### Lỗi 1: "Conversion server is not available!"

**Nguyên nhân:** Conversion Server chưa chạy hoặc không kết nối được port 9999

**Giải pháp:**

1. Kiểm tra Conversion Server có đang chạy không (xem Bước 5)
2. Trong PowerShell, kiểm tra port 9999:
   ```powershell
   netstat -ano | findstr ":9999"
   ```
3. Nếu không có output → Server chưa chạy, chạy lại ServerMain
4. Kiểm tra Firewall có chặn port 9999 không

### Lỗi 2: "Oops! Something went wrong" khi truy cập ServerMain.java

**Nguyên nhân:** Eclipse cố gắng mở file .java qua web browser

**Giải pháp:**

- Đây là do Eclipse deploy cả source code vào Tomcat
- **Cách fix:**
  1. Click chuột phải vào Tomcat server → `Clean...`
  2. Click chuột phải vào Tomcat server → `Clean Tomcat Work Directory...`
  3. Restart Tomcat server
  4. Chỉ truy cập URL chính: `http://localhost:8080/distributed-file-converter/`

### Lỗi 3: "Cannot connect to database"

**Nguyên nhân:** MySQL chưa chạy hoặc thông tin kết nối sai

**Giải pháp:**

1. Kiểm tra MySQL đang chạy trong XAMPP
2. Kiểm tra `database.properties` có đúng thông tin không
3. Test kết nối MySQL:
   ```powershell
   mysql -u root -p -e "SHOW DATABASES;"
   ```
4. Đảm bảo database `file_converter_db` đã được tạo

### Lỗi 4: "Server Tomcat v9.0 Server at localhost failed to start"

**Nguyên nhân:** Port 8080 đã được dùng bởi process khác

**Giải pháp:**

1. Kiểm tra port 8080:
   ```powershell
   netstat -ano | findstr ":8080"
   ```
2. Nếu port bị chiếm, kill process hoặc đổi port Tomcat:
   - Double-click vào Tomcat server trong tab Servers
   - Tìm `HTTP/1.1` port → đổi từ `8080` sang `8081` hoặc port khác
   - `Ctrl+S` để save
   - Restart server

### Lỗi 5: Project báo lỗi build / Maven dependencies không tải

**Giải pháp:**

1. Click chuột phải vào project → `Maven` → `Update Project...`
2. Check vào `Force Update of Snapshots/Releases`
3. Click `OK`
4. Nếu vẫn lỗi, clean project:
   - `Project` → `Clean...` → Chọn project → `Clean`

### Lỗi 6: "Java version mismatch" hoặc "Unsupported class file version"

**Nguyên nhân:** Project được compile với Java 21 nhưng Eclipse/Tomcat dùng Java cũ hơn

**Giải pháp:**

1. Kiểm tra Java version của Tomcat (xem Bước 3.1)
2. Kiểm tra Compiler Compliance Level:
   - Click chuột phải project → `Properties`
   - `Java Compiler` → `Compiler compliance level` phải là `21`
3. Clean và Rebuild project

---

## Quy trình chạy đầy đủ (Tóm tắt)

### Khởi động lần đầu:

1. ✅ Mở Eclipse và import project
2. ✅ Cấu hình JDK 21
3. ✅ Cấu hình Tomcat server
4. ✅ Import database schema vào MySQL
5. ✅ **Chạy Conversion Server** (Run ServerMain.java)
6. ✅ Start Tomcat server
7. ✅ Truy cập http://localhost:8080/distributed-file-converter/

### Chạy lần sau:

1. Mở Eclipse
2. **Chạy Conversion Server** (Run Configuration: "Conversion Server")
3. Start Tomcat server (click ▶️ trong tab Servers)
4. Truy cập web app

### Dừng dự án:

1. Stop Tomcat server (click ⏹️ trong tab Servers)
2. Stop Conversion Server (click 🔴 trong Console)

---

## Tips & Best Practices

### 1. Hot Reload trong Eclipse

- Eclipse có thể tự động reload code khi bạn sửa JSP/HTML
- Với Java code: `Project` → `Build Automatically` (check ✓)

### 2. Debug Conversion Server

1. Click chuột phải vào `ServerMain.java` → `Debug As` → `Java Application`
2. Đặt breakpoint trong code để debug
3. Server sẽ dừng tại breakpoint khi có request

### 3. View Console Log riêng

- Mỗi ứng dụng chạy có console riêng
- Chuyển đổi giữa các console bằng icon 📺 trong tab Console
- `Conversion Server` console: Hiển thị log của ServerMain
- `Tomcat` console: Hiển thị log của web app

### 4. Thay đổi Port nếu bị conflict

- **Tomcat (8080)**: Xem Troubleshooting Lỗi 4
- **Conversion Server (9999)**:
  - Mở `ServerMain.java`
  - Tìm dòng: `private static final int PORT = 9999;`
  - Đổi sang port khác, ví dụ: `9998`
  - **LƯU Ý:** Phải đổi port tương ứng trong `UploadServlet.java` nơi kết nối đến server

## Hỗ trợ thêm

Nếu gặp lỗi không nằm trong danh sách trên, kiểm tra:

1. Eclipse Error Log: `Window` → `Show View` → `Error Log`
2. Tomcat logs: Trong thư mục `workspace\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\logs\`
3. Console output của cả 2 servers

---