package com.agenda.app.repository;

import com.agenda.app.model.Contato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContatoRepository extends JpaRepository<Contato, Long> {

    /**
     * Lista paginada de contatos filtrada pelo ID do usuário dono.
     */
    Page<Contato> findAllByUsuarioId(Long usuarioId, Pageable pageable);

    /**
     * Busca um contato específico garantindo que pertença ao usuário.
     */
    Optional<Contato> findByIdAndUsuarioId(Long id, Long usuarioId);
}
