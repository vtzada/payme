package br.com.vitortheof.payme.transaction.api;

import br.com.vitortheof.payme.transaction.application.TransactionService;
import br.com.vitortheof.payme.transaction.application.dto.TransactionRequestDTO;
import br.com.vitortheof.payme.transaction.application.dto.TransactionResponseDTO;
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
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> create(@Valid @RequestBody TransactionRequestDTO request, @AuthenticationPrincipal Customer customerLogado) {
        TransactionResponseDTO response = transactionService.create(request, customerLogado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponseDTO>> listByAccount(@PathVariable UUID accountId, @AuthenticationPrincipal Customer customerLogado) {
        List<TransactionResponseDTO> response = transactionService.findAllByAccountId(accountId, customerLogado.getId());
        return ResponseEntity.ok(response);
    }
}
