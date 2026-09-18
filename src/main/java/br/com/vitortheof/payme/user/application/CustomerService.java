package br.com.vitortheof.payme.user.application;

import br.com.vitortheof.payme.user.application.exceptions.EmailAlreadyRegisteredException;
import br.com.vitortheof.payme.user.application.mapper.CustomerMapper;
import br.com.vitortheof.payme.user.domain.Customer;
import br.com.vitortheof.payme.user.application.dto.CustomerRequestDTO;
import br.com.vitortheof.payme.user.application.dto.CustomerResponseDTO;
import br.com.vitortheof.payme.user.infrastructure.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerResponseDTO register(CustomerRequestDTO request) {

        var customer = customerRepository.findByEmail(request.email());

        if (customer.isPresent()) {
            throw new EmailAlreadyRegisteredException("Este e-mail já está cadastrado.");
        }

        Customer c = Customer.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .build();

        customerRepository.save(c);
        return customerMapper.toResponse(c);
    }
}
