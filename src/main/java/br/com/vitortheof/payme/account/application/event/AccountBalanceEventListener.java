package br.com.vitortheof.payme.account.application.event;

import br.com.vitortheof.payme.account.domain.Account;
import br.com.vitortheof.payme.account.infrastructure.AccountRepository;
import br.com.vitortheof.payme.shared.event.AccountBalanceChangedEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class AccountBalanceEventListener {

    private static final int MAX_RETRIES = 3;

    private final AccountRepository accountRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(AccountBalanceChangedEvent event) {
        ObjectOptimisticLockingFailureException lastConflict = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                apply(event);
                return;
            } catch (ObjectOptimisticLockingFailureException ex) {
                lastConflict = ex;
            }
        }
        throw lastConflict;
    }

    private void apply(AccountBalanceChangedEvent event) {
        Account account = accountRepository.findById(event.accountId())
                .orElseThrow(() -> new EntityNotFoundException("A conta não foi encontrada"));

        account.setBalance(account.getBalance().add(event.amount()));
        accountRepository.save(account);
    }
}
