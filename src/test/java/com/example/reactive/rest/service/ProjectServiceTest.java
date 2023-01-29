package com.example.reactive.rest.service;

import com.example.reactive.rest.dto.ProjectCreationDto;
import com.example.reactive.rest.mapper.ProjectMapper;
import com.example.reactive.rest.model.Project;
import com.example.reactive.rest.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@ActiveProfiles("test")
class ProjectServiceTest {

    @Autowired
    private ProjectRepository projectRepository;

    private final ProjectMapper projectMapper = new ProjectMapper();

    private ProjectService projectService;

    @BeforeEach
    void setUpTest() {
        projectService = new ProjectService(projectRepository, projectMapper);
        projectRepository.deleteAll().block();
    }

    @Test
    void testWhenGetAllProjectsPageThenMustBeReturnedAllProjectsPage() {
        int amount = 10;
        int pageSize = 5;
        createProjects(amount);

        PageImpl<Project> firstPage = projectService.getAllProjects(PageRequest.of(0, pageSize)).block();
        PageImpl<Project> secondPage = projectService.getAllProjects(PageRequest.of(1, pageSize)).block();

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
    void testWhenFindByIdThenMustBeReturnedExactProject() {
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project")
                .build();
        Project savedProject = projectService.createProject(projectCreationDto).block();
        assertNotNull(savedProject, "Saved project can not be null!");

        Project foundProject = projectService.findById(savedProject.getId()).block();
        assertAll(() -> {
            assertNotNull(foundProject, "Found project can not be null!");
            assertEquals(savedProject.getId(), foundProject.getId(), "Projects must have the same id!");
            assertEquals(savedProject, foundProject, "Projects must be equal!");
        });
    }

    @Test
    void testWhenCreateProjectThenMustBeReturnedNewProjectWithId() {
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project")
                .build();
        Project savedProject = projectService.createProject(projectCreationDto).block();

        assertAll(() -> {
            assertNotNull(savedProject, "Saved project can not be null!");
            assertEquals(projectCreationDto.getName(), savedProject.getName(),
                    "Project name must be equal to project creation dto!");
        });
    }

    @Test
    void testWhenUpdateProjectThenMustBeReturnedUpdatedProject() {
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project")
                .build();
        Project savedProject = projectService.createProject(projectCreationDto).block();
        assertNotNull(savedProject, "Saved project can not be null!");

        ProjectCreationDto projectUpdateDto = ProjectCreationDto.builder()
                .name("Updated name")
                .build();
        Project updatedProject = projectService.updateProject(savedProject.getId(), projectUpdateDto).block();

        assertAll(() -> {
            assertNotNull(updatedProject, "Updated project can not be null!");
            assertEquals(savedProject.getId(), updatedProject.getId(),
                    "Saved and updated projects must have same id!");
        });
    }

    @Test
    void testWhenDeleteProjectThenItMustBeCompletelyDeleted() {
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project")
                .build();
        Project savedProject = projectService.createProject(projectCreationDto).block();
        assertNotNull(savedProject, "Saved project can not be null!");

        projectService.deleteProject(savedProject.getId()).block();

        assertNull(projectService.findById(savedProject.getId()).block(),
                "After project deletion service must return null!");
    }

    @Test
    void testWhenProjectDeletedThenMustBeReturnedFalse() {
        ProjectCreationDto projectCreationDto = ProjectCreationDto.builder()
                .name("New project")
                .build();
        Project savedProject = projectService.createProject(projectCreationDto).block();
        assertNotNull(savedProject, "Saved project can not be null!");

        projectService.deleteProject(savedProject.getId()).block();

        Boolean isProjectExist = projectService.isProjectExist(savedProject.getId()).block();

        assertNotNull(isProjectExist, "Service must not return null!");
        assertFalse(isProjectExist,
                "If project deleted then service must return false");
    }

    private void createProjects(int amount) {
        for (int i = 0; i < amount; i++) {
            projectRepository.save(Project.builder()
                            .name("Project name " + i)
                            .build())
                    .block();
        }
    }

}