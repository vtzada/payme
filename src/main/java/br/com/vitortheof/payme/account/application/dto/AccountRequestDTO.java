package br.com.vitortheof.payme.account.application.dto;

import br.com.vitortheof.payme.account.domain.enums.AccountType;
import br.com.vitortheof.payme.account.domain.enums.SyncType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountRequestDTO(
        @NotNull(message = "O ID do cliente é obrigatório")
        UUID customerId,
        @NotBlank(message = "O nome da conta é obrigatório")
        String name,
        @NotNull(message = "O saldo inicial é obrigatório")
        BigDecimal initialBalance,
        @NotNull(message = "O tipo de conta é obrigatório")
        AccountType accountType,
        @NotNull(message = "O tipo de sincronização é obrigatório.")
        SyncType syncType
) {
}
