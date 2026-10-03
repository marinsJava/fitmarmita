# FitMarmita

## 1. Módulos da aplicação

A aplicação `FitMarmita-api` é organizada em pacotes por funcionalidade. Cada um tem uma responsabilidade clara:

- **Usuário** — responsável pelo cadastro e pelos dados de conta dos usuários, incluindo a lógica de bloqueio por tentativas de login inválidas.
- **Autenticação** — responsável por validar credenciais e emitir o token JWT usado pelo restante da aplicação para identificar quem está fazendo cada requisição.
- **Cardápio** — responsável por expor as marmitas disponíveis na semana, seus preços, informações nutricionais e tags.
- **Pedido** — responsável por registrar os pedidos feitos pelos usuários: os itens escolhidos, forma de pagamento, endereço de entrega e o status do pedido.

## 2. Análise de dependências

Exemplo de dependência existente entre dois módulos:

**Pedido → Cardápio**
Um pedido precisa consultar o cardápio para saber o preço atual e se a marmita ainda está disponível antes de confirmar a compra. Sem essa consulta, o pedido não tem como calcular o valor total nem impedir a compra de um item esgotado.

Outra dependência, mais indireta:

**Pedido → Usuário**
Todo pedido pertence a um usuário. O módulo de pedido não guarda os dados completos do usuário, mas depende de saber *quem* é o usuário autenticado — essa informação chega through o token JWT emitido pela Autenticação, que por sua vez depende do módulo Usuário para validar a senha.

Esse encadeamento foi exatamente o que tornou a extração do Pedido mais trabalhosa do que seria extrair, por exemplo, só o Usuário isoladamente.

## 3. Funcionalidade escolhida para execução separada

- **Funcionalidade escolhida:** Pedido.
- **Responsabilidade:** criar e consultar pedidos — registrar os itens comprados, calcular o valor total, guardar forma de pagamento e endereço de entrega, e manter o histórico de pedidos por usuário.
- **Por que ela poderia ser executada separadamente:** o fluxo de pedidos tem um padrão de uso e de carga bem diferente do resto da aplicação — picotes de escrita concentrados, enquanto cadastro de usuário e consulta de cardápio são majoritariamente leitura constante ao longo do dia. Separar permite escalar o serviço de pedidos de forma independente do restante, e permite evoluir suas regras sem redeployar a aplicação inteira.
- **Partes da aplicação que dependiam dela antes da extração:** nenhuma outra funcionalidade *consumia* dados de pedido de volta (nem cardápio nem usuário liam a tabela de pedidos) — a dependência era só de saída: o próprio módulo de pedido é quem dependia do cardápio (para preço/disponibilidade) e da autenticação (para saber o usuário). Isso, inclusive, foi o que tornou a extração viável sem exigir mudanças em cardápio ou usuário além de expor uma rota já pública.

## 4. O novo serviço

- **Nome do serviço:** `pedido-service`.
- **Responsabilidade principal:** criação e consulta de pedidos (`POST /api/v1/pedidos`, `GET /api/v1/pedidos/{id}`, `GET /api/v1/pedidos`), com banco de dados próprio (`fitmarmita_pedido`).
- **Funcionalidade removida da aplicação principal:** todo o pacote `pedido` (entidade, repositório, serviço, controller e as tabelas `pedidos`/`itens_pedido`) saiu do monólito `FitMarmita-api` e passou a viver exclusivamente no `pedido-service`.
- **Motivo da separação:** isolar o domínio de pedidos, que tem requisitos de disponibilidade, carga e evolução diferentes do restante da aplicação, permitindo escalar e implantar essa parte de forma independente sem afetar cadastro, login ou cardápio

## 5. Qual funcionalidade foi separada da aplicação principal?
O módulo de Pedido.

## 6. Por que ela foi escolhida?
Porque é a funcionalidade com o padrão de carga mais distinto (concentrada em horários de pico) e a que mais provavelmente vai ganhar novas regras de negócio no futuro (novas formas de pagamento, rastreio de entrega), então isolá-la cedo evita que essas mudanças futuras exijam redeploy do monólito inteiro.

