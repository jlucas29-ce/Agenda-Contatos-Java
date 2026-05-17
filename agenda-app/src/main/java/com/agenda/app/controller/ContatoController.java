package com.agenda.app.controller;

import com.agenda.app.dto.ContatoRequest;
import com.agenda.app.dto.ContatoResponse;
import com.agenda.app.model.Usuario;
import com.agenda.app.service.ContatoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contatos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Contatos", description = "CRUD de contatos do usuário autenticado (requer Bearer Token)")
public class ContatoController {

    private final ContatoService contatoService;

    @PostMapping
    @Operation(
            summary = "Criar contato",
            description = "Cria um novo contato associado ao usuário logado.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Contato criado"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
            }
    )
    public ResponseEntity<ContatoResponse> criar(
            @Valid @RequestBody ContatoRequest request,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contatoService.criar(request, usuarioLogado));
    }

    @GetMapping
    @Operation(
            summary = "Listar contatos (paginado)",
            description = "Lista todos os contatos do usuário logado com suporte a paginação e ordenação.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
            }
    )
    public ResponseEntity<Page<ContatoResponse>> listar(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @Parameter(description = "Parâmetros de paginação: page, size, sort")
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(contatoService.listar(usuarioLogado, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar contato por ID",
            description = "Retorna um contato específico. Retorna 403 se o contato não pertencer ao usuário logado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contato encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acesso negado"),
                    @ApiResponse(responseCode = "404", description = "Contato não encontrado")
            }
    )
    public ResponseEntity<ContatoResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(contatoService.buscarPorId(id, usuarioLogado));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar contato",
            description = "Atualiza todos os campos de um contato existente.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contato atualizado"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acesso negado"),
                    @ApiResponse(responseCode = "404", description = "Contato não encontrado")
            }
    )
    public ResponseEntity<ContatoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ContatoRequest request,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(contatoService.atualizar(id, request, usuarioLogado));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir contato",
            description = "Remove permanentemente um contato do usuário logado.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Contato excluído"),
                    @ApiResponse(responseCode = "403", description = "Acesso negado"),
                    @ApiResponse(responseCode = "404", description = "Contato não encontrado")
            }
    )
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        contatoService.excluir(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
