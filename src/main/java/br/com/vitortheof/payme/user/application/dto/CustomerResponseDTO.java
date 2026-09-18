package br.com.vitortheof.payme.user.application.dto;

import java.util.UUID;

public record CustomerResponseDTO(
        UUID id,
        String name,
        String email
) {
}
