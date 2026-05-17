# 📒 Agenda de Contatos Corporativa — Production Ready (V2)

API RESTful multiusuário para gestão de contatos com autenticação JWT stateless.

## 🏗️ Stack Tecnológica

| Tecnologia | Versão | Papel |
|---|---|---|
| Java | 17 | Linguagem |
| Spring Boot | 3.2.x | Framework principal |
| Spring Security + JWT (jjwt) | 0.12.x | Autenticação stateless |
| PostgreSQL | 15+ | Banco de dados produção |
| Flyway | 10.x | Controle de versão do schema |
| Springdoc OpenAPI | 2.5.x | Documentação Swagger |
| JUnit 5 + Mockito + MockMvc | — | Testes unitários e integração |
| Lombok | — | Redução de boilerplate |

---

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.8+
- PostgreSQL 15+ rodando localmente

### 1. Configurar banco de dados

```sql
CREATE DATABASE agenda_db;
```

### 2. Configurar credenciais

Edite `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/agenda_db
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
```

### 3. Executar a aplicação

```bash
mvn spring-boot:run
```

O Flyway criará automaticamente as tabelas `usuarios` e `contatos` na inicialização.

### 4. Executar os testes

```bash
mvn test
```

Os testes de integração usam H2 em memória (perfil `test`), sem necessidade de PostgreSQL.

---

## 📖 Documentação da API (Swagger)

Acesse: **http://localhost:8080/swagger-ui.html**

---

## 🔐 Fluxo de Autenticação

```
1. POST /api/auth/register  →  Cria usuário, retorna JWT
2. POST /api/auth/login     →  Autentica, retorna JWT
3. Endpoints /api/contatos/** precisam do header:
   Authorization: Bearer <seu_token>
```

---

## 📌 Endpoints

### Auth (públicos)

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/auth/register` | Registrar novo usuário |
| POST | `/api/auth/login` | Fazer login |

### Contatos (protegidos com Bearer Token)

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/contatos` | Criar contato |
| GET | `/api/contatos?page=0&size=10&sort=nome` | Listar (paginado) |
| GET | `/api/contatos/{id}` | Buscar por ID |
| PUT | `/api/contatos/{id}` | Atualizar contato |
| DELETE | `/api/contatos/{id}` | Excluir contato |

---

## 🧪 Cobertura de Testes

- **ContatoServiceTest** — 9 testes unitários com Mockito (criar, listar, buscar, atualizar, excluir — cenários de sucesso e falha)
- **ContatoControllerIT** — 11 testes de integração com MockMvc e Spring Security ativo

---

## 🗂️ Estrutura do Projeto

```
agenda-app/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/agenda/app/
    │   │   ├── AgendaApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   └── OpenApiConfig.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   └── ContatoController.java
    │   │   ├── dto/
    │   │   │   ├── RegisterRequest.java
    │   │   │   ├── LoginRequest.java
    │   │   │   ├── AuthResponse.java
    │   │   │   ├── ContatoRequest.java
    │   │   │   └── ContatoResponse.java
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── ForbiddenAccessException.java
    │   │   │   └── UsernameAlreadyExistsException.java
    │   │   ├── model/
    │   │   │   ├── Usuario.java
    │   │   │   └── Contato.java
    │   │   ├── repository/
    │   │   │   ├── UsuarioRepository.java
    │   │   │   └── ContatoRepository.java
    │   │   ├── security/
    │   │   │   ├── JwtService.java
    │   │   │   └── JwtAuthenticationFilter.java
    │   │   └── service/
    │   │       ├── AuthService.java
    │   │       └── ContatoService.java
    │   └── resources/
    │       ├── application.properties
    │       └── db/migration/
    │           └── V1__create_tables.sql
    └── test/
        ├── java/com/agenda/app/
        │   ├── controller/
        │   │   └── ContatoControllerIT.java
        │   └── service/
        │       └── ContatoServiceTest.java
        └── resources/
            └── application-test.properties
```
