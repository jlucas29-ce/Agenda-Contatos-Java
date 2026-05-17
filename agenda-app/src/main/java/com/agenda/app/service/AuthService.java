package com.agenda.app.service;

import com.agenda.app.dto.AuthResponse;
import com.agenda.app.dto.LoginRequest;
import com.agenda.app.dto.RegisterRequest;
import com.agenda.app.exception.UsernameAlreadyExistsException;
import com.agenda.app.model.Usuario;
import com.agenda.app.repository.UsuarioRepository;
import com.agenda.app.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        Usuario usuario = Usuario.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role("ROLE_USER")
                .build();

        usuarioRepository.save(usuario);
        log.info("Novo usuário registrado: {}", usuario.getUsername());

        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token, usuario.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow();

        String token = jwtService.generateToken(usuario);
        log.info("Login bem-sucedido: {}", usuario.getUsername());
        return new AuthResponse(token, usuario.getUsername());
    }
}
