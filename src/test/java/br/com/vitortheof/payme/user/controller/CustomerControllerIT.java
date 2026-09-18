package br.com.vitortheof.payme.user.controller;

import br.com.vitortheof.payme.AbstractIntegrationTest;
import br.com.vitortheof.payme.user.application.dto.CustomerRequestDTO;
import br.com.vitortheof.payme.user.domain.Customer;
import br.com.vitortheof.payme.user.infrastructure.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar 201 ao registrar um cliente")
    void shouldReturn201WhenRegisteringCustomer() throws Exception {
        var request = new CustomerRequestDTO("Vitor", "vitor@gmail.com", "12345678");

        mockMvc.perform(post("/customer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Vitor"))
                .andExpect(jsonPath("$.email").value("vitor@gmail.com"));
    }

    @Test
    @DisplayName("Deve retornar 400 BAD REQUEST se tentar cadastrar e-mail duplicado via API")
    void shouldReturn400WhenEmailAlreadyExists() throws Exception {
        var existingCustomer = Customer.builder()
                .name("Teste")
                .email("duplicado@gmail.com")
                .password("outrasenha")
                .build();
        customerRepository.save(existingCustomer);

        var request = new CustomerRequestDTO("Outro nome", "duplicado@gmail.com", "123456");

        mockMvc.perform(post("/customer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}