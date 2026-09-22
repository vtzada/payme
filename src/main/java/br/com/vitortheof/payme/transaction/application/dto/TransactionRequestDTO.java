package br.com.vitortheof.payme.transaction.application.dto;

import br.com.vitortheof.payme.transaction.domain.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionRequestDTO(
        @NotNull(message = "Ter uma conta é obrigatório")
        UUID accountId,
        @NotNull(message = "Tipo é obrigatório")
        TransactionType type,
        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal amount,
        @NotNull(message = "Data é obrigatório")
        LocalDateTime date,
        @NotNull(message = "Descrição é obrigatório")
        String description,
        UUID categoryId,
        UUID destinationAccountId
) {
}
