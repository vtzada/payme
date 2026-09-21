package br.com.vitortheof.payme.account.api;


import br.com.vitortheof.payme.account.application.AccountService;
import br.com.vitortheof.payme.account.application.dto.AccountRequestDTO;
import br.com.vitortheof.payme.account.application.dto.AccountResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    public ResponseEntity<AccountResponseDTO> create(AccountRequestDTO request) {
        AccountResponseDTO response = accountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<List<AccountResponseDTO>> listByCustomer(@PathVariable UUID customerId) {
        List<AccountResponseDTO> response = accountService.findAllByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

}
