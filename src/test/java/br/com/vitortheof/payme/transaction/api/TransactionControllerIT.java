package br.com.vitortheof.payme.transaction.api;

import br.com.vitortheof.payme.account.domain.Account;
import br.com.vitortheof.payme.account.domain.enums.AccountType;
import br.com.vitortheof.payme.account.domain.enums.SyncType;
import br.com.vitortheof.payme.account.infrastructure.AccountRepository;
import br.com.vitortheof.payme.shared.AbstractIntegrationTest;
import br.com.vitortheof.payme.transaction.application.dto.TransactionRequestDTO;
import br.com.vitortheof.payme.transaction.domain.enums.TransactionType;
import br.com.vitortheof.payme.transaction.infrastructure.TransactionRepository;
import br.com.vitortheof.payme.user.domain.Customer;
import br.com.vitortheof.payme.user.infrastructure.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerIT extends AbstractIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar uma transação de RECEITA e creditar o saldo da conta")
    void deveCriarReceitaECreditarSaldo() throws Exception {
        UUID accountId = criarConta(new BigDecimal("1000.00"));

        var request = new TransactionRequestDTO(
                accountId, TransactionType.RECEITA, new BigDecimal("500.00"),
                LocalDateTime.now(), "Salário", null, null);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.description", is("Salário")));

        Account account = accountRepository.findById(accountId).orElseThrow();
        assertEquals(0, new BigDecimal("1500.00").compareTo(account.getBalance()));
    }

    @Test
    @DisplayName("Deve criar uma transação de DESPESA e debitar o saldo da conta")
    void deveCriarContaEDebitarSaldo() throws Exception {
        UUID accountId = criarConta(new BigDecimal("1000.00"));

        var request = new TransactionRequestDTO(
                accountId, TransactionType.DESPESA, new BigDecimal("300.00"),
                LocalDateTime.now(), "Mercado", null, null);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Account account = accountRepository.findById(accountId).orElseThrow();
        assertEquals(0, new BigDecimal("700.00").compareTo(account.getBalance()));


    }

    @Test
    @DisplayName("Deve transferir valor entre duas contas debitando origem e creditando destino")
    void deveTransferirEntreContas() throws Exception {
        UUID origem = criarConta(new BigDecimal("1000.00"));
        UUID destino = criarConta(new BigDecimal("200.00"));

        var request = new TransactionRequestDTO(
                origem, TransactionType.TRANSFERENCIA, new BigDecimal("400.00"),
                LocalDateTime.now(), "Nubank -> Santander", null, destino);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertEquals(0, new BigDecimal("600.00").compareTo(accountRepository.findById(origem).orElseThrow().getBalance()));
        assertEquals(0, new BigDecimal("600.00").compareTo(accountRepository.findById(destino).orElseThrow().getBalance()));
    }

    @Test
    @DisplayName("Deve retornar 400 ao transferir sem informar conta de destino")
    void deveRetornar400QuandoTransferenciaSemDestino() throws Exception {
        UUID accountId = criarConta(new BigDecimal("1000.00"));

        var request = new TransactionRequestDTO(
                accountId, TransactionType.TRANSFERENCIA, new BigDecimal("100.00"),
                LocalDateTime.now(), "Transferência inválida", null, null);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando amount não é positivo")
    void deveRetornar400QuandoAmountNaoPositivo() throws Exception {
        UUID accountId = criarConta(new BigDecimal("1000.00"));

        var request = new TransactionRequestDTO(
                accountId, TransactionType.DESPESA, new BigDecimal("-50.00"),
                LocalDateTime.now(), "Valor inválido", null, null);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve listar transações de uma conta")
    void deveListarTransacoesPorConta() throws Exception {
        UUID accountId = criarConta(new BigDecimal("1000.00"));

        var t1 = new TransactionRequestDTO(accountId, TransactionType.RECEITA, new BigDecimal("100.00"),
                LocalDateTime.now(), "Receita 1", null, null);
        var t2 = new TransactionRequestDTO(accountId, TransactionType.DESPESA, new BigDecimal("50.00"),
                LocalDateTime.now(), "Despesa 1", null, null);

        mockMvc.perform(post("/transactions").contentType("application/json").content(objectMapper.writeValueAsString(t1)));
        mockMvc.perform(post("/transactions").contentType("application/json").content(objectMapper.writeValueAsString(t2)));

        mockMvc.perform(get("/transactions/account/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder("Receita 1", "Despesa 1")));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando conta não tem transações")
    void deveRetornarListaVaziaQuandoContaSemTransacoes() throws Exception {
        UUID accountId = criarConta(new BigDecimal("1000.00"));

        mockMvc.perform(get("/transactions/account/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private UUID criarConta(BigDecimal saldoInicial) {
        Customer customer = Customer.builder()
                .name("Teste")
                .email(UUID.randomUUID() + "@gmail.com")
                .password("senha123")
                .build();
        UUID customerId = customerRepository.save(customer).getId();

        Account account = Account.builder()
                .customerId(customerId)
                .name("Conta Teste")
                .balance(saldoInicial)
                .accountType(AccountType.CORRENTE)
                .syncType(SyncType.MANUAL)
                .build();

        return accountRepository.save(account).getId();
    }
}

