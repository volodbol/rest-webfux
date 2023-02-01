package com.cucumber;

import com.example.reactive.rest.ReactiveRestApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ReactiveRestApplication.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}
