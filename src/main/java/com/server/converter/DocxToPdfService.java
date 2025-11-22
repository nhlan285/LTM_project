package com.server.converter;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import java.io.*;

public class DocxToPdfService {
    public void convertDocxToPdf(String docxPath, String pdfPath) {
        File inputWord = new File(docxPath);
        File outputPdf = new File(pdfPath);

        try (InputStream docxInputStream = new FileInputStream(inputWord);
             OutputStream pdfOutputStream = new FileOutputStream(outputPdf)) {

            IConverter converter = LocalConverter.builder().build();
            
            converter.convert(docxInputStream).as(DocumentType.DOCX)
                     .to(pdfOutputStream).as(DocumentType.PDF)
                     .execute();
                     
            System.out.println("✅ Convert thành công bằng MS Word!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}