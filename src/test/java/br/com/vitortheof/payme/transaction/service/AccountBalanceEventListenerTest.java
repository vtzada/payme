package br.com.vitortheof.payme.transaction.service;

import br.com.vitortheof.payme.account.application.event.AccountBalanceEventListener;
import br.com.vitortheof.payme.account.domain.Account;
import br.com.vitortheof.payme.account.infrastructure.AccountRepository;
import br.com.vitortheof.payme.shared.event.AccountBalanceChangedEvent;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountBalanceEventListenerTest {

    @InjectMocks
    private AccountBalanceEventListener listener;

    @Mock
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Deve somar o valor do evento ao saldo atual da conta")
    void deveCreditarSaldoDaConta() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder().id(accountId).balance(new BigDecimal("1000.00")).build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        listener.handle(new AccountBalanceChangedEvent(accountId, new BigDecimal("500.00")));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("1500.00").compareTo(captor.getValue().getBalance()));
    }

    @Test
    @DisplayName("Deve subtrair do saldo quando o valor do evento é negativo")
    void deveDebitarSaldoDaConta() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder().id(accountId).balance(new BigDecimal("1000.00")).build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        listener.handle(new AccountBalanceChangedEvent(accountId, new BigDecimal("-300.00")));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("700.00").compareTo(captor.getValue().getBalance()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando a conta do evento não existir")
    void deveLancarExcecaoQuandoContaNaoExiste() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> listener.handle(new AccountBalanceChangedEvent(accountId, BigDecimal.TEN)));

        verify(accountRepository, never()).save(any());
    }
}
