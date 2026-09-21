package br.com.vitortheof.payme.account.application.dto;

import br.com.vitortheof.payme.account.domain.enums.AccountType;
import br.com.vitortheof.payme.account.domain.enums.SyncType;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponseDTO(
        UUID id,
        UUID customerId,
        String name,
        BigDecimal balance,
        AccountType accountType,
        SyncType syncType
) {
}
