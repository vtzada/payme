package br.com.vitortheof.payme.transaction.application.mapper;

import br.com.vitortheof.payme.transaction.application.dto.TransactionResponseDTO;
import br.com.vitortheof.payme.transaction.domain.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponseDTO toResponse(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccountId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .categoryId(transaction.getCategoryId())
                .description(transaction.getDescription())
                .destinationAccountId(transaction.getDestinationAccountId())
                .build();
    }
}
