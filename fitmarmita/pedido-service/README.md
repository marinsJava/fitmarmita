# FitMarmita — Da modularização à extração de um microsserviço

## 1. Módulos da aplicação

A aplicação `FitMarmita-api` é organizada em pacotes por funcionalidade (package-by-feature). Cada um tem uma responsabilidade clara:

- **Usuário** — responsável pelo cadastro e pelos dados de conta dos usuários (nome, e-mail, senha, telefone), incluindo a lógica de bloqueio por tentativas de login inválidas.
- **Autenticação (auth)** — responsável por validar credenciais e emitir o token JWT usado pelo restante da aplicação para identificar quem está fazendo cada requisição.
- **Cardápio** — responsável por expor as marmitas disponíveis na semana, seus preços, informações nutricionais e tags (ex.: vegana, low carb).
- **Pedido** — responsável por registrar os pedidos feitos pelos usuários: os itens escolhidos, forma de pagamento, endereço de entrega e o status do pedido.

## 2. Análise de dependências

Exemplo de dependência existente entre dois módulos:

**Pedido → Cardápio**
Um pedido precisa consultar o cardápio para saber o preço atual e se a marmita ainda está disponível antes de confirmar a compra. Sem essa consulta, o pedido não tem como calcular o valor total nem impedir a compra de um item esgotado.

Outra dependência, mais indireta:

**Pedido → Usuário**
Todo pedido pertence a um usuário. O módulo de pedido não guarda os dados completos do usuário, mas depende de saber *quem* é o usuário autenticado — essa informação chega through o token JWT emitido pela Autenticação, que por sua vez depende do módulo Usuário para validar a senha.

Esse encadeamento (Pedido depende de Cardápio e, indiretamente, de Usuário via Autenticação) foi exatamente o que tornou a extração do Pedido mais trabalhosa do que seria extrair, por exemplo, só o Usuário isoladamente.

## 3. Funcionalidade escolhida para execução separada

- **Funcionalidade escolhida:** Pedido.
- **Responsabilidade:** criar e consultar pedidos — registrar os itens comprados, calcular o valor total, guardar forma de pagamento e endereço de entrega, e manter o histórico de pedidos por usuário.
- **Por que ela poderia ser executada separadamente:** o fluxo de pedidos tem um padrão de uso e de carga bem diferente do resto da aplicação — picotes de escrita concentrados (hora do almoço, por exemplo), enquanto cadastro de usuário e consulta de cardápio são majoritariamente leitura constante ao longo do dia. Separar permite escalar o serviço de pedidos de forma independente do restante, e permite evoluir suas regras (novas formas de pagamento, integração com um futuro serviço de entrega) sem redeployar a aplicação inteira.
- **Partes da aplicação que dependiam dela antes da extração:** nenhuma outra funcionalidade *consumia* dados de pedido de volta (nem cardápio nem usuário liam a tabela de pedidos) — a dependência era só de saída: o próprio módulo de pedido é quem dependia do cardápio (para preço/disponibilidade) e da autenticação (para saber o usuário). Isso, inclusive, foi o que tornou a extração viável sem exigir mudanças em cardápio ou usuário além de expor uma rota já pública.

## 4. O novo serviço

- **Nome do serviço:** `pedido-service`.
- **Responsabilidade principal:** criação e consulta de pedidos (`POST /api/v1/pedidos`, `GET /api/v1/pedidos/{id}`, `GET /api/v1/pedidos`), com banco de dados próprio (`fitmarmita_pedido`).
- **Funcionalidade removida da aplicação principal:** todo o pacote `pedido` (entidade, repositório, serviço, controller e as tabelas `pedidos`/`itens_pedido`) saiu do monólito `FitMarmita-api` e passou a viver exclusivamente no `pedido-service`.
- **Motivo da separação:** isolar o domínio de pedidos, que tem requisitos de disponibilidade, carga e evolução diferentes do restante da aplicação, permitindo escalar e implantar essa parte de forma independente sem afetar cadastro, login ou cardápio.

## 5. Reflexão sobre a separação

**Qual funcionalidade foi separada da aplicação principal?**
O módulo de Pedido.

**Por que ela foi escolhida?**
Porque é a funcionalidade com o padrão de carga mais distinto (concentrada em horários de pico) e a que mais provavelmente vai ganhar novas regras de negócio no futuro (novas formas de pagamento, rastreio de entrega), então isolá-la cedo evita que essas mudanças futuras exijam redeploy do monólito inteiro.

**O que ficou mais complexo depois da separação?**
Três coisas, na prática:
1. **A consulta de preço deixou de ser uma leitura em memória e virou uma chamada de rede.** Antes, o pedido lia a entidade `Marmita` no mesmo processo; agora, o `pedido-service` chama o cardápio via HTTP (Feign) a cada item do pedido, e essa chamada pode falhar, atrasar ou ficar indisponível.
2. **A autenticação precisou ser propagada manualmente.** O token JWT que chega no `pedido-service` não segue sozinho para a chamada ao cardápio — foi preciso um `RequestInterceptor` explícito para repassar o header `Authorization`, algo que dentro do monólito era automático (mesma requisição, mesmo contexto de segurança).
3. **Consistência deixou de ser transacional.** Criar um pedido e (futuramente) debitar estoque no cardápio não pode mais acontecer dentro de uma única transação de banco — exigiria um evento assíncrono ou um padrão de saga, não uma chamada síncrona simples.

**O que aconteceria com a funcionalidade principal caso o novo serviço ficasse indisponível?**
Se o `pedido-service` cair, ninguém consegue criar um pedido novo, mas o resto da aplicação continua funcionando normalmente: login, cadastro e consulta de cardápio não dependem dele. Pedidos já criados também continuam consultáveis normalmente pelo próprio `pedido-service` quando ele voltar, já que os dados ficam no banco dele. Já se for o **cardápio** que cair, aí sim a criação de pedidos novos trava (porque o `pedido-service` depende dele para validar preço e disponibilidade) — mas consultar pedidos já feitos continua funcionando, porque o snapshot do preço já foi salvo no momento da compra.

**A funcionalidade realmente precisa permanecer como um serviço independente, ou poderia continuar dentro da aplicação?**
Tecnicamente, não precisa — para o volume e a complexidade atuais do FitMarmita, um monólito modular bem organizado (como ele já era, com os módulos separados por pacote) resolveria com menos esforço operacional: um único deploy, uma transação de banco cobrindo pedido e cardápio, sem chamada de rede nem propagação de token. A separação em serviço faz mais sentido como exercício de aprendizado de arquitetura de microsserviços — e como base para, no futuro, escalar pedidos de forma independente se o volume de pedidos crescer muito mais rápido que o resto da aplicação — do que como uma necessidade real do sistema hoje.
