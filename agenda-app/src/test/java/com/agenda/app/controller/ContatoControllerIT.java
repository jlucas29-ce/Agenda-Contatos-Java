package com.agenda.app.controller;

import com.agenda.app.model.Contato;
import com.agenda.app.model.Usuario;
import com.agenda.app.repository.ContatoRepository;
import com.agenda.app.repository.UsuarioRepository;
import com.agenda.app.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ContatoController — Testes de Integração (MockMvc)")
class ContatoControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ContatoRepository contatoRepository;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    private String tokenPrincipal;
    private String tokenOutro;
    private Usuario usuarioPrincipal;
    private Long contatoCriadoId;

    @BeforeEach
    void setUp() {
        contatoRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuarioPrincipal = usuarioRepository.save(Usuario.builder()
                .username("joaoteste")
                .password(passwordEncoder.encode("senha123"))
                .role("ROLE_USER")
                .build());

        Usuario usuarioOutro = usuarioRepository.save(Usuario.builder()
                .username("mariateste")
                .password(passwordEncoder.encode("senha123"))
                .role("ROLE_USER")
                .build());

        tokenPrincipal = "Bearer " + jwtService.generateToken(usuarioPrincipal);
        tokenOutro     = "Bearer " + jwtService.generateToken(usuarioOutro);
    }

    // =============================================
    // POST /api/contatos — Criar contato
    // =============================================
    @Test
    @Order(1)
    @DisplayName("POST /api/contatos — deve criar contato com status 201")
    void deveCriarContatoComSucesso() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Ana Lima",
                "email", "ana@email.com",
                "telefone", "11977778888",
                "endereco", "Av. Brasil, 500"
        ));

        MvcResult result = mockMvc.perform(post("/api/contatos")
                        .header("Authorization", tokenPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Ana Lima"))
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        contatoCriadoId = objectMapper.readTree(responseBody).get("id").asLong();
    }

    @Test
    @DisplayName("POST /api/contatos — deve retornar 401 sem token JWT")
    void deveRetornar401SemToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Sem Token",
                "email", "sem@token.com",
                "telefone", "11911112222"
        ));

        mockMvc.perform(post("/api/contatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/contatos — deve retornar 400 quando dados inválidos")
    void deveRetornar400QuandoDadosInvalidos() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "A",         // muito curto
                "email", "invalido", // formato errado
                "telefone", ""       // obrigatório
        ));

        mockMvc.perform(post("/api/contatos")
                        .header("Authorization", tokenPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").exists())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.nome").exists());
    }

    // =============================================
    // GET /api/contatos — Listar paginado
    // =============================================
    @Test
    @DisplayName("GET /api/contatos — deve listar apenas contatos do usuário logado")
    void deveListarContatosDoUsuarioLogado() throws Exception {
        // Salva um contato para o usuarioPrincipal e outro para usuarioOutro
        contatoRepository.save(Contato.builder()
                .nome("Contato Joao").email("c@j.com").telefone("111")
                .usuario(usuarioPrincipal).build());

        mockMvc.perform(get("/api/contatos")
                        .header("Authorization", tokenPrincipal)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome").value("Contato Joao"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // =============================================
    // GET /api/contatos/{id} — Buscar por ID
    // =============================================
    @Test
    @DisplayName("GET /api/contatos/{id} — deve retornar 403 ao acessar contato de outro usuário")
    void deveRetornar403AoAcessarContatoDeOutroUsuario() throws Exception {
        Contato contato = contatoRepository.save(Contato.builder()
                .nome("Contato Joao").email("c@j.com").telefone("111")
                .usuario(usuarioPrincipal).build());

        mockMvc.perform(get("/api/contatos/{id}", contato.getId())
                        .header("Authorization", tokenOutro))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/contatos/{id} — deve retornar 404 para contato inexistente")
    void deveRetornar404ParaContatoInexistente() throws Exception {
        mockMvc.perform(get("/api/contatos/{id}", 9999L)
                        .header("Authorization", tokenPrincipal))
                .andExpect(status().isNotFound());
    }

    // =============================================
    // PUT /api/contatos/{id} — Atualizar
    // =============================================
    @Test
    @DisplayName("PUT /api/contatos/{id} — deve atualizar contato com sucesso")
    void deveAtualizarContatoComSucesso() throws Exception {
        Contato contato = contatoRepository.save(Contato.builder()
                .nome("Nome Original").email("orig@email.com").telefone("111")
                .usuario(usuarioPrincipal).build());

        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Nome Atualizado",
                "email", "novo@email.com",
                "telefone", "11988887777",
                "endereco", "Rua Nova, 10"
        ));

        mockMvc.perform(put("/api/contatos/{id}", contato.getId())
                        .header("Authorization", tokenPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"))
                .andExpect(jsonPath("$.email").value("novo@email.com"));
    }

    // =============================================
    // DELETE /api/contatos/{id} — Excluir
    // =============================================
    @Test
    @DisplayName("DELETE /api/contatos/{id} — deve excluir contato com status 204")
    void deveExcluirContatoComSucesso() throws Exception {
        Contato contato = contatoRepository.save(Contato.builder()
                .nome("Para Excluir").email("del@email.com").telefone("000")
                .usuario(usuarioPrincipal).build());

        mockMvc.perform(delete("/api/contatos/{id}", contato.getId())
                        .header("Authorization", tokenPrincipal))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/contatos/{id} — deve retornar 403 ao excluir contato de outro usuário")
    void deveRetornar403AoExcluirContatoAlheio() throws Exception {
        Contato contato = contatoRepository.save(Contato.builder()
                .nome("Protegido").email("prot@email.com").telefone("999")
                .usuario(usuarioPrincipal).build());

        mockMvc.perform(delete("/api/contatos/{id}", contato.getId())
                        .header("Authorization", tokenOutro))
                .andExpect(status().isForbidden());
    }

    // =============================================
    // AUTH endpoints
    // =============================================
    @Test
    @DisplayName("POST /api/auth/register — deve registrar novo usuário e retornar token")
    void deveRegistrarUsuario() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", "novousuario",
                "password", "senha999"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("novousuario"));
    }

    @Test
    @DisplayName("POST /api/auth/login — deve fazer login com credenciais válidas")
    void deveFazerLoginComSucesso() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", "joaoteste",
                "password", "senha123"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("joaoteste"));
    }

    @Test
    @DisplayName("POST /api/auth/login — deve retornar 401 com senha incorreta")
    void deveRetornar401ComSenhaErrada() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", "joaoteste",
                "password", "senhaerrada"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
