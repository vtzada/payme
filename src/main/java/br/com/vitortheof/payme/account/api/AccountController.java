package br.com.vitortheof.payme.account.api;

import br.com.vitortheof.payme.account.application.AccountService;
import br.com.vitortheof.payme.account.application.dto.AccountRequestDTO;
import br.com.vitortheof.payme.account.application.dto.AccountResponseDTO;
import br.com.vitortheof.payme.user.domain.Customer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDTO> create(@Valid @RequestBody AccountRequestDTO request, @AuthenticationPrincipal Customer customerLogado) {
        AccountResponseDTO response = accountService.create(request, customerLogado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> listMyAccounts(
            @AuthenticationPrincipal Customer customerLogado) {
        List<AccountResponseDTO> response = accountService.findAllByCustomerId(customerLogado.getId());
        return ResponseEntity.ok(response);
    }
}
