package com.portfolio.ecommerce.dto;
import jakarta.validation.constraints.*;
public record AddCartItemRequest(@NotNull Long productId,@Min(1) int quantity) {}
