package com.example.reactive.rest.service;

import com.example.reactive.rest.dto.ProjectCreationDto;
import com.example.reactive.rest.mapper.ProjectMapper;
import com.example.reactive.rest.model.Project;
import com.example.reactive.rest.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final ProjectMapper projectMapper;


    public Mono<Page<Project>> getAllProjects(Pageable pageable) {
        log.debug("Getting all projects with: {}", pageable);
        return projectRepository.findAllBy(pageable)
                .collectList()
                .zipWith(projectRepository.count())
                .flatMap(tuple2 -> Mono.just(new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2())));
    }

    public Mono<Project> findById(Long id) {
        log.debug("Getting project with id: {}", id);
        return projectRepository.findById(id);
    }


    public Mono<Project> createProject(ProjectCreationDto projectCreationDto) {
        log.debug("Saving new project: {}", projectCreationDto);
        return projectRepository.save(projectMapper.toEntity(projectCreationDto));
    }

    public Mono<Project> updateProject(Long id, ProjectCreationDto projectCreationDto) {
        log.debug("Updating project, id: {}, project dto: {}", id, projectCreationDto);
        return projectRepository.findById(id)
                .zipWith(
                        Mono.just(projectCreationDto),
                        (Project project, ProjectCreationDto projectDto) -> {
                            project.setName(projectDto.getName());
                            return project;
                        })
                .flatMap(projectRepository::save);

    }

    public Mono<Void> deleteProject(Long id) {
        log.debug("Deleting project with id: {}", id);
        return projectRepository.deleteById(id);
    }

    public Mono<Boolean> isProjectExist(Long projectId) {
        log.debug("Searching if project exist, id: {}", projectId);
        return projectRepository.existsById(projectId);
    }

}
