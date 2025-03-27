package com.example.demo.service;

import com.example.demo.model.ExecutionHistory;
import com.example.demo.model.User;
import com.example.demo.repository.ExecutionHistoryRepository;
import com.example.demo.repository.UserRepository;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PythonExecutionService {
    private final Engine engine;
    private final UserRepository userRepository;
    private final ExecutionHistoryRepository executionHistoryRepository;
    
    @Value("${python.execution.timeout:5000}")
    private long timeout;
    
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");
    private static final String[] FORBIDDEN_MODULES = {"os", "sys", "subprocess", "shutil", "importlib"};

    public String executePython(String code, Long userId) throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        code = replaceTemplates(code, user);
        validateCodeSecurity(code);

        ExecutionHistory history = new ExecutionHistory();
        history.setUser(user);
        history.setCode(code);
        long startTime = System.currentTimeMillis();

        try (Context context = Context.newBuilder("python")
            .engine(engine)
            .allowAllAccess(false)
            .allowIO(false)
            .build()) {
            
            context.eval("python", createRestrictedEnvironment());
            CompletableFuture<Value> future = CompletableFuture.supplyAsync(() -> 
                context.eval("python", code)
            );

            Value result = future.get(timeout, TimeUnit.MILLISECONDS);
            String output = result.asString();
            
            history.setResult(output);
            history.setStatus("SUCCESS");
            history.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            executionHistoryRepository.save(history);
            
            return output;
        } catch (TimeoutException e) {
            history.setResult("Execution timed out");
            history.setStatus("TIMEOUT");
            history.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            executionHistoryRepository.save(history);
            throw new Exception("Execution timed out after " + timeout + "ms");
        } catch (Exception e) {
            history.setResult(e.getMessage());
            history.setStatus("ERROR");
            history.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            executionHistoryRepository.save(history);
            throw new Exception("Error executing Python code: " + e.getMessage());
        }
    }

    private String replaceTemplates(String code, User user) {
        Matcher matcher = TEMPLATE_PATTERN.matcher(code);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String replacement = switch (varName) {
                case "userId" -> user.getId().toString();
                case "username" -> user.getUsername();
                case "email" -> user.getEmail();
                default -> "null";
            };
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private void validateCodeSecurity(String code) throws Exception {
        for (String module : FORBIDDEN_MODULES) {
            if (code.contains("import " + module) || 
                code.contains("from " + module)) {
                throw new Exception("Forbidden module: " + module);
            }
        }
        
        if (code.contains("eval(") || code.contains("exec(")) {
            throw new Exception("Dynamic code evaluation is not allowed");
        }
    }

    private String createRestrictedEnvironment() {
        return """
            import numpy as np
            import pandas as pd
            from sklearn import *
            
            # Override dangerous builtins
            __import__ = None
            open = None
            eval = None
            exec = None
            """;
    }
}