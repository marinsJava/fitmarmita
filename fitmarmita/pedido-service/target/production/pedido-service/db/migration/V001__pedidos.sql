-- Schema proprio do pedido-service.
-- Extraido do V001__schema_inicial.sql do monolito.
-- Sem FK para usuarios nem para marmitas: os dois vivem em outros bancos agora.

CREATE TABLE pedidos (
    id                   BIGSERIAL      PRIMARY KEY,
    usuario_id           BIGINT         NOT NULL,     -- id logico, sem FK
    status               VARCHAR(30)    NOT NULL DEFAULT 'AGUARDANDO_PAGAMENTO',
    valor_total          NUMERIC(10,2)  NOT NULL,
    forma_pagamento      VARCHAR(30)    NOT NULL,
    endereco_entrega     VARCHAR(255)   NOT NULL,
    id_pagamento_externo VARCHAR(100),
    criado_em            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em        TIMESTAMP
);

CREATE INDEX idx_pedidos_usuario_id ON pedidos (usuario_id);
CREATE INDEX idx_pedidos_status ON pedidos (status);

CREATE TABLE itens_pedido (
    id               BIGSERIAL      PRIMARY KEY,
    pedido_id        BIGINT         NOT NULL REFERENCES pedidos (id) ON DELETE CASCADE,
    marmita_id       BIGINT         NOT NULL,     -- id logico, sem FK
    nome_marmita     VARCHAR(150)   NOT NULL,     -- snapshot: nao muda se o cardapio mudar
    preco_unitario   NUMERIC(10,2)  NOT NULL,     -- snapshot do preco no momento da compra
    calorias         INTEGER,                     -- snapshot nutricional
    quantidade       INTEGER        NOT NULL CHECK (quantidade > 0)
);

CREATE INDEX idx_itens_pedido_pedido_id ON itens_pedido (pedido_id);
