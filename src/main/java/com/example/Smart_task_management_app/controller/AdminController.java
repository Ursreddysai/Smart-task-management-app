package com.example.Smart_task_management_app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Smart_task_management_app.entity.Task;
import com.example.Smart_task_management_app.entity.User;
import com.example.Smart_task_management_app.repository.TaskRepository;
import com.example.Smart_task_management_app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepo;
    private final TaskRepository taskRepo;

    // 1. List all users
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    // 2. Deactivate user
    @PutMapping("/deactivate/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUser(@PathVariable String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        user.setActive(false);
        userRepo.save(user);
        return ResponseEntity.ok("User deactivated");
    }

    // 3. Reactivate user
    @PutMapping("/activate/{username}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> activateUser(@PathVariable String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        user.setActive(true);
        userRepo.save(user);
        return ResponseEntity.ok("User activated");
    }

    // 4. Get all tasks of a specific user
    @GetMapping("/tasks/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Task>> getTasksByUser(@PathVariable String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(taskRepo.findByUser(user));
    }
}
