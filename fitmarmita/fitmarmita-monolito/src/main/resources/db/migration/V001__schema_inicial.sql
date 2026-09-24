CREATE TABLE usuarios (
    id                BIGSERIAL     PRIMARY KEY,
    nome              VARCHAR(120)  NOT NULL,
    email             VARCHAR(180)  NOT NULL,
    senha             VARCHAR(100)  NOT NULL,
    telefone          VARCHAR(20),
    ativo             BOOLEAN       NOT NULL DEFAULT TRUE,
    tentativas_login  INTEGER       NOT NULL DEFAULT 0,
    bloqueado_ate     TIMESTAMP,
    criado_em         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em     TIMESTAMP,
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);
CREATE INDEX idx_usuarios_email ON usuarios (email);

CREATE TABLE tags (
    id     BIGSERIAL     PRIMARY KEY,
    nome   VARCHAR(60)   NOT NULL,
    slug   VARCHAR(60)   NOT NULL,
    CONSTRAINT uk_tags_nome UNIQUE (nome),
    CONSTRAINT uk_tags_slug UNIQUE (slug)
);

CREATE TABLE marmitas (
    id                 BIGSERIAL      PRIMARY KEY,
    nome               VARCHAR(150)   NOT NULL,
    descricao          VARCHAR(500),
    preco              NUMERIC(10,2)  NOT NULL,
    calorias           INTEGER,
    proteinas          NUMERIC(6,2),
    carboidratos       NUMERIC(6,2),
    gorduras           NUMERIC(6,2),
    imagem_url         VARCHAR(300),
    esgotada           BOOLEAN        NOT NULL DEFAULT FALSE,
    ativa              BOOLEAN        NOT NULL DEFAULT TRUE,
    semana_referencia  DATE           NOT NULL,
    criado_em          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em      TIMESTAMP
);
CREATE INDEX idx_marmitas_semana_referencia ON marmitas (semana_referencia);

CREATE TABLE marmita_tag (
    marmita_id  BIGINT NOT NULL REFERENCES marmitas (id) ON DELETE CASCADE,
    tag_id      BIGINT NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (marmita_id, tag_id)
);

-- Schema de pedidos, criado desde o inicio do projeto (modulo ainda em
-- desenvolvimento na epoca). Removido pela V003, quando o modulo foi
-- extraido para o pedido-service.
CREATE TABLE pedidos (
    id                   BIGSERIAL      PRIMARY KEY,
    usuario_id           BIGINT         NOT NULL REFERENCES usuarios (id),
    status               VARCHAR(30)    NOT NULL DEFAULT 'AGUARDANDO_PAGAMENTO',
    valor_total          NUMERIC(10,2)  NOT NULL,
    forma_pagamento      VARCHAR(30)    NOT NULL,
    endereco_entrega     VARCHAR(255)   NOT NULL,
    id_pagamento_externo VARCHAR(100),
    criado_em            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em        TIMESTAMP
);

CREATE TABLE itens_pedido (
    id               BIGSERIAL      PRIMARY KEY,
    pedido_id        BIGINT         NOT NULL REFERENCES pedidos (id) ON DELETE CASCADE,
    marmita_id       BIGINT         NOT NULL REFERENCES marmitas (id),
    quantidade       INTEGER        NOT NULL CHECK (quantidade > 0),
    preco_unitario   NUMERIC(10,2)  NOT NULL
);
