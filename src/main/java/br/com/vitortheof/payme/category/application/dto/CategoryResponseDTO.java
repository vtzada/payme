package br.com.vitortheof.payme.category.application.dto;

import br.com.vitortheof.payme.transaction.domain.enums.TransactionType;

import java.util.UUID;

public record CategoryResponseDTO(
        UUID id,
        UUID customerId,
        String name,
        TransactionType type
) {
}