## 7. O que ficou mais complexo depois da separação?
Três coisas, na prática:
1. **A consulta de preço deixou de ser uma leitura em memória e virou uma chamada de rede.** Antes, o pedido lia a entidade `Marmita` no mesmo processo; agora, o `pedido-service` chama o cardápio via HTTP a cada item do pedido, e essa chamada pode falhar, atrasar ou ficar indisponível.
2. **A autenticação precisou ser propagada manualmente.** O token JWT que chega no `pedido-service` não segue sozinho para a chamada ao cardápio — foi preciso um `RequestInterceptor` explícito para repassar o header `Authorization`, algo que dentro do monólito era automático.
3. **Consistência deixou de ser transacional.** Criar um pedido e debitar estoque no cardápio não pode mais acontecer dentro de uma única transação de banco — exigiria um evento assíncrono ou um padrão de saga, não uma chamada síncrona simples.

## 8. O que aconteceria com a funcionalidade principal caso o novo serviço ficasse indisponível?
Se o `pedido-service` cair, ninguém consegue criar um pedido novo, mas o resto da aplicação continua funcionando normalmente: login, cadastro e consulta de cardápio não dependem dele. Pedidos já criados também continuam consultáveis normalmente pelo próprio `pedido-service` quando ele voltar, já que os dados ficam no banco dele. Já se for o **cardápio** que cair, aí sim a criação de pedidos novos trava — mas consultar pedidos já feitos continua funcionando, porque o snapshot do preço já foi salvo no momento da compra.

## 9. A funcionalidade realmente precisa permanecer como um serviço independente, ou poderia continuar dentro da aplicação?
Tecnicamente, não precisa — para o volume e a complexidade atuais do FitMarmita, um monólito modular bem organizado resolveria com menos esforço operacional: um único deploy, uma transação de banco cobrindo pedido e cardápio, sem chamada de rede nem propagação de token. A separação em serviço faz mais sentido como exercício de aprendizado de arquitetura de microsserviços — e como base para, no futuro, escalar pedidos de forma independente se o volume de pedidos crescer muito mais rápido que o resto da aplicação — do que como uma necessidade real do sistema hoje.

## 10. Quais configurações da aplicação podem variar entre ambientes?
Porta do servidor, URL/usuário/senha do banco de dados, URL de comunicação
entre serviços (`pedido-service` → `fitmarmita-api`), origem permitida de
CORS, segredo e tempo de expiração do JWT, política de bloqueio de login
(tentativas e minutos), nível de log e se o Swagger fica habilitado.

## 11. Quais dessas configurações foram externalizadas?
Todas as listadas acima. Porta, dados de conexão com o banco, URL entre
serviços, segredo/expiração do JWT e política de login saem de **variáveis
de ambiente** (`SERVER_PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`,
`CARDAPIO_SERVICE_URL`, `JWT_SECRET`, `JWT_EXPIRACAO_HORAS`,
`CORS_ALLOWED_ORIGIN`, `LOGIN_MAX_TENTATIVAS`, `LOGIN_MINUTOS_BLOQUEIO`).
Porta, política de login, CORS, URL entre serviços e nível de log também
são servidos de forma centralizada pelo **Config Server**, variando por
**profile** (`dev`/`prod`). Nada disso fica fixo no código Java.

## 12. Por que um serviço não deve acessar diretamente o banco de outro serviço?
Porque isso quebra o isolamento de cada serviço: o banco vira um contrato
implícito entre eles, e qualquer mudança de schema em um serviço arrisca
quebrar o outro sem aviso. Isso acopla fortemente os dois no nível de
dados, impede que evoluam, façam deploy e escalem de forma independente, e
viola o princípio de que cada serviço é o único dono dos seus dados. A
comunicação entre responsabilidades separadas deve passar pela interface
que o serviço expõe (API HTTP), não por SQL direto no banco alheio.

## 13. Qual problema o Docker resolve no projeto?
Resolve o "na minha máquina funciona": empacota a aplicação junto com o
runtime e as dependências necessárias numa imagem única, garantindo
execução previsível e reproduzível em qualquer máquina, sem exigir que
quem for rodar instale Java, Maven ou Postgres manualmente com a versão
certa.

