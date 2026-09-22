package br.com.vitortheof.payme.transaction.api;

import br.com.vitortheof.payme.transaction.application.TransactionService;
import br.com.vitortheof.payme.transaction.application.dto.TransactionRequestDTO;
import br.com.vitortheof.payme.transaction.application.dto.TransactionResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> create(@Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO response = transactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponseDTO>> listByAccount(@PathVariable UUID accountId) {
        List<TransactionResponseDTO> response = transactionService.findAllByAccountId(accountId);
        return ResponseEntity.ok(response);
    }
}
