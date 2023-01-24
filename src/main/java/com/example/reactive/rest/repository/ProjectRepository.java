package com.example.reactive.rest.repository;

import com.example.reactive.rest.model.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProjectRepository extends ReactiveCrudRepository<Project, Long> {

    Flux<Project> findAllBy(Pageable pageable);

}
