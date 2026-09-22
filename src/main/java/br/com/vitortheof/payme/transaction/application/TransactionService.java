package br.com.vitortheof.payme.transaction.application;

import br.com.vitortheof.payme.shared.event.AccountBalanceChangedEvent;
import br.com.vitortheof.payme.transaction.application.dto.TransactionRequestDTO;
import br.com.vitortheof.payme.transaction.application.dto.TransactionResponseDTO;
import br.com.vitortheof.payme.transaction.application.mapper.TransactionMapper;
import br.com.vitortheof.payme.transaction.domain.Transaction;
import br.com.vitortheof.payme.transaction.domain.enums.TransactionType;
import br.com.vitortheof.payme.transaction.infrastructure.TransactionRepository;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransactionResponseDTO create(TransactionRequestDTO request) {

        if (request.type() == TransactionType.TRANSFERENCIA) {
            if (request.destinationAccountId() == null) {
                throw new IllegalArgumentException("destination account id is required");
            }
            if (request.accountId().equals(request.destinationAccountId())) {
                throw new IllegalArgumentException("A conta de origem e destino não podem ser a mesma.");
            }
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

    public List<TransactionResponseDTO> findAllByAccountId(UUID accountId) {
        return transactionRepository.findAllByAccountId(accountId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }


    private BigDecimal resolveDelta(TransactionType type, BigDecimal amount) {
       return switch(type) {
           case RECEITA -> amount;
           case DESPESA -> amount.negate();
           case TRANSFERENCIA -> throw new IllegalStateException("Transferências não devem passar pelo resolveDelta padrão.");
       };
    }
}
