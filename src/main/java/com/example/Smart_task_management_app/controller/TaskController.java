package com.example.Smart_task_management_app.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Smart_task_management_app.entity.Task;
import com.example.Smart_task_management_app.entity.User;
import com.example.Smart_task_management_app.repository.TaskRepository;
import com.example.Smart_task_management_app.repository.UserRepository;
import com.example.Smart_task_management_app.service.TaskExportService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;
    private final TaskExportService exportService;

   
    public static class TaskRequest {

        public String name;
        public String description;
        public String category;
        public LocalDate dueDate;
        public String status;
    }

    public static class TaskResponse {

        public Long id;
        public String name;
        public String description;
        public String category;
        public LocalDate dueDate;
        public String status;

        public TaskResponse(Task task) {
            this.id = task.getId();
            this.name = task.getName();
            this.description = task.getDescription();
            this.category = task.getCategory();
            this.dueDate = task.getDueDate();
            this.status = task.getStatus();
        }
    }

   
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = new Task();
        task.setName(req.name);
        task.setDescription(req.description);
        task.setCategory(req.category);
        task.setDueDate(req.dueDate);
        task.setStatus(req.status);
        task.setUser(user);

        Task saved = taskRepo.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TaskResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();
        List<TaskResponse> list = taskRepo.findByUser(user).stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Task task = taskRepo.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUser().getUsername().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(new TaskResponse(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        Task task = taskRepo.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUser().getUsername().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        task.setName(req.name);
        task.setDescription(req.description);
        task.setCategory(req.category);
        task.setDueDate(req.dueDate);
        task.setStatus(req.status);

        Task updated = taskRepo.save(task);
        return ResponseEntity.ok(new TaskResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Task task = taskRepo.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUser().getUsername().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        taskRepo.delete(task);
        return ResponseEntity.noContent().build();
    }

    
    @GetMapping("/due-today")
    public ResponseEntity<List<TaskResponse>> tasksDueToday(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();
        var tasks = taskRepo.findByUserAndDueDate(user, LocalDate.now()).stream()
                .map(TaskResponse::new).toList();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/completed-last-7-days")
    public ResponseEntity<List<TaskResponse>> completedLast7Days(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        var tasks = taskRepo.findByUserAndStatusAndDueDateBetween(user, "Completed", start, end).stream()
                .map(TaskResponse::new).toList();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<TaskResponse>> upcomingTasks(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();
        var tasks = taskRepo.findByUserAndDueDateAfter(user, LocalDate.now()).stream()
                .map(TaskResponse::new).toList();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/popular-categories")
    public ResponseEntity<Map<String, Long>> popularCategories(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();
        List<Object[]> result = taskRepo.countTasksByCategory(user);
        Map<String, Long> data = new HashMap<>();
        for (Object[] row : result) {
            data.put((String) row[0], (Long) row[1]);
        }
        return ResponseEntity.ok(data);
    }

   
    @GetMapping("/export/csv")
    public void exportToCSV(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) throws IOException {
        exportService.exportToCSV(userDetails.getUsername(), response);
    }

    @GetMapping("/export/excel")
    public void exportToExcel(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) throws IOException {
        exportService.exportToExcel(userDetails.getUsername(), response);
    }

    @GetMapping("/export/pdf")
    public void exportToPDF(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) throws Exception {
        exportService.exportToPDF(userDetails.getUsername(), response);
    }

    @GetMapping("/export/json")
    public ResponseEntity<List<TaskResponse>> exportToJSON(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();
        var list = taskRepo.findByUser(user).stream().map(TaskResponse::new).toList();
        return ResponseEntity.ok(list);
    }
}
