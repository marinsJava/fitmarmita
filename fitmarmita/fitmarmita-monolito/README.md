# FitMarmita API

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-migrations-red?logo=flyway&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-compose-2496ED?logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?logo=swagger&logoColor=black)

> API REST de **delivery de marmitas fitness** desenvolvida em **Java 21 + Spring Boot 3**, com autenticação **stateless via JWT**, **PostgreSQL + Flyway**, documentação **OpenAPI/Swagger** e arquitetura modular *package-by-feature*.

Backend do produto **FitMarmita**: um serviço de assinatura/compra de marmitas saudáveis com cardápio semanal, informações nutricionais completas (calorias e macros) e filtro por tags de dieta (low carb, vegana, sem lactose etc.).

---

## 📑 Índice

- [Sobre o projeto](#-sobre-o-projeto)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Modelo de domínio](#-modelo-de-domínio)
- [Segurança e autenticação](#-segurança-e-autenticação)
- [Endpoints da API](#-endpoints-da-api)
- [Como executar](#-como-executar)
- [Variáveis de ambiente](#-variáveis-de-ambiente)
- [Exemplos de requisição](#-exemplos-de-requisição)
- [Roadmap](#-roadmap)
- [Aprendizados e destaques técnicos](#-aprendizados-e-destaques-técnicos)
- [Autor](#-autor)

---

## 💡 Sobre o projeto

O **FitMarmita** é o backend de um e-commerce de marmitas fitness. O sistema permite:

- **Cadastro público de usuários** com validação de dados e senha criptografada (BCrypt);
- **Login com emissão de JWT** — token assinado (HMAC-SHA) com expiração configurável;
- Consulta pública do **cardápio semanal**, com filtro opcional por **tags** de dieta;
- Consulta de **detalhes de uma marmita** — preço, descrição, calorias e macros (proteínas, carboidratos e gorduras);
- Controle de disponibilidade (marmitas **esgotadas** e **ativas**) por semana de referência;
- Base preparada para o módulo de **pedidos** (schema já versionado via Flyway).

O projeto nasceu de um ciclo completo de engenharia ágil: User Stories, critérios de aceitação em BDD, Kanban e estimativas por story points — o código implementa essas histórias.

---

## 🛠 Tecnologias

| Camada          | Tecnologia                                          |
| --------------- | ---------------------------------------------------- |
| Linguagem       | Java 21 (records, pattern matching)                  |
| Framework       | Spring Boot 3.3.4 (Web, Data JPA, Security, Validation, Actuator) |
| Segurança       | Spring Security + JWT (jjwt 0.12)                    |
| Banco de dados  | PostgreSQL 16 (Docker) · H2 para testes              |
| Migrations      | Flyway (schema versionado em SQL)                    |
| Documentação    | springdoc-openapi (Swagger UI)                       |
| Produtividade   | Lombok, Spring DevTools                              |
| Infraestrutura  | Docker Compose (Postgres com healthcheck e volume)   |
| Build           | Maven (wrapper `./mvnw` incluso)                     |

---

## 🏗 Arquitetura

O projeto usa organização **package-by-feature**: cada módulo de negócio agrupa suas próprias camadas (controller → service → repository → entity → dto), facilitando coesão e evolução independente:

```
br.com.fitmarmita/
├── auth/        →  Login, JwtService e JwtAuthenticationFilter
├── usuario/     →  Cadastro e gestão de usuários
├── cardapio/    →  Cardápio semanal, marmitas e tags
├── pedido/      →  (em desenvolvimento — schema já criado)
├── config/      →  SecurityConfig, CORS, OpenAPI e UserDetailsService
└── shared/      →  Exceções de negócio e handler global de erros
```

Dentro de cada feature, o fluxo segue camadas clássicas:

```
controller  →  Camada REST: recebe requisições, retorna DTOs
   ↓
service     →  Regras de negócio (ex.: e-mail duplicado, marmita esgotada)
   ↓
repository  →  Acesso a dados (Spring Data JPA)
   ↓
entity      →  Mapeamento objeto-relacional validado contra o schema Flyway
```

- **`shared/exception`** — `GlobalExceptionHandler` (`@RestControllerAdvice`) padroniza respostas de erro para `BusinessException`, `NotFoundException` e `DuplicateResourceException`;
- **`ddl-auto: validate`** — o Hibernate **não** cria o schema; quem manda é o **Flyway** (`V001__schema_inicial.sql`, `V002__marmita_teste.sql`), garantindo ambiente reproduzível.

---

## 🗂 Modelo de domínio

```
Usuario ───< Pedido ───< ItemPedido >─── Marmita >───< Tag
                                            │
                                     semana_referencia
```

- **Usuario** — nome, e-mail único, senha com hash BCrypt, telefone, flag de ativo e campos de **proteção contra força bruta** (`tentativas_login`, `bloqueado_ate`);
- **Marmita** — nome, descrição, preço, **informações nutricionais** (calorias, proteínas, carboidratos, gorduras), imagem, flags `esgotada`/`ativa` e `semana_referencia` (indexada) para montar o cardápio da semana;
- **Tag** — categorias de dieta com nome e slug únicos (ex.: `low-carb`, `vegana`), relacionadas às marmitas em N:N;
- **Pedido** — status, valor total, forma de pagamento, endereço de entrega e id de pagamento externo (schema pronto, módulo em desenvolvimento).

---

## 🔐 Segurança e autenticação

Autenticação **stateless** com JWT:

1. O usuário faz login em `/api/v1/auth/login` com e-mail e senha;
2. O `JwtService` emite um token **HMAC-SHA** contendo o subject e a expiração (24h por padrão);
3. Em cada requisição, o `JwtAuthenticationFilter` (um `OncePerRequestFilter`) valida o token do header `Authorization: Bearer <token>` e popula o `SecurityContextHolder`;
4. Sem sessão no servidor: `SessionCreationPolicy.STATELESS`.

**Regras de acesso:**

| Recurso                              | Acesso        |
| ------------------------------------ | ------------- |
| `POST /api/v1/usuarios` (cadastro)   | Público       |
| `POST /api/v1/auth/login`            | Público       |
| `GET /api/v1/cardapio/semanal`       | Público       |
| Swagger UI e Actuator health         | Público       |
| Demais endpoints                     | Autenticado (JWT) |

**CORS** configurável para o front-end (padrão: `http://localhost:5173`, Vite/React).

---

## 📡 Endpoints da API

### Autenticação `/api/v1/auth`

| Método | Rota                  | Acesso  | Descrição                        |
| ------ | --------------------- | ------- | --------------------------------- |
| `POST` | `/api/v1/auth/login`  | Público | Autentica e retorna o token JWT   |

### Usuários `/api/v1/usuarios`

| Método | Rota                | Acesso  | Descrição                                    |
| ------ | ------------------- | ------- | --------------------------------------------- |
| `POST` | `/api/v1/usuarios`  | Público | Cadastra usuário (nome, e-mail, senha, telefone) |

### Cardápio `/api/v1`

| Método | Rota                        | Acesso  | Descrição                                          |
| ------ | --------------------------- | ------- | --------------------------------------------------- |
| `GET`  | `/api/v1/cardapio/semanal`  | Público | Cardápio da semana (filtro opcional `?tags=low-carb,vegana`) |
| `GET`  | `/api/v1/marmitas/{id}`     | JWT     | Detalhes de uma marmita (preço, calorias, macros)   |

📖 **Documentação interativa:** com a aplicação rodando, acesse o Swagger UI em `http://localhost:8080/swagger-ui.html`.

---

## ▶ Como executar

### Pré-requisitos

- Java 21+
- Docker e Docker Compose
- Maven (ou use o wrapper `./mvnw` incluso)

### Passos

```bash
# clonar o repositório
git clone https://github.com/marinsJava/FitMarmita-api.git
cd FitMarmita-api

# subir o PostgreSQL 16
docker compose up -d

# executar a aplicação (o Flyway aplica as migrations automaticamente)
./mvnw spring-boot:run
```

Serviços disponíveis:

| Serviço      | URL                                          |
| ------------ | -------------------------------------------- |
| API          | `http://localhost:8080`                      |
| Swagger UI   | `http://localhost:8080/swagger-ui.html`      |
| Health check | `http://localhost:8080/actuator/health`      |
| PostgreSQL   | `localhost:5432` (db/user: `fitmarmita`)     |

### Testes

```bash
./mvnw test
```

---

## ⚙ Variáveis de ambiente

Todas possuem valores padrão para desenvolvimento local:

| Variável       | Padrão                                          | Descrição                        |
| -------------- | ------------------------------------------------ | --------------------------------- |
| `DB_URL`       | `jdbc:postgresql://localhost:5432/fitmarmita`    | URL do banco                      |
| `DB_USER`      | `fitmarmita`                                     | Usuário do banco                  |
| `DB_PASSWORD`  | `dev_password`                                   | Senha do banco                    |
| `JWT_SECRET`   | *(default no `application.yml`)*                 | Chave HMAC do JWT (mín. 32 chars) |

> ⚠️ Em produção, **sempre** sobrescreva `JWT_SECRET` e `DB_PASSWORD`.

---

## 🧪 Exemplos de requisição

### 1. Cadastrar um usuário

```bash
curl -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
        "nome": "Maria Silva",
        "email": "maria@email.com",
        "senha": "senhaForte123",
        "telefone": "21999998888"
      }'
```

> Validações via **Bean Validation**: nome com mínimo de 3 caracteres, e-mail válido e senha com mínimo de 8. E-mail duplicado retorna erro padronizado pelo handler global.

### 2. Fazer login e obter o token

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
        "email": "maria@email.com",
        "senha": "senhaForte123"
      }'
```

Resposta (resumida):

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer"
}
```

### 3. Consultar o cardápio da semana (público, com filtro por tags)

```bash
curl "http://localhost:8080/api/v1/cardapio/semanal?tags=low-carb,vegana"
```

### 4. Detalhar uma marmita (requer JWT)

```bash
curl http://localhost:8080/api/v1/marmitas/1 \
  -H "Authorization: Bearer <SEU_TOKEN>"
```

---

## 🗺 Roadmap

- [x] Cadastro de usuários com validação e BCrypt
- [x] Autenticação JWT stateless
- [x] Cardápio semanal com filtro por tags
- [x] Detalhamento de marmita com informações nutricionais
- [x] Schema completo versionado com Flyway (incluindo pedidos)
- [ ] Módulo de **pedidos** (criação, itens e cálculo de total)
- [ ] Integração com gateway de **pagamento**
- [ ] Bloqueio automático por tentativas de login excedidas
- [ ] Perfis de acesso (cliente × administrador do cardápio)

---

## 🎯 Aprendizados e destaques técnicos

Este projeto consolida conceitos importantes de back-end moderno com Spring:

- ✅ **JWT stateless do zero** com jjwt 0.12: emissão, validação e filtro customizado (`OncePerRequestFilter`);
- ✅ **Spring Security 6** com `SecurityFilterChain`, regras por rota/método HTTP e política de sessão stateless;
- ✅ **Flyway como fonte da verdade do schema** — Hibernate em modo `validate`, migrations SQL versionadas;
- ✅ **PostgreSQL em Docker** com healthcheck e volume persistente, ambiente idêntico em qualquer máquina;
- ✅ **Package-by-feature** — módulos coesos e independentes, prontos para escalar;
- ✅ **Tratamento global de exceções** com `@RestControllerAdvice` e exceções de negócio semânticas;
- ✅ **DTOs como records** com Bean Validation, isolando as entidades da API;
- ✅ **OpenAPI/Swagger** para documentação viva e testável;
- ✅ **Actuator** para observabilidade (health check pronto para orquestradores);
- ✅ Projeto guiado por **User Stories e critérios de aceitação BDD** (metodologia ágil aplicada de ponta a ponta).

---

## ✍ Autor

**Marins** · [GitHub @marinsJava](https://github.com/marinsJava)

---

⭐ Se este projeto te ajudou ou te interessou, deixe uma estrela!