## 14. Qual é a função do Docker Compose?
Orquestrar vários containers (as duas aplicações, o Config Server e os dois
bancos) como uma única solução: define a rede compartilhada entre eles, as
variáveis de ambiente de cada um, a ordem/condição de inicialização
(`depends_on` + healthcheck) e os volumes de dados — tudo isso subindo com
um único comando (`docker compose up`).

## 15. Qual problema uma configuração centralizada procura resolver?
Evita que a mesma informação de ambiente fique duplicada e possa ficar
dessincronizada em vários serviços/arquivos. Com o Config Server, existe um
único ponto de verdade para saber qual configuração está valendo em cada
ambiente, e é possível mudar o comportamento de vários serviços (porta,
CORS, política de login, URLs) sem alterar código-fonte nem reconstruir as
imagens.

## 16

### 16.1 Mensageria (RabbitMQ)

**Operação escolhida:** geração de um cupom de boas-vindas (10% de desconto
na primeira compra) quando um novo usuário se cadastra.

- **Produtor:** `fitmarmita-api`. Depois de salvar o novo usuário
  (`UsuarioService.criar`), publica o evento `USUARIO_CADASTRADO`
  (`{ tipo, identificador, nome, email, dataCadastro }`) na exchange
  `fitmarmita.exchange`, routing key `usuario.cadastrado`.
- **Fila:** `usuario.cadastrado.queue`, durável (sobrevive a restart do
  broker; mensagens não confirmadas continuam nela).
- **Consumidor:** `pedido-service` (`CupomBoasVindasConsumer`), que ouve
  essa fila, gera um código de cupom e grava na tabela
  `cupons_boas_vindas` — que pertence ao `pedido-service`, porque é ele
  quem sabe aplicar um cupom na hora de fechar um pedido.

**Por que essa operação não precisa ser concluída durante a requisição original?**
Porque o cadastro do usuário é uma operação completa e válida com ou sem o
cupom: o que garante que o e-mail é único, que a senha foi criptografada e
que o usuário existe é só a gravação na tabela `usuarios`. Gerar o cupom é
um "bônus" que pode acontecer alguns milissegundos (ou minutos) depois, sem
que o usuário perceba diferença na resposta do `POST /api/v1/usuarios`. Se
essa geração fosse síncrona (uma chamada REST do monólito para o
pedido-service dentro do mesmo request), o cadastro ficaria refém da
disponibilidade de um serviço que não tem nada a ver com autenticar ou
validar os dados do usuário.

**O que acontece com a mensagem se o consumidor estiver indisponível?**
Ela fica esperando, sem ser perdida, na fila `usuario.cadastrado.queue`
dentro do RabbitMQ — diferente de uma chamada REST, que falharia
imediatamente (ou exigiria retry manual) se o `pedido-service` estivesse
fora do ar. Assim que o `pedido-service` volta a subir e o
`CupomBoasVindasConsumer` reconecta, ele consome as mensagens acumuladas em
ordem e gera os cupons pendentes.

**Como demonstrar isso na prática:**
1. Suba tudo com `docker compose up --build` a partir da raiz.
2. Pare só o consumidor: `docker compose stop pedido-service`.
3. Cadastre um ou mais usuários: `POST http://localhost:8080/api/v1/usuarios`.
4. Abra a UI do RabbitMQ em `http://localhost:15672` (usuário/senha
   `guest`/`guest`) e veja as mensagens acumuladas em
   **Queues → usuario.cadastrado.queue** (coluna "Ready").
5. Suba o consumidor de novo: `docker compose start pedido-service`. As
   mensagens são drenadas e os cupons aparecem na tabela
   `cupons_boas_vindas` do banco `fitmarmita_pedido` — confirme com:
   `docker compose exec postgres-pedido psql -U fitmarmita -d fitmarmita_pedido -c "select * from cupons_boas_vindas;"`

### 16.2 Processamento em lote (Spring Batch)

**Funcionalidade escolhida:** importação de marmitas do cardápio a partir
de um arquivo CSV (`fitmarmita-monolito/src/main/resources/batch/marmitas-importacao.csv`).

