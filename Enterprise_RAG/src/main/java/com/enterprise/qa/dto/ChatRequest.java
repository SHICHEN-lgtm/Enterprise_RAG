package com.enterprise.qa.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "问题不能为空") String question
) {
}
