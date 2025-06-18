
package com.example.Smart_task_management_app.controller;

import com.example.Smart_task_management_app.service.TaskExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class TaskExportController {

    private final TaskExportService taskExportService;

    @GetMapping("/csv")
    public void exportCSV(HttpServletResponse response, Principal principal) throws IOException {
        taskExportService.exportToCSV(principal.getName(), response);
    }

    @GetMapping("/excel")
    public void exportExcel(HttpServletResponse response, Principal principal) throws IOException {
        taskExportService.exportToExcel(principal.getName(), response);
    }

    @GetMapping("/pdf")
    public void exportPDF(HttpServletResponse response, Principal principal) throws Exception {
        taskExportService.exportToPDF(principal.getName(), response);
    }
}
