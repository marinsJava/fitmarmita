# Migração da funcionalidade de Pedidos para Microsserviço

## Funcionalidade escolhida (Nome do serviço)

A funcionalidade escolhida para ser extraída do monólito é o **gerenciamento de Pedidos**.

A partir da aplicação monolítica original, a funcionalidade de pedidos será isolada em um microsserviço denominado `pedido-service`.

## Responsabilidade principal

O `pedido-service` será responsável pelas operações relacionadas aos pedidos realizados pelos usuários, incluindo:

- criação de pedidos;
- consulta de pedidos;
- atualização do status dos pedidos;
- gerenciamento dos itens pertencentes a cada pedido;
- cálculo e manutenção das informações relacionadas ao pedido;
- validação das regras de negócio específicas de pedidos.

O microsserviço deverá concentrar as regras de negócio diretamente relacionadas ao ciclo de vida de um pedido, evitando manter responsabilidades pertencentes a outros contextos da aplicação.

## Funcionalidade que foi removida ou separada da aplicação principal.

Separada: "Pedido" passou a ser "pedido-service" utilizando-se de microsserviço

Removida: "Carrinho" foi removido a fim de simplificar a aplicação e não existir estado intermediário.


## Motivo da separação.

A funcionalidade de pedidos possui um conjunto relativamente bem definido de responsabilidades e regras de negócio, podendo ser isolada do restante da aplicação.

O processamento de um pedido não precisa estar fisicamente dentro do mesmo processo que gerencia usuários, autenticação ou outras funcionalidades do sistema. A comunicação entre essas funcionalidades pode ocorrer por meio de uma API.

