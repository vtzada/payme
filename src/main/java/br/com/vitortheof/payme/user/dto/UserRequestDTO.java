package br.com.vitortheof.payme.user.dto;

public record UserRequestDTO(
        String name,
        String email,
        String password
) {
}
