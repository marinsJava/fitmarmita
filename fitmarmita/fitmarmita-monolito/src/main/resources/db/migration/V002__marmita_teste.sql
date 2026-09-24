INSERT INTO tags (nome, slug) VALUES
    ('Low Carb', 'low-carb'),
    ('Vegana', 'vegana'),
    ('Sem Lactose', 'sem-lactose'),
    ('Fitness', 'fitness');

INSERT INTO marmitas (nome, descricao, preco, calorias, proteinas, carboidratos, gorduras, esgotada, ativa, semana_referencia) VALUES
    ('Frango Grelhado com Batata Doce', 'Peito de frango grelhado, batata doce e brocolis no vapor', 24.90, 420, 38.0, 45.0, 8.0, false, true, date_trunc('week', CURRENT_DATE)),
    ('Bowl Vegano de Grao de Bico', 'Grao de bico, quinoa, legumes assados e molho tahine', 22.90, 380, 16.0, 52.0, 12.0, false, true, date_trunc('week', CURRENT_DATE)),
    ('Salmao ao Molho de Ervas', 'Salmao grelhado, aspargos e arroz integral', 32.90, 460, 34.0, 30.0, 20.0, true, true, date_trunc('week', CURRENT_DATE));

INSERT INTO marmita_tag (marmita_id, tag_id)
SELECT m.id, t.id FROM marmitas m, tags t
WHERE (m.nome = 'Frango Grelhado com Batata Doce' AND t.slug IN ('fitness'))
   OR (m.nome = 'Bowl Vegano de Grao de Bico' AND t.slug IN ('vegana', 'low-carb'))
   OR (m.nome = 'Salmao ao Molho de Ervas' AND t.slug IN ('sem-lactose', 'fitness'));
