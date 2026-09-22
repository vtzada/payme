package br.com.vitortheof.payme.account.api;

import br.com.vitortheof.payme.account.application.AccountService;
import br.com.vitortheof.payme.account.application.dto.AccountRequestDTO;
import br.com.vitortheof.payme.account.application.dto.AccountResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDTO> create(@Valid @RequestBody AccountRequestDTO request) {
        AccountResponseDTO response = accountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponseDTO>> listByCustomer(@PathVariable UUID customerId) {
        List<AccountResponseDTO> response = accountService.findAllByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

}
