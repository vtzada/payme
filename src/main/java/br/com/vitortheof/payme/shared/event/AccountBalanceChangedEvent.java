package br.com.vitortheof.payme.shared.event;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceChangedEvent(
        UUID accountId,
        BigDecimal amount
) {
}
