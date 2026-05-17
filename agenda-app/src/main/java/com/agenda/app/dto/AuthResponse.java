package com.agenda.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação com Bearer Token JWT")
public record AuthResponse(

        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,

        @Schema(example = "Bearer")
        String type,

        @Schema(example = "joaosilva")
        String username

) {
    public AuthResponse(String token, String username) {
        this(token, "Bearer", username);
    }
}
