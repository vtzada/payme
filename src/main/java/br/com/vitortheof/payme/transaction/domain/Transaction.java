package br.com.vitortheof.payme.transaction.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private UUID id;

    private UUID accountId;

    private TransactionType type;

    private BigDecimal amount;

    private LocalDateTime date;

    private String description;

    private UUID categoryId;

    private UUID destinationAccountId;
}
