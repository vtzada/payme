package br.com.vitortheof.payme.user.api;

import br.com.vitortheof.payme.user.application.CustomerService;
import br.com.vitortheof.payme.user.application.dto.CustomerRequestDTO;
import br.com.vitortheof.payme.user.application.dto.CustomerResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<CustomerResponseDTO> register(@Valid @RequestBody CustomerRequestDTO request){
        CustomerResponseDTO response = customerService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
