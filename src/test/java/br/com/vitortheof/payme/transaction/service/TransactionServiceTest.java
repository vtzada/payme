package br.com.vitortheof.payme.transaction.service;

import br.com.vitortheof.payme.shared.event.AccountBalanceChangedEvent;
import br.com.vitortheof.payme.transaction.application.TransactionService;
import br.com.vitortheof.payme.transaction.application.dto.TransactionRequestDTO;
import br.com.vitortheof.payme.transaction.application.dto.TransactionResponseDTO;
import br.com.vitortheof.payme.transaction.application.mapper.TransactionMapper;
import br.com.vitortheof.payme.transaction.domain.Transaction;
import br.com.vitortheof.payme.transaction.domain.enums.TransactionType;
import br.com.vitortheof.payme.transaction.infrastructure.TransactionRepository;
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

    @Test
    @DisplayName("Deve publicar evento com valor positivo para RECEITA")
    void deveCreditarSaldoParaReceita() {

        UUID accountId = UUID.randomUUID();

        var request = new TransactionRequestDTO(
                accountId, TransactionType.RECEITA, new BigDecimal("100.00"),
                LocalDateTime.now(), "Salário", null, null);

        var savedTransaction = Transaction.builder().id(UUID.randomUUID()).accountId(accountId).build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toResponse(savedTransaction)).thenReturn(TransactionResponseDTO.builder().build());

        transactionService.create(request);

        ArgumentCaptor<AccountBalanceChangedEvent> captor = ArgumentCaptor.forClass(AccountBalanceChangedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        AccountBalanceChangedEvent event = captor.getValue();
        assertEquals(accountId, event.accountId());
        assertEquals(0, new BigDecimal("100.00").compareTo(event.amount()));
    }

    @Test
    @DisplayName("Deve publicar evento com valor negativo para DESPESA")
    void deveDebitarSaldoParaDespesa() {

        UUID accountId = UUID.randomUUID();

        var request = new TransactionRequestDTO(
                accountId, TransactionType.DESPESA, new BigDecimal("50.00"),
                LocalDateTime.now(), "Mercado", null, null);

        var savedTransaction = Transaction.builder().id(UUID.randomUUID()).accountId(accountId).build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toResponse(savedTransaction)).thenReturn(TransactionResponseDTO.builder().build());

        transactionService.create(request);

        ArgumentCaptor<AccountBalanceChangedEvent> captor = ArgumentCaptor.forClass(AccountBalanceChangedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        assertEquals(0, new BigDecimal("-50.00").compareTo(captor.getValue().amount()));
    }

    @Test
    @DisplayName("Deve publicar dois eventos (débito na origem e crédito no destino) para transfêrencia")
    void devePublicarDoisEventosParaTransferencia() {
        UUID origem = UUID.randomUUID();
        UUID destino = UUID.randomUUID();
        var request = new TransactionRequestDTO(
                origem, TransactionType.TRANSFERENCIA, new BigDecimal("300.00"),
                LocalDateTime.now(), "Nubank -> Santander", null, destino
        );
        var savedTransaction = Transaction.builder().id(UUID.randomUUID()).accountId(origem).build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toResponse(savedTransaction)).thenReturn(TransactionResponseDTO.builder().build());

        transactionService.create(request);

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
        UUID accountId = UUID.randomUUID();
        var request = new TransactionRequestDTO(
                accountId, TransactionType.TRANSFERENCIA, new BigDecimal("100.00"),
                LocalDateTime.now(), "Transferência", null, accountId);

        assertThrows(IllegalArgumentException.class, () -> transactionService.create(request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar transações de uma conta")
    void deveListarTransacoesPorAccount() {
        UUID accountId = UUID.randomUUID();
        var transaction = Transaction.builder().id(UUID.randomUUID()).accountId(accountId).build();

        when(transactionRepository.findAllByAccountId(accountId)).thenReturn(List.of(transaction));
        when(transactionMapper.toResponse(transaction))
                .thenReturn(TransactionResponseDTO.builder().id(transaction.getId()).build());

        var result = transactionService.findAllByAccountId(accountId);

        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findAllByAccountId(accountId);
    }



}
