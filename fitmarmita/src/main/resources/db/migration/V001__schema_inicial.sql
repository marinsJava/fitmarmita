-- ============================================================
-- FITMARMITA - SEED COMPLETO
-- ============================================================


-- ============================================================
-- USUARIO
-- ============================================================

CREATE TABLE usuario (
                         id                BIGSERIAL PRIMARY KEY,
                         nome              VARCHAR(100) NOT NULL,
                         email             VARCHAR(150) NOT NULL UNIQUE,
                         senha_hash        VARCHAR(255) NOT NULL,
                         telefone          VARCHAR(20),
                         ativo             BOOLEAN NOT NULL DEFAULT TRUE,
                         tentativas_login  INT NOT NULL DEFAULT 0,
                         bloqueado_ate     TIMESTAMP,
                         created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuario_email ON usuario(email);


-- ============================================================
-- TAG
-- ============================================================

CREATE TABLE tag (
                     id    BIGSERIAL PRIMARY KEY,
                     nome  VARCHAR(50) NOT NULL UNIQUE,
                     slug  VARCHAR(50) NOT NULL UNIQUE
);


-- ============================================================
-- MARMITA
-- ============================================================

CREATE TABLE marmita (
                         id                  BIGSERIAL PRIMARY KEY,
                         nome                VARCHAR(150) NOT NULL,
                         descricao           TEXT,
                         preco               DECIMAL(10,2) NOT NULL,
                         calorias            INT NOT NULL,
                         proteinas_g         DECIMAL(6,2),
                         carboidratos_g      DECIMAL(6,2),
                         gorduras_g          DECIMAL(6,2),
                         imagem_url           VARCHAR(500),
                         esgotada            BOOLEAN NOT NULL DEFAULT FALSE,
                         ativa               BOOLEAN NOT NULL DEFAULT TRUE,
                         semana_referencia   DATE NOT NULL,
                         created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_marmita_semana
    ON marmita(semana_referencia);


-- ============================================================
-- MARMITA_TAG
-- ============================================================

CREATE TABLE marmita_tag (
                             marmita_id  BIGINT NOT NULL
                                 REFERENCES marmita(id)
                                     ON DELETE CASCADE,

                             tag_id      BIGINT NOT NULL
                                 REFERENCES tag(id)
                                     ON DELETE CASCADE,

                             PRIMARY KEY (marmita_id, tag_id)
);


-- ============================================================
-- PEDIDO
-- ============================================================

CREATE TABLE pedido (
                        id                     BIGSERIAL PRIMARY KEY,

                        usuario_id             BIGINT NOT NULL
                            REFERENCES usuario(id),

                        status                 VARCHAR(30) NOT NULL,

                        valor_total            DECIMAL(10,2) NOT NULL,

                        forma_pagamento        VARCHAR(20),

                        endereco_entrega       TEXT NOT NULL,

                        observacoes            TEXT,

                        pagamento_id_externo   VARCHAR(100),

                        created_at             TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,

                        updated_at             TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pedido_usuario
    ON pedido(usuario_id);

CREATE INDEX idx_pedido_status
    ON pedido(status);


-- ============================================================
-- ITEM_PEDIDO
-- ============================================================

CREATE TABLE item_pedido (
                             id              BIGSERIAL PRIMARY KEY,

                             pedido_id       BIGINT NOT NULL
                                 REFERENCES pedido(id)
                                     ON DELETE CASCADE,

                             marmita_id      BIGINT NOT NULL
                                 REFERENCES marmita(id),

                             quantidade      INT NOT NULL
                                 CHECK (quantidade > 0),

                             preco_unitario  DECIMAL(10,2) NOT NULL
);


-- ============================================================
-- CARRINHO
-- ============================================================

CREATE TABLE carrinho (
                          id              BIGSERIAL PRIMARY KEY,

                          usuario_id      BIGINT NOT NULL UNIQUE
                              REFERENCES usuario(id),

                          valor_total     DECIMAL(10,2) NOT NULL
);


-- ============================================================
-- ITEM_CARRINHO
-- ============================================================

CREATE TABLE item_carrinho (
                               id              BIGSERIAL PRIMARY KEY,

                               carrinho_id     BIGINT NOT NULL
                                   REFERENCES carrinho(id)
                                       ON DELETE CASCADE,

                               marmita_id      BIGINT NOT NULL
                                   REFERENCES marmita(id),

                               quantidade      INT NOT NULL
                                   CHECK (quantidade > 0),

                               preco_unitario  DECIMAL(10,2) NOT NULL
);


-- ============================================================
-- SEED - TAGS
-- ============================================================

INSERT INTO tag (
    nome,
    slug
) VALUES
      ('Low Carb', 'low-carb'),
      ('Vegetariana', 'vegetariana'),
      ('Vegana', 'vegana'),
      ('Sem Gluten', 'sem-gluten'),
      ('Sem Lactose', 'sem-lactose'),
      ('Fitness', 'fitness');


-- ============================================================
-- SEED - MARMITA
-- ============================================================

INSERT INTO marmita (
    nome,
    descricao,
    preco,
    calorias,
    proteinas_g,
    carboidratos_g,
    gorduras_g,
    imagem_url,
    esgotada,
    ativa,
    semana_referencia
) VALUES (
             'Frango Grelhado com Batata Doce',

             'Peito de frango grelhado, batata doce assada e brocolis no vapor. Refeicao fitness balanceada, rica em proteina.',

             29.90,

             520,

             42.50,

             48.00,

             12.30,

             'https://picsum.photos/seed/frango-batata-doce/600/400',

             FALSE,

             TRUE,

             -- Segunda-feira da semana atual.
             -- Compatível com CardapioService.semanaAtual()
             date_trunc('week', CURRENT_DATE)::date
         );


-- ============================================================
-- SEED - RELACIONAMENTO MARMITA x TAG
-- ============================================================

INSERT INTO marmita_tag (
    marmita_id,
    tag_id
)
SELECT
    m.id,
    t.id
FROM marmita m
         CROSS JOIN tag t
WHERE m.nome = 'Frango Grelhado com Batata Doce'
  AND t.slug IN (
                 'fitness',
                 'sem-lactose'
    );


-- ============================================================
-- SEED - USUARIO
-- ============================================================
--
-- O ID NÃO é informado propositalmente.
-- O PostgreSQL gera automaticamente pelo BIGSERIAL.
--
-- Hash BCrypt:
-- {bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
--
-- ============================================================

INSERT INTO usuario (
    nome,
    email,
    senha_hash,
    telefone
) VALUES (
             'Usuário Teste',

             'teste@fitmarmita.com',

             '{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',

             '21999999999'
         );


-- ============================================================
-- SEED - CARRINHO
-- ============================================================
--
-- O carrinho é associado ao usuário pelo e-mail.
-- Dessa forma não dependemos de usuario.id = 1.
--
-- A marmita custa R$ 29,90.
-- Portanto, para 1 unidade:
--
-- quantidade       = 1
-- preco_unitario   = 29,90
-- valor_total      = 29,90
--
-- ============================================================

INSERT INTO carrinho (
    usuario_id,
    valor_total
)
SELECT
    u.id,
    29.90
FROM usuario u
WHERE u.email = 'teste@fitmarmita.com';


-- ============================================================
-- SEED - ITEM DO CARRINHO
-- ============================================================

INSERT INTO item_carrinho (
    carrinho_id,
    marmita_id,
    quantidade,
    preco_unitario
)
SELECT
    c.id,
    m.id,
    1,
    m.preco
FROM carrinho c
         JOIN usuario u
              ON u.id = c.usuario_id
         JOIN marmita m
              ON m.nome = 'Frango Grelhado com Batata Doce'
WHERE u.email = 'teste@fitmarmita.com';


-- ============================================================
-- FIM DO SEED
-- ============================================================