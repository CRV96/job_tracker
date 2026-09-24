package com.jobtracker.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewProfileForm(@NotBlank @Size(max = 100) String name) {
}
