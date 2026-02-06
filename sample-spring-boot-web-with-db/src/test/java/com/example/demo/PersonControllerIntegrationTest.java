package com.example.demo;

import com.example.demo.entity.Person;
import com.example.demo.repository.PersonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class PersonControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
    }

    @Test
    void shouldCreatePerson() throws Exception {
        Person person = new Person("John", "Doe", "john.doe@example.com",
                LocalDate.of(1990, 1, 15), "+1234567890", "123 Main St");

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(person)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-01-15"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.address").value("123 Main St"));
    }

    @Test
    void shouldGetAllPersons() throws Exception {
        Person person1 = new Person("John", "Doe", "john.doe@example.com",
                LocalDate.of(1990, 1, 15), "+1234567890", "123 Main St");
        Person person2 = new Person("Jane", "Smith", "jane.smith@example.com",
                LocalDate.of(1992, 5, 20), "+9876543210", "456 Oak Ave");

        personRepository.save(person1);
        personRepository.save(person2);

        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    void shouldGetPersonById() throws Exception {
        Person person = new Person("John", "Doe", "john.doe@example.com",
                LocalDate.of(1990, 1, 15), "+1234567890", "123 Main St");
        Person savedPerson = personRepository.save(person);

        mockMvc.perform(get("/api/persons/{id}", savedPerson.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPerson.getId()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void shouldReturnNotFoundForInvalidPersonId() throws Exception {
        mockMvc.perform(get("/api/persons/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdatePerson() throws Exception {
        Person person = new Person("John", "Doe", "john.doe@example.com",
                LocalDate.of(1990, 1, 15), "+1234567890", "123 Main St");
        Person savedPerson = personRepository.save(person);

        Person updatedPerson = new Person("John", "Smith", "john.smith@example.com",
                LocalDate.of(1990, 1, 15), "+9999999999", "789 New St");

        mockMvc.perform(put("/api/persons/{id}", savedPerson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPerson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPerson.getId()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("john.smith@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+9999999999"))
                .andExpect(jsonPath("$.address").value("789 New St"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentPerson() throws Exception {
        Person person = new Person("John", "Doe", "john.doe@example.com",
                LocalDate.of(1990, 1, 15), "+1234567890", "123 Main St");

        mockMvc.perform(put("/api/persons/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(person)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePerson() throws Exception {
        Person person = new Person("John", "Doe", "john.doe@example.com",
                LocalDate.of(1990, 1, 15), "+1234567890", "123 Main St");
        Person savedPerson = personRepository.save(person);

        mockMvc.perform(delete("/api/persons/{id}", savedPerson.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/persons/{id}", savedPerson.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentPerson() throws Exception {
        mockMvc.perform(delete("/api/persons/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidatePersonCreation() throws Exception {
        Person invalidPerson = new Person("", "", "invalid-email",
                LocalDate.now().plusDays(1), "+1234567890", "123 Main St");

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPerson)))
                .andExpect(status().isBadRequest());
    }
}
