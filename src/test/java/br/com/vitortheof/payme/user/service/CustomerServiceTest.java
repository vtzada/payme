package br.com.vitortheof.payme.user.service;

import br.com.vitortheof.payme.user.application.CustomerService;
import br.com.vitortheof.payme.user.application.dto.CustomerRequestDTO;
import br.com.vitortheof.payme.user.application.dto.CustomerResponseDTO;
import br.com.vitortheof.payme.user.application.exceptions.EmailAlreadyRegisteredException;
import br.com.vitortheof.payme.user.application.mapper.CustomerMapper;
import br.com.vitortheof.payme.user.domain.Customer;
import br.com.vitortheof.payme.user.infrastructure.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Test
    @DisplayName("Deve registrar com sucesso um novo cliente")
    void shouldRegisterSucessfully() {

        var request = new CustomerRequestDTO("Vitor", "vitor@gmail.com", "123456");
        var customer = Customer.builder().name("Vitor").email("vitor@gmail.com").password("123456").build();
        var responseDto = new CustomerResponseDTO(UUID.randomUUID(), "Vitor", "vitor@gmail.com");

        when(customerRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(responseDto);

        CustomerResponseDTO response = customerService.register(request);

        assertNotNull(response);
        assertEquals(request.email(), response.email());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o e-mail já estiver cadastrado.")
    void shouldThrowExceptionWhenEmailExists(){

        var request = new CustomerRequestDTO("Vitor", "vitor@email.com", "123456");
        var existingCustomer = new Customer();
        existingCustomer.setEmail("vitor@email.com");

        when(customerRepository.findByEmail(request.email())).thenReturn(Optional.of(existingCustomer));

        assertThrows(EmailAlreadyRegisteredException.class, () -> customerService.register(request));

        verify(customerRepository, never()).save(any(Customer.class));
    }

}
