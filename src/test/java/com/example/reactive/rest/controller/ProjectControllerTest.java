package com.example.reactive.rest.controller;

import com.example.reactive.rest.IntegrationTest;
import com.example.reactive.rest.dto.ProjectCreationDto;
import com.example.reactive.rest.dto.TaskCreationDto;
import com.example.reactive.rest.model.Project;
import com.example.reactive.rest.model.Task;
import com.example.reactive.rest.service.ProjectService;
import com.example.reactive.rest.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest
@IntegrationTest
class ProjectControllerTest {

    @MockBean
    private ProjectService projectService;

    @MockBean
    private TaskService taskService;

    @Autowired
    private WebTestClient webTestClient;

    private final Clock clock = Clock.fixed(Instant.parse("2023-01-25T12:30:30.00Z"), ZoneId.systemDefault());

    @Test
    void testWhenProjectsPageRequestedThenPageMustBeReturned() {
        int amount = 4;
        when(projectService.getAllProjects(any(Pageable.class)))
                .thenReturn(Mono.just(new PageImpl<>(getProjects(amount))));

        webTestClient.get().uri("/api/v1/projects")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isNotEmpty()
                .jsonPath("$.content.length()").isEqualTo(amount)
                .jsonPath("$.content[*].links").isArray();

        verify(projectService, times(1)).getAllProjects(any(Pageable.class));
    }

    @Test
    void testWhenProjectRequestedThenIfMustBeReturned() {
        long projectId = 1;
        when(projectService.findById(projectId))
                .thenReturn(Mono.just(getProject(projectId)));

        webTestClient.get().uri("/api/v1/projects/{id}", projectId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId)
                .jsonPath("$.links").isNotEmpty();

        verify(projectService, times(1)).findById(projectId);
    }

    @Test
    void testWhenCreateProjectThenMustBeReturnedNewProject() {
        long projectId = 1;
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project name")
                .build();
        when(projectService.createProject(projectCreationDto))
                .thenReturn(Mono.just(getProject(projectId, projectCreationDto)));

        String expectedTime = LocalDateTime.now(clock).toString();
        webTestClient.post().uri("/api/v1/projects")
                .bodyValue(projectCreationDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId)
                .jsonPath("$.name").isEqualTo(projectCreationDto.getName())
                .jsonPath("$.createdAt").isEqualTo(expectedTime)
                .jsonPath("$.updatedAt").isEqualTo(expectedTime)
                .jsonPath("$.links").isNotEmpty();

        verify(projectService, times(1)).createProject(projectCreationDto);
    }

    @Test
    void testWhenUpdateProjectThenMustBeReturnedUpdatedProject() {
        long projectId = 1;
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project name")
                .build();
        when(projectService.updateProject(projectId, projectCreationDto))
                .thenReturn(Mono.just(getProject(projectId, projectCreationDto)));

        webTestClient.put().uri("/api/v1/projects/{id}", projectId)
                .bodyValue(projectCreationDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId)
                .jsonPath("$.name").isEqualTo(projectCreationDto.getName())
                .jsonPath("$.links").isNotEmpty();

        verify(projectService, times(1)).updateProject(projectId, projectCreationDto);
    }

    @Test
    void testWhenDeleteProjectThenMustBeReturnedNoContent() {
        long projectId = 1;
        when(projectService.deleteProject(projectId))
                .thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/projects/{id}", projectId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(projectService, times(1)).deleteProject(projectId);
    }

