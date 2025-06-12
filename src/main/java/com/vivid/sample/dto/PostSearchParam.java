package com.vivid.sample.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class PostSearchParam {
    @Parameter(description = "검색 키워드 (제목 또는 내용)")
    private String keyword;
}
