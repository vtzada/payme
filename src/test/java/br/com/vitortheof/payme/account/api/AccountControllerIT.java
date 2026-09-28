package br.com.vitortheof.payme.account.api;

import br.com.vitortheof.payme.shared.AbstractIntegrationTest;
import br.com.vitortheof.payme.account.application.dto.AccountRequestDTO;
import br.com.vitortheof.payme.account.domain.enums.AccountType;
import br.com.vitortheof.payme.account.domain.enums.SyncType;
import br.com.vitortheof.payme.account.infrastructure.AccountRepository;
import br.com.vitortheof.payme.transaction.infrastructure.TransactionRepository;
import br.com.vitortheof.payme.user.application.auth.TokenService;
import br.com.vitortheof.payme.user.domain.Customer;
import br.com.vitortheof.payme.user.infrastructure.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TokenService tokenService;

    private Customer customerLogado;
    private String token;

    @BeforeEach
    void cleanUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();

        customerLogado = customerRepository.save(Customer.builder()
                .name("Teste")
                .email(UUID.randomUUID().toString() + "@gmail.com")
                .password("outrasenha")
                .build());
        token = tokenService.generateToken(customerLogado);
    }

    @Test
    void deveCriarContaComSucesso() throws Exception {
        AccountRequestDTO request = new AccountRequestDTO(
                "Conta corrente",
                new BigDecimal("1000.00"),
                AccountType.CORRENTE,
                SyncType.MANUAL
        );

        mockMvc.perform(post("/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Conta corrente")))
                .andExpect(jsonPath("$.balance", is(1000.00)));
    }

    @Test
    void deveListarContasPorCustomerId() throws Exception {
        AccountRequestDTO conta1 = new AccountRequestDTO(
                "Conta 1", new BigDecimal("500.00"), AccountType.CORRENTE, SyncType.MANUAL);
        AccountRequestDTO conta2 = new AccountRequestDTO(
                "Conta 2", new BigDecimal("2000.00"), AccountType.POUPANCA, SyncType.MANUAL);

        mockMvc.perform(post("/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(conta1))
        ).andExpect(status().isCreated());
        mockMvc.perform(post("/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(conta2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Conta 1", "Conta 2")));
    }

    @Test
    void deveRetornarListaVaziaQuandoCustomerNaoTemContas() throws Exception {
        mockMvc.perform(get("/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveRetornar401QuandoSemToken() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveIsolarContasEntreCustomers() throws Exception {
        AccountRequestDTO conta = new AccountRequestDTO(
                "Conta do logado", new BigDecimal("500.00"), AccountType.CORRENTE, SyncType.MANUAL);
        mockMvc.perform(post("/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(conta)))
                .andExpect(status().isCreated());

        Customer outro = customerRepository.save(Customer.builder()
                .name("Outro")
                .email(UUID.randomUUID() + "@gmail.com")
                .password("outrasenha")
                .build());
        String outroToken = tokenService.generateToken(outro);

        mockMvc.perform(get("/accounts")
                        .header("Authorization", "Bearer " + outroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
