package com.voidkey.backend.form;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormRepository extends JpaRepository<Form, Long> {

    List<Form> findByOwnerId(Long ownerId);
}