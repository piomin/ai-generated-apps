package com.example.sample.repository;

import com.example.sample.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByEmail(String email);

    List<Person> findByLastNameContainingIgnoreCase(String lastName);

    List<Person> findByFirstNameContainingIgnoreCase(String firstName);

    List<Person> findByCityIgnoreCase(String city);

    List<Person> findByCountryIgnoreCase(String country);

    boolean existsByEmail(String email);
}
