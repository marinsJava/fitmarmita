-- Tabela alimentada pelo consumidor de mensageria (CupomBoasVindasConsumer),
-- a partir do evento USUARIO_CADASTRADO publicado pela fitmarmita-api.

CREATE TABLE cupons_boas_vindas (
    id                   BIGSERIAL      PRIMARY KEY,
    usuario_id           BIGINT         NOT NULL,    -- id logico, sem FK (usuario vive em outro banco)
    codigo               VARCHAR(20)    NOT NULL,
    percentual_desconto  INTEGER        NOT NULL,
    utilizado            BOOLEAN        NOT NULL DEFAULT FALSE,
    criado_em            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cupons_codigo UNIQUE (codigo)
);

CREATE UNIQUE INDEX idx_cupons_usuario_id ON cupons_boas_vindas (usuario_id);
