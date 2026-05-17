-- =============================================
-- V1 - Criação das Tabelas: usuarios e contatos
-- =============================================

CREATE TABLE IF NOT EXISTS usuarios (
    id         BIGSERIAL    PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(30)  NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);

-- -----------------------------------------------

CREATE TABLE IF NOT EXISTS contatos (
    id         BIGSERIAL     PRIMARY KEY,
    nome       VARCHAR(100)  NOT NULL,
    endereco   VARCHAR(255),
    email      VARCHAR(150)  NOT NULL,
    telefone   VARCHAR(20)   NOT NULL,
    usuario_id BIGINT        NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_contatos_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_contatos_usuario_id ON contatos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_contatos_email      ON contatos(email);
