package com.voidkey.backend.form;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubmittedResponseRepository extends JpaRepository<SubmittedResponse, Long> {

    @Query("SELECT r FROM SubmittedResponse r LEFT JOIN FETCH r.answers WHERE r.form.id = :formId ORDER BY r.submittedAt DESC")
    List<SubmittedResponse> findByFormIdWithAnswers(@Param("formId") Long formId);
}