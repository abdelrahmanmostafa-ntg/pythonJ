package com.example.demo.repository;

import com.example.demo.model.ExecutionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExecutionHistoryRepository extends JpaRepository<ExecutionHistory, Long> {
    List<ExecutionHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ExecutionHistory> findByIdAndUserId(Long id, Long userId);
}
