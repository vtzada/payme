package br.com.vitortheof.payme.category.application.dto;

import br.com.vitortheof.payme.transaction.domain.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CategoryRequestDTO(
        @NotBlank(message = "O nome da categoria é obrigatório.")
        String name,
        @NotNull(message = "O tipo de categoria é obrigatório.")
        TransactionType type
) {
}
