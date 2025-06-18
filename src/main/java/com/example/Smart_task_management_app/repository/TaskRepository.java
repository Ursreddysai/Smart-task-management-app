package com.example.Smart_task_management_app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.Smart_task_management_app.entity.Task;
import com.example.Smart_task_management_app.entity.User;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser(User user);

    List<Task> findByUserId(Long userId);

    List<Task> findByUserAndDueDate(User user, LocalDate dueDate);

    List<Task> findByUserAndStatusAndDueDateBetween(User user, String status, LocalDate start, LocalDate end);

    List<Task> findByUserAndDueDateAfter(User user, LocalDate date);
// Get count of tasks by category for user

    @Query("SELECT t.category, COUNT(t) FROM Task t WHERE t.user = :user GROUP BY t.category")
    List<Object[]> countTasksByCategory(User user);
}
