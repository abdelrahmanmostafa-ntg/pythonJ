package com.example.demo.controller;

import com.example.demo.service.PythonExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/python")
public class PythonExecutionController {
    private final PythonExecutionService pythonService;

    public PythonExecutionController(PythonExecutionService pythonService) {
        this.pythonService = pythonService;
    }

    @PostMapping("/execute")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> executePythonCode(@RequestBody String pythonCode) {
        try {
            String result = pythonService.executePythonCode(pythonCode);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}