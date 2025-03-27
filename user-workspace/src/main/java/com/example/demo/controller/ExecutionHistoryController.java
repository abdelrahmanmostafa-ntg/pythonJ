package com.example.demo.controller;

import com.example.demo.model.ExecutionHistory;
import com.example.demo.repository.ExecutionHistoryRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class ExecutionHistoryController {
    private final ExecutionHistoryRepository executionHistoryRepository;

    public ExecutionHistoryController(ExecutionHistoryRepository executionHistoryRepository) {
        this.executionHistoryRepository = executionHistoryRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ExecutionHistory> getExecutionHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return executionHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ExecutionHistory getExecutionDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return executionHistoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Execution not found"));
    }
}