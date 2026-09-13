package com.voidkey.backend.form;

import com.voidkey.backend.logic.LogicEvaluator.LogicNode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false)
    private String label;

    private boolean required;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "config_json", columnDefinition = "jsonb")
    private String configJson;

    @Convert(converter = LogicNodeConverter.class)
    @Column(name = "logic_json", columnDefinition = "jsonb")
    private LogicNode logicJson;
}