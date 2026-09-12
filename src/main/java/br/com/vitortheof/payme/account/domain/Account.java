package br.com.vitortheof.payme.account.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {

    private UUID id;

    private UUID userId;

    private String name;

    private BigDecimal balance;

    private AccountType accountType;
    
    private SyncType syncType;

}