    @Test
    void testWhenTasksPageRequestedThenPageMustBeReturned() {
        long projectId = 1;
        int amount = 5;
        when(taskService.getTasks(eq(projectId), any(Pageable.class)))
                .thenReturn(Mono.just(new PageImpl<>(getTasks(amount, projectId))));

        webTestClient.get().uri("/api/v1/projects/{projectId}/tasks", projectId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isNotEmpty()
                .jsonPath("$.content.length()").isEqualTo(amount)
                .jsonPath("$.content[*].links").isArray();

        verify(taskService, times(1)).getTasks(eq(projectId), any(Pageable.class));
    }

    @Test
    void testWhenTaskRequestedThenItMustBeReturned() {
        long projectId = 1;
        long taskId = 1;
        when(taskService.getTask(projectId, taskId))
                .thenReturn(Mono.just(getTask(taskId, projectId)));

        webTestClient.get().uri("/api/v1/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(taskId)
                .jsonPath("$.projectId").isEqualTo(projectId);

        verify(taskService, times(1)).getTask(projectId, taskId);
    }

    @Test
    void testWhenCreateTaskThenNewTaskMustBeReturned() {
        long projectId = 1;
        long taskId = 1;
        TaskCreationDto taskCreationDto = TaskCreationDto.builder()
                .description("new task description")
                .build();
        when(taskService.createTask(projectId, taskCreationDto))
                .thenReturn(Mono.just(getTask(taskId, projectId, taskCreationDto)));

        String expectedTime = LocalDateTime.now(clock).toString();
        webTestClient.post().uri("/api/v1/projects/{projectId}/tasks", projectId)
                .bodyValue(taskCreationDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(taskId)
                .jsonPath("$.projectId").isEqualTo(projectId)
                .jsonPath("$.description").isEqualTo(taskCreationDto.getDescription())
                .jsonPath("$.createdAt").isEqualTo(expectedTime)
                .jsonPath("$.updatedAt").isEqualTo(expectedTime)
                .jsonPath("$.links").isNotEmpty();

        verify(taskService, times(1)).createTask(projectId, taskCreationDto);
    }

    @Test
    void testWhenUpdateTaskThenUpdatedTaskMustBeReturned() {
        long projectId = 1;
        long taskId = 1;
        TaskCreationDto taskCreationDto = TaskCreationDto.builder()
                .description("new task description")
                .build();
        when(taskService.updateTask(projectId, taskId, taskCreationDto))
                .thenReturn(Mono.just(getTask(taskId, projectId, taskCreationDto)));

        webTestClient.put().uri("/api/v1/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                .bodyValue(taskCreationDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(taskId)
                .jsonPath("$.projectId").isEqualTo(projectId)
                .jsonPath("$.description").isEqualTo(taskCreationDto.getDescription())
                .jsonPath("$.links").isNotEmpty();

        verify(taskService, times(1)).updateTask(projectId, taskId, taskCreationDto);
    }

    @Test
    void testWhenDeleteTaskThenMustBeReturnedNoContent() {
        long projectId = 1;
        long taskId = 1;
        when(taskService.deleteTask(projectId, taskId))
                .thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(taskService, times(1)).deleteTask(projectId, taskId);
    }

    private List<Task> getTasks(int amount, long projectId) {
        ArrayList<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= amount; i++) {
            tasks.add(
                    Task.builder()
                            .id((long) i)
                            .projectId(projectId)
                            .description("Task description")
                            .createdAt(LocalDateTime.now(clock).minusHours(i))
                            .updatedAt(LocalDateTime.now(clock).plusHours(i))
                            .build()
            );
        }
        return tasks;
    }

    private Task getTask(long taskId, long projectId) {
        return Task.builder()
                .id(taskId)
                .projectId(projectId)
                .description("Description")
                .createdAt(LocalDateTime.now(clock))
                .updatedAt(LocalDateTime.now(clock))
                .build();
    }

    private Task getTask(long taskId, long projectId, TaskCreationDto taskCreationDto) {
        return Task.builder()
                .id(taskId)
                .projectId(projectId)
                .description(taskCreationDto.getDescription())
                .createdAt(LocalDateTime.now(clock))
                .updatedAt(LocalDateTime.now(clock))
                .build();
    }

    private List<Project> getProjects(int amount) {
        ArrayList<Project> projects = new ArrayList<>();
        for (int i = 1; i <= amount; i++) {
            projects.add(
                    Project.builder()
                            .id((long) i)
                            .name("Project 1")
                            .createdAt(LocalDateTime.now(clock).minusHours(i))
                            .updatedAt(LocalDateTime.now(clock).plusHours(i))
                            .build()
            );
        }
        return projects;
    }

    private Project getProject(Long id) {
        return Project.builder()
                .id(id)
                .name("Project name")
                .createdAt(LocalDateTime.now(clock))
                .updatedAt(LocalDateTime.now(clock))
                .build();
    }

    private Project getProject(Long id, ProjectCreationDto projectCreationDto) {
        return Project.builder()
                .id(id)
                .name(projectCreationDto.getName())
                .createdAt(LocalDateTime.now(clock))
                .updatedAt(LocalDateTime.now(clock))
                .build();
    }

}