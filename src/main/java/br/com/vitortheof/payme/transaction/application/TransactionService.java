package br.com.vitortheof.payme.transaction.application;

import br.com.vitortheof.payme.account.domain.Account;
import br.com.vitortheof.payme.account.infrastructure.AccountRepository;
import br.com.vitortheof.payme.category.infrastructure.CategoryRepository;
import br.com.vitortheof.payme.shared.event.AccountBalanceChangedEvent;
import br.com.vitortheof.payme.transaction.application.dto.TransactionRequestDTO;
import br.com.vitortheof.payme.transaction.application.dto.TransactionResponseDTO;
import br.com.vitortheof.payme.transaction.application.mapper.TransactionMapper;
import br.com.vitortheof.payme.transaction.domain.Transaction;
import br.com.vitortheof.payme.transaction.domain.enums.TransactionType;
import br.com.vitortheof.payme.transaction.infrastructure.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransactionResponseDTO create(TransactionRequestDTO request, UUID customerId) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }

        Account origem = accountRepository.findByIdAndCustomerId(request.accountId(), customerId)
                .orElseThrow(() -> new EntityNotFoundException("Conta de origem não encontrada ou não pertence a você."));

        if (request.type() == TransactionType.DESPESA || request.type() == TransactionType.TRANSFERENCIA) {
            if (origem.getBalance() == null || origem.getBalance().compareTo(request.amount()) < 0) {
                throw new IllegalArgumentException("Saldo insuficiente para realizar a operação.");
            }
        }

        if (request.type() == TransactionType.TRANSFERENCIA) {
            if (request.destinationAccountId() == null) {
                throw new IllegalArgumentException("A conta de destino é obrigatória para transferências.");
            }
            if (request.accountId().equals(request.destinationAccountId())) {
                throw new IllegalArgumentException("A conta de origem e destino não podem ser a mesma.");
            }

            accountRepository.findByIdAndCustomerId(request.destinationAccountId(), customerId)
                    .orElseThrow(() -> new EntityNotFoundException("Conta de destino não encontrada ou não pertence a você."));
        }

        if (request.categoryId() != null) {
            categoryRepository.findByIdAndCustomerId(request.categoryId(), customerId)
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada ou não pertence a você."));
        }

        Transaction transaction = Transaction.builder()
                .accountId(request.accountId())
                .type(request.type())
                .amount(request.amount())
                .date(request.date())
                .description(request.description())
                .categoryId(request.categoryId())
                .destinationAccountId(request.destinationAccountId())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        if (request.type() == TransactionType.TRANSFERENCIA) {
            eventPublisher.publishEvent(new AccountBalanceChangedEvent(request.accountId(), request.amount().negate()));
            eventPublisher.publishEvent(new AccountBalanceChangedEvent(request.destinationAccountId(), request.amount()));
        } else {
            BigDecimal delta = resolveDelta(request.type(), request.amount());
            eventPublisher.publishEvent(new AccountBalanceChangedEvent(request.accountId(), delta));
        }
        return transactionMapper.toResponse(savedTransaction);
    }

    public List<TransactionResponseDTO> findAllByAccountId(UUID accountId, UUID customerId) {
        accountRepository.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada ou não pertence a você."));

        return transactionRepository.findAllByAccountIdOrDestinationAccountId(accountId, accountId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }


    private BigDecimal resolveDelta(TransactionType type, BigDecimal amount) {
        return switch (type) {
            case RECEITA -> amount;
            case DESPESA -> amount.negate();
            case TRANSFERENCIA ->
                    throw new IllegalStateException("Transferências não devem passar pelo resolveDelta padrão.");
        };
    }
}
