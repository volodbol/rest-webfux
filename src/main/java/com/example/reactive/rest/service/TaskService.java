package com.example.reactive.rest.service;

import com.example.reactive.rest.dto.TaskCreationDto;
import com.example.reactive.rest.mapper.TaskMapper;
import com.example.reactive.rest.model.Task;
import com.example.reactive.rest.repository.TaskRepository;
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
public class TaskService {

    private final ProjectService projectService;

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    public Mono<Page<Task>> getTasks(Long projectId, Pageable pageable) {
        log.debug("Getting all tasks, project id: {}", projectId);
        return taskRepository.findAllByProjectId(projectId, pageable)
                .collectList()
                .zipWith(taskRepository.countAllByProjectId(projectId))
                .flatMap(tuple2 -> Mono.just(new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2())));
    }

    public Mono<Task> getTask(Long projectId, Long taskId) {
        log.debug("Getting task, project id: {}, task id: {}", projectId, taskId);
        return taskRepository.findByProjectIdAndId(projectId, taskId);
    }

    public Mono<Task> createTask(Long projectId, TaskCreationDto taskCreationDto) {
        log.debug("Creating task, project id: {}, task creation dto: {}", projectId, taskCreationDto);
        Task task = taskMapper.toEntity(taskCreationDto);
        task.setProjectId(projectId);
        return projectService.isProjectExist(projectId)
                .flatMap((Boolean aBoolean) -> {
                    if (Boolean.TRUE.equals(aBoolean)) {
                        return taskRepository.save(task);
                    }
                    return Mono.empty();
                });
    }

    public Mono<Task> updateTask(Long projectId, Long taskId, TaskCreationDto taskCreationDto) {
        log.debug("Updating task, project id: {}, task updating dto: {}", projectId, taskCreationDto);
        return projectService.isProjectExist(projectId)
                .flatMap((Boolean aBoolean) -> {
                    if (Boolean.TRUE.equals(aBoolean)) {
                        return updateTaskModel(projectId, taskId, taskCreationDto);
                    }
                    return Mono.empty();
                })
                .flatMap(taskRepository::save);
    }

    private Mono<Task> updateTaskModel(Long projectId, Long taskId, TaskCreationDto taskCreationDto) {
        return Mono.zip(
                taskRepository.findByProjectIdAndId(projectId, taskId),
                Mono.just(taskCreationDto),
                (Task task, TaskCreationDto taskUpdateDto) -> {
                    task.setDescription(taskUpdateDto.getDescription());
                    return task;
                }
        );
    }

    public Mono<Void> deleteTask(Long projectId, Long taskId) {
        log.debug("Deleting task, project id: {}, task id: {}", projectId, taskId);
        return taskRepository.deleteTaskByProjectIdAndId(projectId, taskId);
    }

    public Mono<Void> deleteProjectTasks(Long projectId) {
        log.debug("Deleting all task for project id: {}", projectId);
        return taskRepository.deleteTasksByProjectId(projectId);
    }
}
