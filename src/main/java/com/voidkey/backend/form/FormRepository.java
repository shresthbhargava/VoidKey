package com.voidkey.backend.form;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface FormRepository extends JpaRepository<Form, Long> {

    List<Form> findByOwnerId(Long ownerId);

    @Query("SELECT f FROM Form f LEFT JOIN FETCH f.questions q WHERE f.id = :id ORDER BY q.orderIndex ASC")
    Optional<Form> findByIdWithQuestions(@Param("id") Long id);
}