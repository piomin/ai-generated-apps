package com.example.demo.controller;

import com.example.demo.dto.PersonDTO;
import com.example.demo.entity.Person;
import com.example.demo.mapper.PersonMapper;
import com.example.demo.repository.PersonRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
@Tag(name = "Person", description = "Person management APIs")
@SecurityRequirement(name = "bearerAuth")
public class PersonController {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonController(PersonRepository personRepository, PersonMapper personMapper) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
    }

    @Operation(summary = "Get all persons", description = "Retrieve a list of all persons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PersonDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<PersonDTO>> getAllPersons() {
        List<Person> persons = personRepository.findAll();
        List<PersonDTO> personDTOs = personMapper.toDTOList(persons);
        return ResponseEntity.ok(personDTOs);
    }

    @Operation(summary = "Get person by ID", description = "Retrieve a person by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved person",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PersonDTO.class))),
            @ApiResponse(responseCode = "404", description = "Person not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PersonDTO> getPersonById(
            @Parameter(description = "ID of the person to retrieve") @PathVariable Long id) {
        return personRepository.findById(id)
                .map(personMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new person", description = "Create a new person with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Person created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PersonDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<PersonDTO> createPerson(
            @Parameter(description = "Person object to be created") @Valid @RequestBody PersonDTO personDTO) {
        Person person = personMapper.toEntity(personDTO);
        Person savedPerson = personRepository.save(person);
        PersonDTO responseDTO = personMapper.toDTO(savedPerson);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Update a person", description = "Update an existing person by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PersonDTO.class))),
            @ApiResponse(responseCode = "404", description = "Person not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<PersonDTO> updatePerson(
            @Parameter(description = "ID of the person to update") @PathVariable Long id,
            @Parameter(description = "Updated person object") @Valid @RequestBody PersonDTO personDTO) {
        return personRepository.findById(id)
                .map(person -> {
                    personMapper.updateEntityFromDTO(personDTO, person);
                    Person updatedPerson = personRepository.save(person);
                    PersonDTO responseDTO = personMapper.toDTO(updatedPerson);
                    return ResponseEntity.ok(responseDTO);
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
