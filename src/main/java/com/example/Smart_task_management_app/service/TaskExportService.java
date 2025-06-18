package com.example.Smart_task_management_app.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.Smart_task_management_app.entity.Task;
import com.example.Smart_task_management_app.entity.User;
import com.example.Smart_task_management_app.repository.TaskRepository;
import com.example.Smart_task_management_app.repository.UserRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskExportService {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private List<Task> getUserTasks(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return taskRepo.findByUser(user);
    }

    // ✅ Export CSV
    public void exportToCSV(String username, HttpServletResponse response) throws IOException {
        List<Task> tasks = getUserTasks(username);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,Name,Description,Category,Due Date,Status");
            for (Task t : tasks) {
                writer.printf("%d,%s,%s,%s,%s,%s%n",
                        t.getId(), t.getName(), t.getDescription(),
                        t.getCategory(), dtf.format(t.getDueDate()), t.getStatus());
            }
        }
    }

    // ✅ Export Excel
    public void exportToExcel(String username, HttpServletResponse response) throws IOException {
        List<Task> tasks = getUserTasks(username);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tasks");

            String[] columns = {"ID", "Name", "Description", "Category", "Due Date", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Task t : tasks) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getId());
                row.createCell(1).setCellValue(t.getName());
                row.createCell(2).setCellValue(t.getDescription());
                row.createCell(3).setCellValue(t.getCategory());
                row.createCell(4).setCellValue(dtf.format(t.getDueDate()));
                row.createCell(5).setCellValue(t.getStatus());
            }

            workbook.write(response.getOutputStream());
        }
    }

    // ✅ Export PDF
    public void exportToPDF(String username, HttpServletResponse response) throws Exception {
        List<Task> tasks = getUserTasks(username);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
        com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        com.itextpdf.text.Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("Task List", titleFont));
        document.add(new Paragraph("Generated for: " + username));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 3, 5, 3, 3, 2});

        String[] headers = {"ID", "Name", "Description", "Category", "Due Date", "Status"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }

        for (Task t : tasks) {
            table.addCell(new Phrase(String.valueOf(t.getId()), bodyFont));
            table.addCell(new Phrase(t.getName(), bodyFont));
            table.addCell(new Phrase(t.getDescription(), bodyFont));
            table.addCell(new Phrase(t.getCategory(), bodyFont));
            table.addCell(new Phrase(dtf.format(t.getDueDate()), bodyFont));
            table.addCell(new Phrase(t.getStatus(), bodyFont));
        }

        document.add(table);
        document.close();
    }
}
