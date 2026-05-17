package com.agenda.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Payload para criação ou atualização de contato")
public record ContatoRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
        @Schema(example = "Maria Souza")
        String nome,

        @Schema(example = "Rua das Flores, 123, São Paulo - SP")
        String endereco,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        @Schema(example = "maria@email.com")
        String email,

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "^\\+?[0-9\\s\\-().]{6,20}$", message = "Formato de telefone inválido")
        @Schema(example = "+55 11 91234-5678")
        String telefone

) {}
