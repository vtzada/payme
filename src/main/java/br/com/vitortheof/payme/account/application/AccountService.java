package br.com.vitortheof.payme.account.application;

import br.com.vitortheof.payme.account.application.dto.AccountRequestDTO;
import br.com.vitortheof.payme.account.application.dto.AccountResponseDTO;
import br.com.vitortheof.payme.account.application.mapper.AccountMapper;
import br.com.vitortheof.payme.account.domain.Account;
import br.com.vitortheof.payme.account.infrastructure.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountResponseDTO create(AccountRequestDTO request) {
        Account account = Account.builder()
                .customerId(request.customerId())
                .name(request.name())
                .balance(request.initialBalance())
                .accountType(request.accountType())
                .syncType(request.syncType())
                .build();

        Account savedAccount = accountRepository.save(account);
        return accountMapper.toResponse(savedAccount);
    }

    public List<AccountResponseDTO> findAllByCustomerId(UUID customerId) {
        return accountRepository.findAllByCustomerId(customerId)
                .stream()
                .map(accountMapper::toResponse)
                .toList();
    }
}
