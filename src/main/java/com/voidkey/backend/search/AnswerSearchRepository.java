package com.voidkey.backend.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface AnswerSearchRepository extends ElasticsearchRepository<AnswerDocument, String> {
    List<AnswerDocument> findByFormIdAndValueContaining(Long formId, String query);
}