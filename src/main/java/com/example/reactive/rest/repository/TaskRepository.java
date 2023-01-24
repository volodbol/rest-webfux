package com.example.reactive.rest.repository;

import com.example.reactive.rest.model.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskRepository extends ReactiveCrudRepository<Task, Long> {

    Flux<Task> findAllByProjectId(Long projectId, Pageable pageable);

    Mono<Long> countAllByProjectId(Long projectId);

    Mono<Task> findByProjectIdAndId(Long projectId, Long taskId);

    @Modifying
    Mono<Void> deleteTaskByProjectIdAndId(Long projectId, Long taskId);

    @Modifying
    Mono<Void> deleteTasksByProjectId(Long projectId);

}
