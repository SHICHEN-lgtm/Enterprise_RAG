package com.enterprise.qa.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentRequest(
        @NotBlank(message = "标题不能为空") String title,
        String category,
        @NotBlank(message = "内容不能为空") String content,
        String source
) {
}
