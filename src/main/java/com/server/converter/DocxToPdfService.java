package com.server.converter;

import java.io.File;
import java.io.IOException;

public class DocxToPdfService {
    
    // Đường dẫn LibreOffice (Hãy đảm bảo đúng với máy của bạn)
    private static final String LIBRE_PATH = "C:\\Program Files\\LibreOffice\\program\\soffice.exe";

    /**
     * Convert file với UserInstallation riêng biệt cho mỗi worker
     */
    public void convertDocxToPdf(String inputPath, String outputPath, int workerId)
            throws IOException, InterruptedException {

        File input = new File(inputPath);
        File outFile = new File(outputPath);
        File outDir = outFile.getParentFile();

        if (!outDir.exists()) outDir.mkdirs();

        // 1. TẠO ĐƯỜNG DẪN PROFILE TẠM THỜI
        // Windows Temp: C:\Users\User\AppData\Local\Temp\
        // Chúng ta tạo: C:\Users\User\AppData\Local\Temp\LibreOffice_Worker_1
        String tempDir = System.getProperty("java.io.tmpdir");
        File customUserDir = new File(tempDir, "LibreOffice_Worker_" + workerId);
        
        // Chuẩn hóa đường dẫn cho LibreOffice (dùng dấu gạch chéo / và thêm file:///)
        String userInstallationArg = "-env:UserInstallation=file:///" + 
                                     customUserDir.getAbsolutePath().replace("\\", "/");

        // 2. CẤU HÌNH PROCESS BUILDER
        ProcessBuilder pb = new ProcessBuilder(
            LIBRE_PATH,
            userInstallationArg, // <--- THAM SỐ QUAN TRỌNG ĐỂ CHẠY SONG SONG
            "--headless",
            "--invisible",
            "--norestore",
            "--nolockcheck",
            "--nodefault",
            "--nofirststartwizard",
            "--convert-to", "pdf",
            input.getAbsolutePath(),
            "--outdir", outDir.getAbsolutePath()
        );

        pb.redirectErrorStream(true);

        // 3. THỰC THI
        Process process = pb.start();
        
        // Đọc log từ LibreOffice (Optional - để debug nếu lỗi)
        // try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        //     String line;
        //     while ((line = reader.readLine()) != null) { System.out.println(line); }
        // }

        int exit = process.waitFor();

        if (exit != 0) {
            throw new RuntimeException("LibreOffice convert failed with exit code: " + exit);
        }

        // System.out.println("✔ LibreOffice (Worker " + workerId + "): Converted " + input.getName());
    }
}