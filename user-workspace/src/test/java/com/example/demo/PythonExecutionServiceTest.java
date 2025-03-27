package com.example.demo;

import com.example.demo.service.PythonExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PythonExecutionServiceTest {
    @Autowired
    private PythonExecutionService pythonService;

    @Test
    void testSimplePythonExecution() throws Exception {
        String result = pythonService.executePythonCode("print('Hello, World!')");
        assertTrue(result.contains("Hello, World!"));
    }

    @Test
    void testPandasExecution() throws Exception {
        String code = "import pandas as pd\n" +
                     "df = pd.DataFrame({'A': [1, 2, 3]})\n" +
                     "print(df.sum())";
        String result = pythonService.executePythonCode(code);
        assertTrue(result.contains("6"));
    }

    @Test
    void testNumpyExecution() throws Exception {
        String code = "import numpy as np\n" +
                     "arr = np.array([1, 2, 3])\n" +
                     "print(np.sum(arr))";
        String result = pythonService.executePythonCode(code);
        assertTrue(result.contains("6"));
    }
}