**Por que essa funcionalidade é adequada para Batch?**
Cadastrar o cardápio da semana é, por natureza, uma operação em **conjunto**
(várias marmitas de uma vez, normalmente feita pela equipe de nutrição uma
vez por semana) e **tolerante a atraso** — ninguém precisa que as 30
marmitas da semana apareçam instantaneamente, uma resposta HTTP síncrona
por item seria lenta e desnecessária. O modelo leitura → processamento →
escrita do Spring Batch encaixa perfeitamente: ler o arquivo inteiro,
validar/normalizar cada linha e gravar em chunks (a cada 5 registros),
continuando mesmo que uma linha específica seja inválida.

**Estrutura implementada** (`br.com.fitmarmita.batch`):
- **Job:** `importarMarmitasJob` → **Step:** `importarMarmitasStep` (chunk = 5).
- **ItemReader:** `FlatFileItemReader` lê o CSV
  (`nome,descricao,preco,calorias,semanaReferencia`).
- **ItemProcessor:** `MarmitaItemProcessor` valida (nome e preço
  obrigatórios; registro inválido é **ignorado**, não interrompe o Job),
  normaliza texto (remove espaços duplicados) e normaliza a semana de
  referência para a segunda-feira daquela semana.
- **ItemWriter:** `RepositoryItemWriter` grava direto pelo
  `MarmitaRepository` já existente no módulo Cardápio — sem acesso
  paralelo ao banco.

**Como disparar:**
```
POST http://localhost:8080/api/v1/batch/marmitas/importar
Authorization: Bearer <token de um usuário autenticado>
```
A resposta traz `itensLidos`, `itensGravados` e `itensIgnorados`. O CSV de
exemplo inclui de propósito uma linha sem nome e uma com preço inválido,
para demonstrar que elas são ignoradas sem derrubar o Job.

### 16.3 Diferença entre mensageria e Batch

A mensageria resolve **comunicação assíncrona entre componentes**: um
evento pontual (“um usuário acabou de se cadastrar”) que outro serviço
processa de forma independente, item por item, quase em tempo real. O
Spring Batch resolve **processamento estruturado de um conjunto de
dados**: um lote inteiro (o CSV de marmitas), processado de forma
controlada em chunks, tipicamente disparado sob demanda ou em um horário
programado — não em reação a um evento de negócio individual.

### 16.4 Em quais situações da aplicação usar REST, mensageria ou Batch?

- **REST** — quando quem chama precisa de uma resposta imediata para
  continuar: `pedido-service` consultando preço/disponibilidade de uma
  marmita na `fitmarmita-api` antes de confirmar um pedido, ou o login
  emitindo o token JWT. Sem resposta síncrona, a operação não pode
  prosseguir.
- **Mensageria** — quando uma ação de negócio dispara um efeito colateral
  que não precisa bloquear quem a originou: o cupom de boas-vindas deste
  README, ou, evoluindo o projeto, notificar o usuário quando o status de
  um pedido muda (`AGUARDANDO_PAGAMENTO` → `CONFIRMADO`).
- **Batch** — quando existe um **conjunto** de dados para processar de uma
  vez, geralmente periódico ou disparado manualmente: a importação de
  cardápio deste README, ou, evoluindo o projeto, um fechamento diário que
  agrega o valor total de pedidos do dia por usuário.

## 17. Por que a operação de gerar o cupom de boas-vindas pode ser executada de forma assíncrona?

Porque ela não faz parte do que define um cadastro de usuário válido: o que
garante isso é só gravar o usuário na tabela `usuarios`, com e-mail único e
senha criptografada. O cupom é um benefício adicional, que pode ser
calculado alguns instantes depois sem que o usuário perceba diferença na
resposta do `POST /api/v1/usuarios`. Se essa geração fosse síncrona (uma
chamada REST do monólito para o `pedido-service` dentro do mesmo request),
o cadastro passaria a depender da disponibilidade de um serviço que não
tem nada a ver com autenticar ou validar os dados do usuário.

## 18. Qual a diferença entre mensageria e Batch (as duas funcionalidades desta etapa)?

