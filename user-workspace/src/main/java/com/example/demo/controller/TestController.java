package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PythonExecutionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private final PythonExecutionService pythonService;
    private final UserRepository userRepository;

    public TestController(PythonExecutionService pythonService, 
                         UserRepository userRepository) {
        this.pythonService = pythonService;
        this.userRepository = userRepository;
    }

    @GetMapping("/python") 
    public String testPythonExecution() throws Exception {
        String pythonCode = "import pandas as pd\n" +
                          "df = pd.DataFrame({'A': [1, 2, 3], 'B': [4, 5, 6]})\n" +
                          "print(df.describe())";
        // Use first user's ID for testing
        User firstUser = userRepository.findAll().stream().findFirst().orElseThrow();
        return pythonService.executePython(pythonCode, firstUser.getId());
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}