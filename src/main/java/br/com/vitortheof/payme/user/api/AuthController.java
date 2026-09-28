package br.com.vitortheof.payme.user.api;

import br.com.vitortheof.payme.user.application.auth.TokenService;
import br.com.vitortheof.payme.user.application.dto.AuthRequestDTO;
import br.com.vitortheof.payme.user.application.dto.AuthResponseDTO;
import br.com.vitortheof.payme.user.domain.Customer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        var auth = authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((Customer) auth.getPrincipal());
        return ResponseEntity.ok(new AuthResponseDTO(token));
    }


}
