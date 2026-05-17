package com.agenda.app.service;

import com.agenda.app.dto.ContatoRequest;
import com.agenda.app.dto.ContatoResponse;
import com.agenda.app.exception.ForbiddenAccessException;
import com.agenda.app.exception.ResourceNotFoundException;
import com.agenda.app.model.Contato;
import com.agenda.app.model.Usuario;
import com.agenda.app.repository.ContatoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContatoService {

    private final ContatoRepository contatoRepository;

    // ---- Mapeamento Entidade → DTO ----
    private ContatoResponse toResponse(Contato contato) {
        return new ContatoResponse(
                contato.getId(),
                contato.getNome(),
                contato.getEndereco(),
                contato.getEmail(),
                contato.getTelefone(),
                contato.getCreatedAt(),
                contato.getUpdatedAt()
        );
    }

    // ---- CREATE ----
    @Transactional
    public ContatoResponse criar(ContatoRequest request, Usuario usuarioLogado) {
        Contato contato = Contato.builder()
                .nome(request.nome())
                .endereco(request.endereco())
                .email(request.email())
                .telefone(request.telefone())
                .usuario(usuarioLogado)
                .build();

        Contato salvo = contatoRepository.save(contato);
        log.info("Contato criado [id={}] para usuário [{}]", salvo.getId(), usuarioLogado.getUsername());
        return toResponse(salvo);
    }

    // ---- LIST (paginado) ----
    @Transactional(readOnly = true)
    public Page<ContatoResponse> listar(Usuario usuarioLogado, Pageable pageable) {
        return contatoRepository
                .findAllByUsuarioId(usuarioLogado.getId(), pageable)
                .map(this::toResponse);
    }

    // ---- GET BY ID ----
    @Transactional(readOnly = true)
    public ContatoResponse buscarPorId(Long id, Usuario usuarioLogado) {
        Contato contato = contatoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado com id: " + id));

        if (!contato.getUsuario().getId().equals(usuarioLogado.getId())) {
            throw new ForbiddenAccessException("Você não tem permissão para acessar este contato.");
        }

        return toResponse(contato);
    }

    // ---- UPDATE ----
    @Transactional
    public ContatoResponse atualizar(Long id, ContatoRequest request, Usuario usuarioLogado) {
        Contato contato = contatoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado com id: " + id));

        if (!contato.getUsuario().getId().equals(usuarioLogado.getId())) {
            throw new ForbiddenAccessException("Você não tem permissão para editar este contato.");
        }

        contato.setNome(request.nome());
        contato.setEndereco(request.endereco());
        contato.setEmail(request.email());
        contato.setTelefone(request.telefone());

        Contato atualizado = contatoRepository.save(contato);
        log.info("Contato atualizado [id={}] por usuário [{}]", id, usuarioLogado.getUsername());
        return toResponse(atualizado);
    }

    // ---- DELETE ----
    @Transactional
    public void excluir(Long id, Usuario usuarioLogado) {
        Contato contato = contatoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado com id: " + id));

        if (!contato.getUsuario().getId().equals(usuarioLogado.getId())) {
            throw new ForbiddenAccessException("Você não tem permissão para excluir este contato.");
        }

        contatoRepository.delete(contato);
        log.info("Contato excluído [id={}] por usuário [{}]", id, usuarioLogado.getUsername());
    }
}
