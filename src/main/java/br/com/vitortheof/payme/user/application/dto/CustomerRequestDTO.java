package br.com.vitortheof.payme.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CustomerRequestDTO(
        @NotBlank(message = "O campo do nome não pode está vazio ou em branco.")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.")
        String name,
        @NotBlank(message = "O e-mail não pode estar vazio.")
        @Email(message = "Formato de e-mail inválido.")
        String email,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 20, message = "A senha deve ter entre 8 e 20 caracteres.")
        String password
) {
}
