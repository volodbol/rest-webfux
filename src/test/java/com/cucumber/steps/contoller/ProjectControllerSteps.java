package com.cucumber.steps.contoller;

import com.example.reactive.rest.dto.ProjectCreationDto;
import com.example.reactive.rest.model.Project;
import com.example.reactive.rest.repository.ProjectRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

public class ProjectControllerSteps {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private WebTestClient webTestClient;

    private WebTestClient.ResponseSpec responseSpec;

    private Long createdProjectId;

    private ProjectCreationDto projectCreationDto;

    @Given("{int} created projects")
    public void number_of_created_projects(int amount) {
        createProjects(amount);
    }

    @When("get projects page with number {int}, size {int}")
    public void get_page_with_some_number_and_size(int page, int size) {
        responseSpec = webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/v1/projects")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .exchange();
    }

    @Then("received page with number {int}, size {int}")
    public void received_page_with_proper_number_and_size(int page, int size) {
        responseSpec.expectBody()
                .jsonPath("$.content").isNotEmpty()
                .jsonPath("$.content.size()").isEqualTo(size)
                .jsonPath("$.number").isEqualTo(page)
                .jsonPath("$.content[*].links").isNotEmpty();
    }

    @And("status code is {int}")
    public void status_code_is(int statusCode) {
        responseSpec.expectStatus()
                .isEqualTo(statusCode);
    }

    @When("get project by id")
    public void get_project_with_id() {
        responseSpec = webTestClient.get().uri("/api/v1/projects/{projectId}", createdProjectId)
                .exchange();
    }

    @Then("received project by id")
    public void received_project_by_id() {
        responseSpec.expectBody()
                .jsonPath("$.id").isEqualTo(createdProjectId)
                .jsonPath("$._links").isNotEmpty();
    }

    @When("create new project")
    public void create_new_project() {
        projectCreationDto = ProjectCreationDto.builder()
                .name("Created new project")
                .build();
        responseSpec = webTestClient.post().uri("/api/v1/projects")
                .bodyValue(projectCreationDto)
                .exchange();
    }

    @Then("received created project")
    public void received_created_project() {
        responseSpec.expectBody()
                .jsonPath("$.name").isEqualTo(projectCreationDto.getName())
                .jsonPath("$._links").isNotEmpty();
    }

    @When("update created project")
    public void update_created_project() {
        projectCreationDto = ProjectCreationDto.builder()
                .name("Updated created project")
                .build();
        responseSpec = webTestClient.put().uri("/api/v1/projects/{projectId}", createdProjectId)
                .bodyValue(projectCreationDto)
                .exchange();
    }

    @Then("received updated project")
    public void received_updated_project() {
        responseSpec.expectBody()
                .jsonPath("$.id").isEqualTo(createdProjectId)
                .jsonPath("$.name").isEqualTo(projectCreationDto.getName())
                .jsonPath("$._links").isNotEmpty();
    }

    @When("delete project")
    public void delete_project() {
        responseSpec = webTestClient.delete().uri("/api/v1/projects/{projectId}", createdProjectId)
                .exchange();
    }

    @Then("received response without content")
    public void received_response_without_content() {
        responseSpec.expectBody()
                .isEmpty();
    }

    @And("project deleted")
    public void project_deleted() {
        webTestClient.get().uri("/api/v1/projects/{projectId}", createdProjectId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }



    private void createProjects(int amount) {
        Project project;
        for (int i = 0; i < amount; i++) {
            project = projectRepository.save(Project.builder()
                            .name("Project name " + i)
                            .build())
                    .block();
            if (project != null) {
                createdProjectId = project.getId();
            }
        }
    }

}
