package com.example.sample;

import com.example.sample.entity.Person;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class SampleApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
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
    private ObjectMapper objectMapper;

    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = new Person();
        testPerson.setFirstName("John");
        testPerson.setLastName("Doe");
        testPerson.setEmail("john.doe@example.com");
        testPerson.setPhoneNumber("+1234567890");
        testPerson.setDateOfBirth(LocalDate.of(1990, 1, 15));
        testPerson.setAddress("123 Main St");
        testPerson.setCity("New York");
        testPerson.setCountry("USA");
        testPerson.setPostalCode("10001");
    }

    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
    }

    @Test
    void testCreatePerson() throws Exception {
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.city").value("New York"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testGetAllPersons() throws Exception {
        // Create a person first
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isCreated());

        // Get all persons
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].firstName").exists());
    }

    @Test
    void testGetPersonById() throws Exception {
        // Create a person first
        String response = mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Person createdPerson = objectMapper.readValue(response, Person.class);

        // Get person by ID
        mockMvc.perform(get("/api/persons/" + createdPerson.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdPerson.getId()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void testGetPersonByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/persons/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdatePerson() throws Exception {
        // Create a person first
        String response = mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Person createdPerson = objectMapper.readValue(response, Person.class);

        // Update the person
        createdPerson.setFirstName("Jane");
        createdPerson.setCity("Los Angeles");

        mockMvc.perform(put("/api/persons/" + createdPerson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdPerson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.city").value("Los Angeles"));
    }

    @Test
    void testUpdatePersonNotFound() throws Exception {
        mockMvc.perform(put("/api/persons/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletePerson() throws Exception {
        // Create a person first
        String response = mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Person createdPerson = objectMapper.readValue(response, Person.class);

        // Delete the person
        mockMvc.perform(delete("/api/persons/" + createdPerson.getId()))
                .andExpect(status().isNoContent());

        // Verify person is deleted
        mockMvc.perform(get("/api/persons/" + createdPerson.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletePersonNotFound() throws Exception {
        mockMvc.perform(delete("/api/persons/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindByEmail() throws Exception {
        // Create a person first
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isCreated());

        // Search by email
        mockMvc.perform(get("/api/persons/search/email")
                        .param("email", "john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void testFindByEmailNotFound() throws Exception {
        mockMvc.perform(get("/api/persons/search/email")
                        .param("email", "nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindByLastName() throws Exception {
        // Create a person first
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isCreated());

        // Search by last name
        mockMvc.perform(get("/api/persons/search/lastname")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].lastName", containsStringIgnoringCase("Doe")));
    }

    @Test
    void testFindByCity() throws Exception {
        // Create a person first
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isCreated());

        // Search by city
        mockMvc.perform(get("/api/persons/search/city")
                        .param("city", "New York"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].city").value("New York"));
    }

    @Test
    void testCreatePersonWithoutRequiredFields() throws Exception {
        Person invalidPerson = new Person();
        invalidPerson.setEmail("test@example.com");

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPerson)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreatePersonWithInvalidEmail() throws Exception {
        testPerson.setEmail("invalid-email");

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateDuplicateEmail() throws Exception {
        // Create first person
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isCreated());

        // Try to create another person with same email
        Person duplicatePerson = new Person();
        duplicatePerson.setFirstName("Jane");
        duplicatePerson.setLastName("Smith");
        duplicatePerson.setEmail("john.doe@example.com");

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicatePerson)))
                .andExpect(status().isConflict());
    }
}
