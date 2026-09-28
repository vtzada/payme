package br.com.vitortheof.payme.account.service;

import br.com.vitortheof.payme.account.application.AccountService;
import br.com.vitortheof.payme.account.application.dto.AccountRequestDTO;
import br.com.vitortheof.payme.account.application.dto.AccountResponseDTO;
import br.com.vitortheof.payme.account.application.mapper.AccountMapper;
import br.com.vitortheof.payme.account.domain.Account;
import br.com.vitortheof.payme.account.domain.enums.AccountType;
import br.com.vitortheof.payme.account.domain.enums.SyncType;
import br.com.vitortheof.payme.account.infrastructure.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Test
    @DisplayName("Deve criar conta vinculada ao customerId do token")
    void deveCriarContaComCustomerDoToken() {
        UUID customerId = UUID.randomUUID();
        var request = new AccountRequestDTO("Conta corrente", new BigDecimal("1000.00"), AccountType.CORRENTE, SyncType.MANUAL);
        var saved = Account.builder().id(UUID.randomUUID()).customerId(customerId).name("Conta corrente").build();
        var response = new AccountResponseDTO(saved.getId(), customerId, "Conta corrente", new BigDecimal("1000.00"), AccountType.CORRENTE, SyncType.MANUAL);

        when(accountRepository.save(any(Account.class))).thenReturn(saved);
        when(accountMapper.toResponse(saved)).thenReturn(response);

        AccountResponseDTO result = accountService.create(request, customerId);

        assertNotNull(result);
        assertEquals(customerId, result.customerId());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Deve listar apenas contas do customer informado")
    void deveListarContasDoCustomer() {
        UUID customerId = UUID.randomUUID();
        var account = Account.builder().id(UUID.randomUUID()).customerId(customerId).name("Conta 1").build();
        var response = new AccountResponseDTO(account.getId(), customerId, "Conta 1", BigDecimal.TEN, AccountType.CORRENTE, SyncType.MANUAL);

        when(accountRepository.findAllByCustomerId(customerId)).thenReturn(List.of(account));
        when(accountMapper.toResponse(account)).thenReturn(response);

        var result = accountService.findAllByCustomerId(customerId);

        assertEquals(1, result.size());
        assertEquals("Conta 1", result.get(0).name());
        verify(accountRepository, times(1)).findAllByCustomerId(customerId);
    }
}
