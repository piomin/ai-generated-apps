package com.example.sample.controller;

import com.example.sample.entity.Person;
import com.example.sample.repository.PersonRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
@Tag(name = "Person", description = "Person management APIs")
public class PersonController {

    private final PersonRepository personRepository;

    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Operation(summary = "Get all persons", description = "Retrieve a list of all persons")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    @Operation(summary = "Get a person by ID", description = "Retrieve a person by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person found",
                    content = @Content(schema = @Schema(implementation = Person.class))),
            @ApiResponse(responseCode = "404", description = "Person not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Person> getPersonById(
            @Parameter(description = "ID of the person to retrieve") @PathVariable Long id) {
        return personRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new person", description = "Create a new person record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Person created successfully",
                    content = @Content(schema = @Schema(implementation = Person.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<Person> createPerson(@Valid @RequestBody Person person) {
        if (person.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        if (person.getEmail() != null && personRepository.existsByEmail(person.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Person savedPerson = personRepository.save(person);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPerson);
    }

    @Operation(summary = "Update a person", description = "Update an existing person record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person updated successfully",
                    content = @Content(schema = @Schema(implementation = Person.class))),
            @ApiResponse(responseCode = "404", description = "Person not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Person> updatePerson(
            @Parameter(description = "ID of the person to update") @PathVariable Long id,
            @Valid @RequestBody Person personDetails) {
        return personRepository.findById(id)
                .map(person -> {
                    person.setFirstName(personDetails.getFirstName());
                    person.setLastName(personDetails.getLastName());
                    person.setEmail(personDetails.getEmail());
                    person.setPhoneNumber(personDetails.getPhoneNumber());
                    person.setDateOfBirth(personDetails.getDateOfBirth());
                    person.setAddress(personDetails.getAddress());
                    person.setCity(personDetails.getCity());
                    person.setCountry(personDetails.getCountry());
                    person.setPostalCode(personDetails.getPostalCode());
                    Person updatedPerson = personRepository.save(person);
                    return ResponseEntity.ok(updatedPerson);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a person", description = "Delete a person by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Person deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Person not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(
            @Parameter(description = "ID of the person to delete") @PathVariable Long id) {
        return personRepository.findById(id)
                .map(person -> {
                    personRepository.delete(person);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Search persons by email", description = "Find a person by their email address")
    @ApiResponse(responseCode = "200", description = "Person found or not found")
    @GetMapping("/search/email")
    public ResponseEntity<Person> findByEmail(
            @Parameter(description = "Email to search for") @RequestParam String email) {
        return personRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Search persons by last name", description = "Find persons by last name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/search/lastname")
    public List<Person> findByLastName(
            @Parameter(description = "Last name to search for") @RequestParam String lastName) {
        return personRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    @Operation(summary = "Search persons by city", description = "Find persons by city (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/search/city")
    public List<Person> findByCity(
            @Parameter(description = "City to search for") @RequestParam String city) {
        return personRepository.findByCityIgnoreCase(city);
    }
}
