package com.example.Smart_task_management_app.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Smart_task_management_app.entity.Task;
import com.example.Smart_task_management_app.entity.User;
import com.example.Smart_task_management_app.repository.TaskRepository;
import com.example.Smart_task_management_app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    @GetMapping("/predict-category")
    public ResponseEntity<String> predictNextCategory(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();

        List<Task> tasks = taskRepo.findByUser(user);
        if (tasks.isEmpty()) {
            return ResponseEntity.ok("General");
        }

        Map<String, Long> freq = tasks.stream()
                .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));

        String predicted = freq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("General");

        return ResponseEntity.ok(predicted);
    }
}
