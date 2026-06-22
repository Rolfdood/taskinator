package com.taskinator.taskinator.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeEmailRequest(
    @NotNull @NotBlank @Email String newEmail
) {

}
