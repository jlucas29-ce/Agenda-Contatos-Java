package com.agenda.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados de contato retornados pela API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContatoResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Maria Souza")
        String nome,

        @Schema(example = "Rua das Flores, 123")
        String endereco,

        @Schema(example = "maria@email.com")
        String email,

        @Schema(example = "+55 11 91234-5678")
        String telefone,

        @Schema(description = "Data de criação")
        LocalDateTime createdAt,

        @Schema(description = "Data da última atualização")
        LocalDateTime updatedAt

) {}
