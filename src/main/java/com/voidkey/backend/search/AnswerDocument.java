package com.voidkey.backend.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "answers")
public class AnswerDocument {

    @Id
    private String id; // the Answer's Postgres ID, as a string

    @Field(type = FieldType.Long)
    private Long formId;

    @Field(type = FieldType.Long)
    private Long questionId;

    @Field(type = FieldType.Text) // FieldType.Text enables full-text search,
    private String questionLabel; // tokenized/analyzed, not exact-match like Keyword

    @Field(type = FieldType.Text)
    private String value;

    public AnswerDocument() {}

    public AnswerDocument(String id, Long formId, Long questionId, String questionLabel, String value) {
        this.id = id;
        this.formId = formId;
        this.questionId = questionId;
        this.questionLabel = questionLabel;
        this.value = value;
    }

    public String getId() { return id; }
    public Long getFormId() { return formId; }
    public Long getQuestionId() { return questionId; }
    public String getQuestionLabel() { return questionLabel; }
    public String getValue() { return value; }
}