package br.com.vitortheof.payme.account.application.event;

import br.com.vitortheof.payme.account.domain.Account;
import br.com.vitortheof.payme.account.infrastructure.AccountRepository;
import br.com.vitortheof.payme.shared.event.AccountBalanceChangedEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class AccountBalanceEventListener {

    private final AccountRepository accountRepository;

    @EventListener
    @Transactional
    public void handle(AccountBalanceChangedEvent event) {
        Account account = accountRepository.findById(event.accountId())
                .orElseThrow(() -> new EntityNotFoundException("A conta não foi encontrada"));

        account.setBalance(account.getBalance().add(event.amount()));
        accountRepository.save(account);
    }
}
