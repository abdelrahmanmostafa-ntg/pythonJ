package com.example.demo.service;

import org.python.core.Py;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.concurrent.*;

@Service
public class PythonExecutionService {
    @Value("${python.execution.timeout}")
    private long timeout;
    
    @Value("${python.max.memory}")
    private int maxMemory;

    public String executePythonCode(String code) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            PySystemState.initialize();
            PySystemState sys = Py.getSystemState();
            sys.setMaxStackSize(maxMemory);
            
            try (PythonInterpreter interpreter = new PythonInterpreter()) {
                StringWriter output = new StringWriter();
                interpreter.setOut(output);
                interpreter.setErr(output);
                
                interpreter.exec("import sys\n" +
                               "sys.path.append('.')\n" +
                               "import numpy as np\n" +
                               "import pandas as pd\n" +
                               "from sklearn import *\n");
                
                interpreter.exec(code);
                return output.toString();
            }
        });

        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new Exception("Python execution timed out after " + timeout + "ms");
        } catch (Exception e) {
            throw new Exception("Error executing Python code: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }
}