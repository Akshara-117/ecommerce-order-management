package com.portfolio.ecommerce.dto;

import jakarta.validation.constraints.*;
public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email,
                              @NotBlank @Size(min = 8, max = 72) String password) {}
