package br.com.vitortheof.payme.user.application.mapper;

import br.com.vitortheof.payme.user.application.dto.CustomerResponseDTO;
import br.com.vitortheof.payme.user.domain.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponseDTO toResponse(Customer c){
        return new CustomerResponseDTO(
                c.getId(),
                c.getName(),
                c.getEmail()
        );
    }
}
