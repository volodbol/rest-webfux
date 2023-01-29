package com.example.reactive.rest.service;

import com.example.reactive.rest.IntegrationTest;
import com.example.reactive.rest.dto.ProjectCreationDto;
import com.example.reactive.rest.dto.TaskCreationDto;
import com.example.reactive.rest.mapper.ProjectMapper;
import com.example.reactive.rest.mapper.TaskMapper;
import com.example.reactive.rest.model.Project;
import com.example.reactive.rest.model.Task;
import com.example.reactive.rest.repository.ProjectRepository;
import com.example.reactive.rest.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@IntegrationTest
class TaskServiceTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private final TaskMapper taskMapper = new TaskMapper();

    private final ProjectMapper projectMapper = new ProjectMapper();

    private ProjectService projectService;

    private TaskService taskService;

    @BeforeEach
    void setUpTest() {
        projectService = new ProjectService(projectRepository, projectMapper);
        taskService = new TaskService(projectService, taskRepository, taskMapper);
        taskRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void testWhenGetProjectTasksPageThenMustBeReturnedProjectTasksPage() {
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project")
                .build();
        Project project = projectService.createProject(projectCreationDto).block();
        assertNotNull(project, "Saved project can not be null!");

        int amount = 10;
        int pageSize = 5;
        createTasks(project.getId(), amount);

        PageImpl<Task> firstPage = taskService.getTasks(project.getId(), PageRequest.of(0, pageSize)).block();
        PageImpl<Task> secondPage = taskService.getTasks(project.getId(), PageRequest.of(1, pageSize)).block();

        assertAll(() -> {
            assertNotNull(firstPage, "Page can not be null!");
            assertNotNull(secondPage, "Page can not be null!");
            assertEquals(amount, firstPage.getTotalElements(), "Page must have exact amount of elements!");
            assertEquals(amount, secondPage.getTotalElements(), "Page must have exact amount of elements!");
            assertEquals(pageSize, firstPage.getNumberOfElements(), "Page must have exact page size!");
            assertEquals(pageSize, secondPage.getNumberOfElements(), "Page must have exact page size!");
            assertNotEquals(firstPage, secondPage, "Pages must be different!");
        });
    }

    @Test
    void testWhenGetSavedTaskThenMustBeReturnedTask() {
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project")
                .build();
        Project project = projectService.createProject(projectCreationDto).block();
        assertNotNull(project, "Saved project can not be null!");

        TaskCreationDto taskCreationDto = TaskCreationDto.builder()
                .description("Task new")
                .build();
        Task savedTask = taskService.createTask(project.getId(), taskCreationDto).block();
        assertNotNull(savedTask, "Saved task can not be null!");

        Task foundTask = taskService.getTask(project.getId(), savedTask.getId()).block();

        assertAll(() -> {
            assertNotNull(foundTask, "Saved task can not be null!");
            assertEquals(savedTask.getId(), foundTask.getId(), "Saved and found tasks must have the same id!");
            assertEquals(savedTask.getDescription(), foundTask.getDescription(),
                    "Saved and found tasks must have equal description!");
            assertEquals(savedTask, foundTask, "Saved and found tasks must be equal!");
        });
    }

    @Test
    void testWhenCreateTaskThenTaskMustBeCreated() {
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project")
                .build();
        Project project = projectService.createProject(projectCreationDto).block();
        assertNotNull(project, "Saved project can not be null!");

        TaskCreationDto taskCreationDto = TaskCreationDto.builder()
                .description("Task new")
                .build();
        Task savedTask = taskService.createTask(project.getId(), taskCreationDto).block();

        assertAll(() -> {
            assertNotNull(savedTask, "Saved task can not be null!");
            assertEquals(taskCreationDto.getDescription(), savedTask.getDescription(),
                    "Creation dto and saved task must have equal description");
        });
    }

    @Test
    void testWhenTaskUpdatedThenUpdatedTaskMustBeReturned() {
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project")
                .build();
        Project project = projectService.createProject(projectCreationDto).block();
        assertNotNull(project, "Saved project can not be null!");

        TaskCreationDto taskCreationDto = TaskCreationDto.builder()
                .description("Task new")
                .build();
        Task savedTask = taskService.createTask(project.getId(), taskCreationDto).block();
        assertNotNull(savedTask, "Saved task can not be null!");

        TaskCreationDto taskUpdateDto = TaskCreationDto.builder()
                .description("Task description updated")
                .build();
        Task updatedTask = taskService.updateTask(project.getId(), savedTask.getId(), taskUpdateDto).block();

        assertAll(() -> {
            assertNotNull(updatedTask, "Updated task can not be null!");
            assertEquals(savedTask.getId(), updatedTask.getId(),
                    "Updated and saved task must have same id!");
            assertEquals(taskUpdateDto.getDescription(), updatedTask.getDescription(),
                    "Task update dto and updated task must have same description!");
        });
    }

    @Test
    void testWhenTaskDeletedThenItMustBeDeletedCompletely() {
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project")
                .build();
        Project project = projectService.createProject(projectCreationDto).block();
        assertNotNull(project, "Saved project can not be null!");

        TaskCreationDto taskCreationDto = TaskCreationDto.builder()
                .description("Task new")
                .build();
        Task savedTask = taskService.createTask(project.getId(), taskCreationDto).block();
        assertNotNull(savedTask, "Saved task can not be null!");

        taskService.deleteTask(project.getId(), savedTask.getId()).block();

        assertNull(taskService.getTask(project.getId(), savedTask.getId()).block(),
                "When task deleted service can not return this task");
    }

    @Test
    void testWhenProjectDoesNotExistDuringTaskSavingThenMustBeReturnedNull() {
        long nonExistentProjectId = 321;
        TaskCreationDto taskCreationDto = TaskCreationDto.builder()
                .description("Task new")
                .build();
        Task savedTask = taskService.createTask(nonExistentProjectId, taskCreationDto).block();
        assertNull(savedTask, "Saved task must be be null because project does not exist!");
    }

    @Test
    void testWhenProjectDoesNotExistDuringTaskUpdatingThenMustBeReturnedNull() {
        long nonExistentProjectId = 321;
        long nonExistentTaskId = 341;
        TaskCreationDto taskCreationDto = TaskCreationDto.builder()
                .description("Task new")
                .build();
        Task savedTask = taskService.updateTask(nonExistentProjectId, nonExistentTaskId, taskCreationDto).block();
        assertNull(savedTask, "Updated task must be be null because project does not exist!");
    }

    private void createTasks(long projectId, int amount) {
        for (int i = 0; i < amount; i++) {
            taskRepository.save(Task.builder()
                            .description("New task " + i)
                            .projectId(projectId)
                            .build())
                    .block();
        }
    }

}