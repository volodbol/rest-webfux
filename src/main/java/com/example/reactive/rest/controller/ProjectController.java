package com.example.reactive.rest.controller;

import com.example.reactive.rest.dto.ProjectCreationDto;
import com.example.reactive.rest.dto.TaskCreationDto;
import com.example.reactive.rest.model.Project;
import com.example.reactive.rest.model.Task;
import com.example.reactive.rest.service.ProjectService;
import com.example.reactive.rest.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Function;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private static final int DEFAULT_PAGE = 0;

    private static final int DEFAULT_PAGE_SIZE = 5;

    private final ProjectService projectService;

    private final TaskService taskService;

    @GetMapping
    public Mono<Page<EntityModel<Project>>> getAllProjects(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size) {
        return projectService.getAllProjects(PageRequest.of(page, size))
                .flatMap(projects -> Mono.just(projects.map(projectToEntityModel(page, size))));
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<EntityModel<Project>>> getProject(@PathVariable(name = "id") Long id) {
        return projectService.findById(id)
                .flatMap(project -> Mono.just(projectToEntityModel().apply(project)))
                .flatMap(projectEntityModel -> Mono.just(ResponseEntity.ok(projectEntityModel)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<EntityModel<Project>>> createProject(
            @RequestBody ProjectCreationDto projectCreationDto) {
        return projectService.createProject(projectCreationDto)
                .flatMap(project -> Mono.just(
                        ResponseEntity.created(URI.create("api/v1/projects/" + project.getId()))
                                .body(projectToEntityModel().apply(project))));
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<EntityModel<Project>>> updateProject(
            @RequestBody ProjectCreationDto projectCreationDto,
            @PathVariable(name = "id") Long id) {
        return projectService.updateProject(id, projectCreationDto)
                .flatMap(project -> Mono.just(ResponseEntity.ok(projectToEntityModel().apply(project))));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteProject(@PathVariable(name = "id") Long id) {
        return projectService.deleteProject(id);
    }

    @GetMapping("{projectId}/tasks")
    public Mono<Page<EntityModel<Task>>> getProjectTasks(
            @PathVariable(name = "projectId") Long projectId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size) {
        return taskService.getTasks(projectId, PageRequest.of(page, size))
                .flatMap(tasks -> Mono.just(tasks.map(taskToEntityModel(page, size))));
    }

    @GetMapping("{projectId}/tasks/{taskId}")
    public Mono<ResponseEntity<EntityModel<Task>>> getProjectTask(
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "taskId") Long taskId) {
        return taskService.getTask(projectId, taskId)
                .flatMap(task -> Mono.just(ResponseEntity.ok(taskToEntityModel().apply(task))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("{projectId}/tasks")
    public Mono<ResponseEntity<EntityModel<Task>>> createProjectTask(
            @PathVariable(name = "projectId") Long projectId,
            @RequestBody TaskCreationDto taskCreationDto) {
        return taskService.createTask(projectId, taskCreationDto)
                .flatMap(task -> Mono.just(
                        ResponseEntity.created(
                                        URI.create(
                                                "api/v1/projects/%s/tasks/%s"
                                                        .formatted(projectId, task.getId())))
                                .body(taskToEntityModel().apply(task))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("{projectId}/tasks/{taskId}")
    public Mono<ResponseEntity<EntityModel<Task>>> updateProjectTask(
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "taskId") Long taskId,
            @RequestBody TaskCreationDto taskCreationDto) {
        return taskService.updateTask(projectId, taskId, taskCreationDto)
                .flatMap(task -> Mono.just(ResponseEntity.ok(taskToEntityModel().apply(task))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{projectId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteProjectTask(
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "taskId") Long taskId) {
        return taskService.deleteTask(projectId, taskId);
    }

    private static Function<Project, EntityModel<Project>> projectToEntityModel() {
        return projectToEntityModel(DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
    }

    private static Function<Task, EntityModel<Task>> taskToEntityModel() {
        return taskToEntityModel(DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
    }

    private static Function<Project, EntityModel<Project>> projectToEntityModel(Integer page, Integer size) {
        return project -> EntityModel.of(project)
                .add(linkTo(methodOn(ProjectController.class).getProject(project.getId()))
                        .withSelfRel())
                .add(linkTo(methodOn(ProjectController.class).getAllProjects(page, size))
                        .withRel("projects"))
                .add(linkTo(methodOn(ProjectController.class).getProjectTasks(
                        project.getId(), page, size))
                        .withRel("tasks"));
    }

    private static Function<Task, EntityModel<Task>> taskToEntityModel(Integer page, Integer size) {
        return task -> EntityModel.of(task)
                .add(linkTo(methodOn(ProjectController.class).getProjectTask(task.getProjectId(), task.getId()))
                        .withSelfRel())
                .add(linkTo(methodOn(ProjectController.class).getAllProjects(page, size))
                        .withRel("projects"))
                .add(linkTo(methodOn(ProjectController.class).getProjectTasks(task.getProjectId(), page, size))
                        .withRel("tasks"));
    }

}
