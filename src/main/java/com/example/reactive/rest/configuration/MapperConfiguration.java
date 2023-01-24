package com.example.reactive.rest.configuration;

import com.example.reactive.rest.mapper.ProjectMapper;
import com.example.reactive.rest.mapper.TaskMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MapperConfiguration {

    @Bean
    public ProjectMapper projectMapper() {
        return new ProjectMapper();
    }

    @Bean
    public TaskMapper taskMapper() {
        return new TaskMapper();
    }

}