A mensageria resolve **comunicação assíncrona entre componentes**: um
evento pontual ("um usuário acabou de se cadastrar") processado por outro
serviço de forma independente, item por item, quase em tempo real. O
Spring Batch resolve **processamento estruturado de um conjunto de
dados**: um lote inteiro (o CSV de marmitas), processado em chunks de
forma controlada, tipicamente disparado sob demanda ou em um horário
programado — não em reação a um evento de negócio individual.

## 19. Qual operação foi escolhida para comunicação assíncrona?

A geração de um cupom de 10% de desconto na primeira compra, disparada
quando um usuário se cadastra. A `fitmarmita-api` publica o evento
`USUARIO_CADASTRADO` (exchange `fitmarmita.exchange`, fila
`usuario.cadastrado.queue`) e o `pedido-service` consome esse evento
(`CupomBoasVindasConsumer`), gravando o cupom na tabela
`cupons_boas_vindas`.

## 20. Por que essa operação não precisa necessariamente ser concluída durante a requisição original?

Porque o resultado do cadastro (usuário criado, e-mail validado como
único, senha protegida) não depende do cupom existir. O cupom só é
necessário mais tarde, quando esse usuário for fechar sua primeira
compra — ou seja, há uma janela de tempo confortável entre o cadastro e o
momento em que o cupom realmente precisa existir, o que é exatamente o
cenário em que vale a pena desacoplar via mensageria em vez de bloquear a
resposta do cadastro.

## 21. O que acontece com a mensagem caso o consumidor esteja temporariamente indisponível?

Ela permanece guardada, sem ser perdida, na fila `usuario.cadastrado.queue`
dentro do RabbitMQ (fila durável). Diferente de uma chamada REST, que
falharia na hora se o `pedido-service` estivesse fora do ar, a mensagem só
fica esperando. Assim que o `pedido-service` volta a subir, o
`CupomBoasVindasConsumer` reconecta e processa as mensagens acumuladas em
ordem, gerando os cupons pendentes — pode ser testado parando o
`pedido-service` (`docker compose stop pedido-service`), cadastrando
usuários e observando a fila crescer na UI do RabbitMQ
(`localhost:15672`).

## 22. Qual funcionalidade foi escolhida para processamento em lote?

A importação de marmitas do cardápio a partir de um arquivo CSV
(`fitmarmita-monolito/src/main/resources/batch/marmitas-importacao.csv`),
via o Job `importarMarmitasJob` do Spring Batch.

## 23. Por que essa funcionalidade é adequada para Batch?

Porque cadastrar o cardápio da semana é, por natureza, uma operação em
**conjunto** — várias marmitas de uma vez, tipicamente feita pela equipe
de nutrição periodicamente — e **tolerante a atraso**, já que ninguém
precisa que todas apareçam instantaneamente. O modelo leitura →
processamento → escrita do Spring Batch encaixa bem: ler o arquivo
inteiro, validar e normalizar cada linha, e gravar em chunks, continuando
o processamento mesmo que uma linha específica seja inválida (o CSV de
exemplo tem de propósito uma linha sem nome e uma com preço inválido, que
são ignoradas sem interromper o Job).

## 24. Em quais situações da aplicação seria mais adequado utilizar REST, mensageria ou Batch?

- **REST** — quando quem chama precisa de uma resposta imediata para
  continuar: o `pedido-service` consultando preço/disponibilidade de uma
  marmita na `fitmarmita-api` antes de confirmar um pedido, ou o login
  emitindo o token JWT. Sem a resposta síncrona, a operação não pode
  prosseguir.
- **Mensageria** — quando uma ação de negócio dispara um efeito colateral
  que não precisa bloquear quem a originou: o cupom de boas-vindas deste
  README, ou, evoluindo o projeto, notificar o usuário quando o status de
  um pedido muda (`AGUARDANDO_PAGAMENTO` → `CONFIRMADO`).
- **Batch** — quando existe um **conjunto** de dados para processar de uma
  vez, geralmente periódico ou disparado manualmente: a importação de
  cardápio deste README, ou, evoluindo o projeto, um fechamento diário que
  agrega o valor total de pedidos do dia por usuário.
