package com.voidkey.backend.form;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    long countByFormId(Long formId);
    List<Question> findByFormIdOrderByOrderIndexAsc(Long formId);
    Optional<Question> findByIdAndFormId(Long id, Long formId);
}