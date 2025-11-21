package com.server.converter;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * =====================================================
 * DocxToPdfService - FILE CONVERSION ENGINE
 * =====================================================
 * Purpose: Converts DOCX files to PDF using Apache POI and iText
 * 
 * LIBRARIES USED:
 * - Apache POI: Reads .docx files (Microsoft Word format)
 * - iText: Writes PDF files
 * 
 * This is a STATELESS service class (Thread-Safe)
 * Multiple threads can call convertDocxToPdf() simultaneously
 * 
 * @author Your Name
 * @course Network Programming - Final Project
 */
public class DocxToPdfService {

    /**
     * Converts a DOCX file to PDF format
     * 
     * ALGORITHM:
     * 1. Open DOCX file using Apache POI
     * 2. Read all paragraphs
     * 3. Create a new PDF document using iText
     * 4. Write each paragraph to PDF
     * 5. Save and close
     * 
     * @param docxFilePath Full path to input .docx file
     * @param pdfFilePath  Full path where output .pdf will be saved
     * @throws IOException       if file read/write fails
     * @throws DocumentException if PDF creation fails
     */
    public void convertDocxToPdf(String docxFilePath, String pdfFilePath)
            throws IOException, DocumentException {

        System.out.println("[Converter] Starting conversion: " + docxFilePath);

        // STEP 1: Read DOCX file using Apache POI
        FileInputStream docxFile = null;
        XWPFDocument document = null;

        // STEP 2: Create PDF file using iText
        Document pdfDocument = null;
        FileOutputStream pdfFile = null;

        try {
            // Open DOCX file
            docxFile = new FileInputStream(docxFilePath);
            document = new XWPFDocument(docxFile);

            // Extract all paragraphs from DOCX
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            System.out.println("[Converter] Found " + paragraphs.size() + " paragraphs in DOCX");

            // Create PDF document (A4 size)
            pdfDocument = new Document(PageSize.A4);
            pdfFile = new FileOutputStream(pdfFilePath);
            PdfWriter.getInstance(pdfDocument, pdfFile);
            pdfDocument.open();

            // Use Unicode font to correctly display Vietnamese/English characters
            Font font = loadUnicodeFont();

            // STEP 3: Write each paragraph to PDF
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();

                // Skip empty paragraphs
                if (text == null || text.trim().isEmpty()) {
                    continue;
                }

                // Add paragraph to PDF
                Paragraph pdfParagraph = new Paragraph(text, font);
                pdfDocument.add(pdfParagraph);

                // Add space between paragraphs
                pdfDocument.add(Chunk.NEWLINE);
            }

            System.out.println("[Converter] ✅ Conversion successful: " + pdfFilePath);

        } catch (IOException e) {
            System.err.println("[Converter] ❌ IO Error: " + e.getMessage());
            throw e;
        } catch (DocumentException e) {
            System.err.println("[Converter] ❌ PDF Error: " + e.getMessage());
            throw e;
        } finally {
            // STEP 4: Clean up resources (CRITICAL!)
            if (pdfDocument != null && pdfDocument.isOpen()) {
                pdfDocument.close();
            }
            if (pdfFile != null) {
                pdfFile.close();
            }
            if (document != null) {
                document.close();
            }
            if (docxFile != null) {
                docxFile.close();
            }
        }
    }

    private Font loadUnicodeFont() {
        List<String> fontCandidates = Arrays.asList(
                buildWindowsFontPath("arial.ttf"),
                buildWindowsFontPath("arialuni.ttf"),
                buildWindowsFontPath("times.ttf"),
                buildWindowsFontPath("tahoma.ttf"),
                Paths.get("/usr/share/fonts", "truetype", "dejavu", "DejaVuSans.ttf").toString(),
                Paths.get("/usr/share/fonts", "truetype", "liberation", "LiberationSerif-Regular.ttf").toString());

        for (String fontPath : fontCandidates) {
            if (fontPath == null) {
                continue;
            }
            Path path = Paths.get(fontPath);
            if (!Files.exists(path)) {
                continue;
            }
            try {
                BaseFont baseFont = BaseFont.createFont(path.toAbsolutePath().toString(), BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED);
                System.out.println("[Converter] Using font: " + path);
                return new Font(baseFont, 12, Font.NORMAL, BaseColor.BLACK);
            } catch (DocumentException | IOException e) {
                System.err.println("[Converter] Failed loading font " + path + ": " + e.getMessage());
            }
        }

        FontFactory.registerDirectories();
        Font fallback = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.IDENTITY_H, true, 12, Font.NORMAL,
                BaseColor.BLACK);
        System.out.println("[Converter] Falling back to Helvetica (Identity-H)");
        return fallback;
    }

    private String buildWindowsFontPath(String fontFile) {
        String windir = System.getenv("WINDIR");
        if (windir == null) {
            return null;
        }
        return Paths.get(windir, "Fonts", fontFile).toString();
    }

    /**
     * Test method to verify conversion works
     * Run this to test the converter independently
     */
    public static void main(String[] args) {
        DocxToPdfService service = new DocxToPdfService();

        // Example usage (change paths to your actual files)
        String inputDocx = "C:/uploads/sample.docx";
        String outputPdf = "C:/uploads/sample.pdf";

        try {
            service.convertDocxToPdf(inputDocx, outputPdf);
            System.out.println("Test completed successfully!");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
