package com.agenda.app.service;

import com.agenda.app.dto.ContatoRequest;
import com.agenda.app.dto.ContatoResponse;
import com.agenda.app.exception.ForbiddenAccessException;
import com.agenda.app.exception.ResourceNotFoundException;
import com.agenda.app.model.Contato;
import com.agenda.app.model.Usuario;
import com.agenda.app.repository.ContatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContatoService — Testes Unitários")
class ContatoServiceTest {

    @Mock
    private ContatoRepository contatoRepository;

    @InjectMocks
    private ContatoService contatoService;

    private Usuario usuarioPrincipal;
    private Usuario usuarioOutro;
    private Contato contatoExistente;
    private ContatoRequest contatoRequest;

    @BeforeEach
    void setUp() {
        usuarioPrincipal = Usuario.builder()
                .id(1L).username("joao").password("encoded").role("ROLE_USER").build();

        usuarioOutro = Usuario.builder()
                .id(2L).username("maria").password("encoded").role("ROLE_USER").build();

        contatoExistente = Contato.builder()
                .id(10L)
                .nome("Carlos Teste")
                .email("carlos@email.com")
                .telefone("11999999999")
                .endereco("Rua A, 1")
                .usuario(usuarioPrincipal)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        contatoRequest = new ContatoRequest(
                "Carlos Teste", "Rua A, 1", "carlos@email.com", "11999999999"
        );
    }

    // ================================================
    // CRIAR
    // ================================================
    @Nested
    @DisplayName("criar()")
    class CriarTests {

        @Test
        @DisplayName("Deve criar contato e associá-lo ao usuário logado")
        void deveCriarContatoComSucesso() {
            given(contatoRepository.save(any(Contato.class))).willReturn(contatoExistente);

            ContatoResponse response = contatoService.criar(contatoRequest, usuarioPrincipal);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.nome()).isEqualTo("Carlos Teste");
            assertThat(response.email()).isEqualTo("carlos@email.com");

            then(contatoRepository).should(times(1)).save(any(Contato.class));
        }
    }

    // ================================================
    // LISTAR
    // ================================================
    @Nested
    @DisplayName("listar()")
    class ListarTests {

        @Test
        @DisplayName("Deve retornar página de contatos do usuário logado")
        void deveRetornarPaginaDeContatos() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("nome"));
            Page<Contato> page = new PageImpl<>(List.of(contatoExistente), pageable, 1);

            given(contatoRepository.findAllByUsuarioId(usuarioPrincipal.getId(), pageable))
                    .willReturn(page);

            Page<ContatoResponse> resultado = contatoService.listar(usuarioPrincipal, pageable);

            assertThat(resultado.getTotalElements()).isEqualTo(1);
            assertThat(resultado.getContent().get(0).nome()).isEqualTo("Carlos Teste");
        }

        @Test
        @DisplayName("Deve retornar página vazia quando usuário não tem contatos")
        void deveRetornarPaginaVazia() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contato> page = Page.empty(pageable);

            given(contatoRepository.findAllByUsuarioId(usuarioPrincipal.getId(), pageable))
                    .willReturn(page);

            Page<ContatoResponse> resultado = contatoService.listar(usuarioPrincipal, pageable);

            assertThat(resultado).isEmpty();
        }
    }

    // ================================================
    // BUSCAR POR ID
    // ================================================
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorIdTests {

        @Test
        @DisplayName("Deve retornar contato quando pertence ao usuário logado")
        void deveBuscarContatoProprio() {
            given(contatoRepository.findById(10L)).willReturn(Optional.of(contatoExistente));

            ContatoResponse response = contatoService.buscarPorId(10L, usuarioPrincipal);

            assertThat(response.id()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando contato não existe")
        void deveLancarExcecaoQuandoContatoNaoExiste() {
            given(contatoRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> contatoService.buscarPorId(99L, usuarioPrincipal))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Deve lançar ForbiddenAccessException quando contato pertence a outro usuário")
        void deveLancarForbiddenQuandoContatoDeOutroUsuario() {
            given(contatoRepository.findById(10L)).willReturn(Optional.of(contatoExistente));

            assertThatThrownBy(() -> contatoService.buscarPorId(10L, usuarioOutro))
                    .isInstanceOf(ForbiddenAccessException.class);
        }
    }

    // ================================================
    // ATUALIZAR
    // ================================================
    @Nested
    @DisplayName("atualizar()")
    class AtualizarTests {

        @Test
        @DisplayName("Deve atualizar contato com sucesso")
        void deveAtualizarContatoComSucesso() {
            ContatoRequest novosDados = new ContatoRequest(
                    "Carlos Atualizado", "Rua B, 2", "novo@email.com", "11888888888"
            );

            Contato contatoAtualizado = Contato.builder()
                    .id(10L).nome("Carlos Atualizado").email("novo@email.com")
                    .telefone("11888888888").endereco("Rua B, 2")
                    .usuario(usuarioPrincipal)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();

            given(contatoRepository.findById(10L)).willReturn(Optional.of(contatoExistente));
            given(contatoRepository.save(any(Contato.class))).willReturn(contatoAtualizado);

            ContatoResponse response = contatoService.atualizar(10L, novosDados, usuarioPrincipal);

            assertThat(response.nome()).isEqualTo("Carlos Atualizado");
            assertThat(response.email()).isEqualTo("novo@email.com");
        }

        @Test
        @DisplayName("Deve lançar ForbiddenAccessException ao tentar atualizar contato de outro usuário")
        void deveLancarForbiddenAoAtualizarContatoAlheio() {
            given(contatoRepository.findById(10L)).willReturn(Optional.of(contatoExistente));

            assertThatThrownBy(() -> contatoService.atualizar(10L, contatoRequest, usuarioOutro))
                    .isInstanceOf(ForbiddenAccessException.class);
        }
    }

    // ================================================
    // EXCLUIR
    // ================================================
    @Nested
    @DisplayName("excluir()")
    class ExcluirTests {

        @Test
        @DisplayName("Deve excluir contato do usuário logado")
        void deveExcluirContatoComSucesso() {
            given(contatoRepository.findById(10L)).willReturn(Optional.of(contatoExistente));
            willDoNothing().given(contatoRepository).delete(contatoExistente);

            assertThatCode(() -> contatoService.excluir(10L, usuarioPrincipal))
                    .doesNotThrowAnyException();

            then(contatoRepository).should(times(1)).delete(contatoExistente);
        }

        @Test
        @DisplayName("Deve lançar ForbiddenAccessException ao excluir contato de outro usuário")
        void deveLancarForbiddenAoExcluirContatoAlheio() {
            given(contatoRepository.findById(10L)).willReturn(Optional.of(contatoExistente));

            assertThatThrownBy(() -> contatoService.excluir(10L, usuarioOutro))
                    .isInstanceOf(ForbiddenAccessException.class);

            then(contatoRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException ao excluir contato inexistente")
        void deveLancarNotFoundAoExcluirContatoInexistente() {
            given(contatoRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> contatoService.excluir(99L, usuarioPrincipal))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
