package br.com.vitortheof.payme.transaction.application.dto;

import br.com.vitortheof.payme.transaction.domain.enums.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TransactionResponseDTO(
        UUID id,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        LocalDateTime date,
        String description,
        UUID categoryId,
        UUID destinationAccountId
) {
}
