package com.example.reactive.rest.mapper;

import com.example.reactive.rest.dto.ProjectCreationDto;
import com.example.reactive.rest.model.Project;

public class ProjectMapper {

    public Project toEntity(ProjectCreationDto projectCreationDto) {
        return Project.builder()
                .name(projectCreationDto.getName())
                .build();
    }

}
