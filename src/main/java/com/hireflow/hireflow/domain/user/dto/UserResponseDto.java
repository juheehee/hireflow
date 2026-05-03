package com.hireflow.hireflow.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hireflow.hireflow.domain.user.User;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {

    private Long id;
    private String email;
    private String name;
    private String techStack;
    private String resumeUrl;
    private String resumeParseStatus;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.techStack = user.getTechStack();
        this.resumeUrl = user.getResumeUrl();
        this.resumeParseStatus = user.getResumeParseStatus();
    }
}
