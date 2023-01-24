package com.example.reactive.rest.mapper;

import com.example.reactive.rest.dto.TaskCreationDto;
import com.example.reactive.rest.model.Task;

public class TaskMapper {

    public Task toEntity(TaskCreationDto taskCreationDto) {
        return Task.builder()
                .description(taskCreationDto.getDescription())
                .build();
    }

}
