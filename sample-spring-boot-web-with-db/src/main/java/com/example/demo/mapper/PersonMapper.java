package com.example.demo.mapper;

import com.example.demo.dto.PersonDTO;
import com.example.demo.entity.Person;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PersonMapper {

    public PersonDTO toDTO(Person person) {
        if (person == null) {
            return null;
        }
        return new PersonDTO(
                person.getId(),
                person.getFirstName(),
                person.getLastName(),
                person.getEmail(),
                person.getDateOfBirth(),
                person.getPhoneNumber(),
                person.getAddress()
        );
    }

    public Person toEntity(PersonDTO dto) {
        if (dto == null) {
            return null;
        }
        Person person = new Person();
        person.setId(dto.getId());
        person.setFirstName(dto.getFirstName());
        person.setLastName(dto.getLastName());
        person.setEmail(dto.getEmail());
        person.setDateOfBirth(dto.getDateOfBirth());
        person.setPhoneNumber(dto.getPhoneNumber());
        person.setAddress(dto.getAddress());
        return person;
    }

    public List<PersonDTO> toDTOList(List<Person> persons) {
        if (persons == null) {
            return null;
        }
        return persons.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(PersonDTO dto, Person person) {
        if (dto == null || person == null) {
            return;
        }
        person.setFirstName(dto.getFirstName());
        person.setLastName(dto.getLastName());
        person.setEmail(dto.getEmail());
        person.setDateOfBirth(dto.getDateOfBirth());
        person.setPhoneNumber(dto.getPhoneNumber());
        person.setAddress(dto.getAddress());
    }
}
