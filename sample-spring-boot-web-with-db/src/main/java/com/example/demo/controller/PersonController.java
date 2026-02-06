package com.example.demo.controller;

import com.example.demo.entity.Person;
import com.example.demo.repository.PersonRepository;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Person.class)))
    })
    @GetMapping
    public ResponseEntity<List<Person>> getAllPersons() {
        List<Person> persons = personRepository.findAll();
        return ResponseEntity.ok(persons);
    }

    @Operation(summary = "Get person by ID", description = "Retrieve a person by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved person",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Person.class))),
            @ApiResponse(responseCode = "404", description = "Person not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Person> getPersonById(
            @Parameter(description = "ID of the person to retrieve") @PathVariable Long id) {
        return personRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new person", description = "Create a new person with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Person created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Person.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<Person> createPerson(
            @Parameter(description = "Person object to be created") @Valid @RequestBody Person person) {
        Person savedPerson = personRepository.save(person);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPerson);
    }

    @Operation(summary = "Update a person", description = "Update an existing person by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Person.class))),
            @ApiResponse(responseCode = "404", description = "Person not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Person> updatePerson(
            @Parameter(description = "ID of the person to update") @PathVariable Long id,
            @Parameter(description = "Updated person object") @Valid @RequestBody Person personDetails) {
        return personRepository.findById(id)
                .map(person -> {
                    person.setFirstName(personDetails.getFirstName());
                    person.setLastName(personDetails.getLastName());
                    person.setEmail(personDetails.getEmail());
                    person.setDateOfBirth(personDetails.getDateOfBirth());
                    person.setPhoneNumber(personDetails.getPhoneNumber());
                    person.setAddress(personDetails.getAddress());
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
}
