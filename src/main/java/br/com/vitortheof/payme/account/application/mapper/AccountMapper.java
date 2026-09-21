package br.com.vitortheof.payme.account.application.mapper;

import br.com.vitortheof.payme.account.application.dto.AccountResponseDTO;
import br.com.vitortheof.payme.account.domain.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponseDTO toResponse(Account account) {
        return new AccountResponseDTO(
                account.getId(),
                account.getCustomerId(),
                account.getName(),
                account.getBalance(),
                account.getAccountType(),
                account.getSyncType()
        );
    }
}
