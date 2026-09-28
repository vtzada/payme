package br.com.vitortheof.payme.transaction.service;

import br.com.vitortheof.payme.account.domain.Account;
import br.com.vitortheof.payme.account.infrastructure.AccountRepository;
import br.com.vitortheof.payme.category.domain.Category;
import br.com.vitortheof.payme.category.infrastructure.CategoryRepository;
import br.com.vitortheof.payme.shared.event.AccountBalanceChangedEvent;
import br.com.vitortheof.payme.transaction.application.TransactionService;
import br.com.vitortheof.payme.transaction.application.dto.TransactionRequestDTO;
import br.com.vitortheof.payme.transaction.application.dto.TransactionResponseDTO;
import br.com.vitortheof.payme.transaction.application.mapper.TransactionMapper;
import br.com.vitortheof.payme.transaction.domain.Transaction;
import br.com.vitortheof.payme.transaction.domain.enums.TransactionType;
import br.com.vitortheof.payme.transaction.infrastructure.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Deve publicar evento com valor positivo para RECEITA")
    void deveCreditarSaldoParaReceita() {

        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        var request = new TransactionRequestDTO(
                accountId, TransactionType.RECEITA, new BigDecimal("100.00"),
                LocalDateTime.now(), "Salário", null, null);

        var savedTransaction = Transaction.builder().id(UUID.randomUUID()).accountId(accountId).build();

        when(accountRepository.findByIdAndCustomerId(accountId, customerId))
                .thenReturn(Optional.of(Account.builder().id(accountId).customerId(customerId).balance(new BigDecimal("1000.00")).build()));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toResponse(savedTransaction)).thenReturn(TransactionResponseDTO.builder().build());

        transactionService.create(request, customerId);

        ArgumentCaptor<AccountBalanceChangedEvent> captor = ArgumentCaptor.forClass(AccountBalanceChangedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        AccountBalanceChangedEvent event = captor.getValue();
        assertEquals(accountId, event.accountId());
        assertEquals(0, new BigDecimal("100.00").compareTo(event.amount()));
    }

    @Test
    @DisplayName("Deve publicar evento com valor negativo para DESPESA")
    void deveDebitarSaldoParaDespesa() {

        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        var request = new TransactionRequestDTO(
                accountId, TransactionType.DESPESA, new BigDecimal("50.00"),
                LocalDateTime.now(), "Mercado", null, null);

        var savedTransaction = Transaction.builder().id(UUID.randomUUID()).accountId(accountId).build();

        when(accountRepository.findByIdAndCustomerId(accountId, customerId))
                .thenReturn(Optional.of(Account.builder().id(accountId).customerId(customerId).balance(new BigDecimal("1000.00")).build()));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toResponse(savedTransaction)).thenReturn(TransactionResponseDTO.builder().build());

        transactionService.create(request, customerId);

        ArgumentCaptor<AccountBalanceChangedEvent> captor = ArgumentCaptor.forClass(AccountBalanceChangedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        assertEquals(0, new BigDecimal("-50.00").compareTo(captor.getValue().amount()));
    }

    @Test
    @DisplayName("Deve publicar dois eventos (débito na origem e crédito no destino) para transfêrencia")
    void devePublicarDoisEventosParaTransferencia() {
        UUID customerId = UUID.randomUUID();
        UUID origem = UUID.randomUUID();
        UUID destino = UUID.randomUUID();
        var request = new TransactionRequestDTO(
                origem, TransactionType.TRANSFERENCIA, new BigDecimal("300.00"),
                LocalDateTime.now(), "Nubank -> Santander", null, destino
        );
        var savedTransaction = Transaction.builder().id(UUID.randomUUID()).accountId(origem).build();

        when(accountRepository.findByIdAndCustomerId(origem, customerId))
                .thenReturn(Optional.of(Account.builder().id(origem).customerId(customerId).balance(new BigDecimal("1000.00")).build()));
        when(accountRepository.findByIdAndCustomerId(destino, customerId))
                .thenReturn(Optional.of(Account.builder().id(destino).customerId(customerId).balance(new BigDecimal("200.00")).build()));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toResponse(savedTransaction)).thenReturn(TransactionResponseDTO.builder().build());

        transactionService.create(request, customerId);

        ArgumentCaptor<AccountBalanceChangedEvent> captor = ArgumentCaptor.forClass(AccountBalanceChangedEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        List<AccountBalanceChangedEvent> events = captor.getAllValues();
        assertEquals(origem, events.get(0).accountId());
        assertEquals(0, new BigDecimal("-300.00").compareTo(events.get(0).amount()));
        assertEquals(destino, events.get(1).accountId());
        assertEquals(0, new BigDecimal("300.00").compareTo(events.get(1).amount()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando origem e destino da TRANSFERENCIA são iguais")
    void deveLancarExcecaoQuandoOrigemIgualDestino() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        var request = new TransactionRequestDTO(
                accountId, TransactionType.TRANSFERENCIA, new BigDecimal("100.00"),
                LocalDateTime.now(), "Transferência", null, accountId);

        when(accountRepository.findByIdAndCustomerId(accountId, customerId))
                .thenReturn(Optional.of(Account.builder().id(accountId).customerId(customerId).balance(new BigDecimal("1000.00")).build()));

        assertThrows(IllegalArgumentException.class, () -> transactionService.create(request, customerId));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando DESPESA supera o saldo (saldo insuficiente)")
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        var request = new TransactionRequestDTO(
                accountId, TransactionType.DESPESA, new BigDecimal("500.00"),
                LocalDateTime.now(), "Compra grande", null, null);

        when(accountRepository.findByIdAndCustomerId(accountId, customerId))
                .thenReturn(Optional.of(Account.builder().id(accountId).customerId(customerId).balance(new BigDecimal("100.00")).build()));

        assertThrows(IllegalArgumentException.class, () -> transactionService.create(request, customerId));

        verify(transactionRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando TRANSFERENCIA supera o saldo da origem")
    void deveLancarExcecaoQuandoTransferenciaSemSaldo() {
        UUID customerId = UUID.randomUUID();
        UUID origem = UUID.randomUUID();
        UUID destino = UUID.randomUUID();
        var request = new TransactionRequestDTO(
                origem, TransactionType.TRANSFERENCIA, new BigDecimal("2000.00"),
                LocalDateTime.now(), "Pix", null, destino);

        when(accountRepository.findByIdAndCustomerId(origem, customerId))
                .thenReturn(Optional.of(Account.builder().id(origem).customerId(customerId).balance(new BigDecimal("100.00")).build()));

        assertThrows(IllegalArgumentException.class, () -> transactionService.create(request, customerId));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar 404 quando categoria não pertence ao customer")
    void deveLancar404QuandoCategoriaDeOutroCustomer() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        var request = new TransactionRequestDTO(
                accountId, TransactionType.RECEITA, new BigDecimal("100.00"),
                LocalDateTime.now(), "Salário", categoryId, null);

        when(accountRepository.findByIdAndCustomerId(accountId, customerId))
                .thenReturn(Optional.of(Account.builder().id(accountId).customerId(customerId).balance(new BigDecimal("1000.00")).build()));
        when(categoryRepository.findByIdAndCustomerId(categoryId, customerId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> transactionService.create(request, customerId));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve aceitar categoria válida do próprio customer")
    void deveAceitarCategoriaValida() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        var request = new TransactionRequestDTO(
                accountId, TransactionType.RECEITA, new BigDecimal("100.00"),
                LocalDateTime.now(), "Salário", categoryId, null);

        var savedTransaction = Transaction.builder().id(UUID.randomUUID()).accountId(accountId).build();

        when(accountRepository.findByIdAndCustomerId(accountId, customerId))
                .thenReturn(Optional.of(Account.builder().id(accountId).customerId(customerId).balance(new BigDecimal("1000.00")).build()));
        when(categoryRepository.findByIdAndCustomerId(categoryId, customerId))
                .thenReturn(Optional.of(Category.builder().id(categoryId).customerId(customerId).build()));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toResponse(savedTransaction)).thenReturn(TransactionResponseDTO.builder().build());

        assertDoesNotThrow(() -> transactionService.create(request, customerId));
    }

    @Test
    @DisplayName("Deve listar transações de uma conta (incluindo recebidas como destino)")
    void deveListarTransacoesPorAccount() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        var transaction = Transaction.builder().id(UUID.randomUUID()).accountId(accountId).build();

        when(accountRepository.findByIdAndCustomerId(accountId, customerId))
                .thenReturn(Optional.of(Account.builder().id(accountId).customerId(customerId).balance(new BigDecimal("1000.00")).build()));
        when(transactionRepository.findAllByAccountIdOrDestinationAccountId(accountId, accountId)).thenReturn(List.of(transaction));
        when(transactionMapper.toResponse(transaction))
                .thenReturn(TransactionResponseDTO.builder().id(transaction.getId()).build());

        var result = transactionService.findAllByAccountId(accountId, customerId);

        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findAllByAccountIdOrDestinationAccountId(accountId, accountId);
    }



}
