package com.agenda.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload de login")
public record LoginRequest(

        @NotBlank(message = "Username é obrigatório")
        @Schema(example = "joaosilva")
        String username,

        @NotBlank(message = "Senha é obrigatória")
        @Schema(example = "senha123")
        String password

) {}
