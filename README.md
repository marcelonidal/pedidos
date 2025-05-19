# pedido-service

Microserviço responsável pela criação, gerenciamento e consulta de pedidos.
Integra com PostgreSQL para persistência estruturada, MongoDB para histórico e consultas rápidas, e RabbitMQ para entrada de pedidos via fila.
---

## Tecnologias

- Java 21 (Amazon Corretto)
- Spring Boot
- Spring Data JPA (PostgreSQL)
- Spring Data MongoDB
- Spring AMQP (RabbitMQ)
- MapStruct
- Lombok
- Swagger OpenAPI
- JUnit 5, Mockito
- Jacoco (cobertura de testes)
- Docker/Docker Compose

---

## Arquitetura

![Fluxo do Pedido](docs/fluxo_pedido_completo_com_legenda.png)

Este microserviço segue uma arquitetura limpa e orientada a eventos, com separação clara de responsabilidades entre criação, consulta e persistência.

### Fluxo completo:
1. O **`PedidoReceiverController`** atua como porta de entrada (gateway), recebendo as requisições REST (POST para criar, GET para consultar).
2. Ele repassa a chamada para o **`PedidoOrquestradorService`**, que orquestra o fluxo completo.
3. No caso de criação:
    - Chama o `pedido-service` via client interno (`PedidoClient`) para gravar o pedido no PostgreSQL.
    - O `pedido-service`, por sua vez, valida cliente/produto, atualiza o estoque, salva o pedido e publica o evento `pedido.criado` no RabbitMQ.
4. O **`PedidoConsumer`** escuta a fila `pedido.criado` e, ao receber o evento, grava o objeto no MongoDB com status inicial `AGUARDANDO`.
5. Para consultas (`GET /pedido/{id}`):
    - O `PedidoReceiverController` chama o orquestrador, que acessa o `pedido-service` para buscar o pedido atualizado e o status de pagamento via `PagamentoClient`.
    - Se houver mudança no status, o MongoDB é atualizado com o novo estado.

Esse modelo permite performance, rastreabilidade e desacoplamento entre serviços.

---

## Documentação da API

A documentação da API REST está disponível via Swagger OpenAPI:

[swagger-ui.html](http://localhost:8080/pedido/swagger-ui.html)
- [API Pública - Gateway](http://localhost:8080/pedido/swagger-ui.html?configUrl=/v3/api-docs/publico)
- [API Interna - Serviço Pedido](http://localhost:8080/pedido/swagger-ui.html?configUrl=/v3/api-docs/interno)

Observações:<br>
Use o seletor no topo do Swagger UI para alternar entre os grupos `publico` e `interno`.<br>
Você pode testar todos os endpoints diretamente pela interface web.

---

## Banco de Dados e Fila — Setup com Docker

### Criar rede Docker
```bash
docker network create network-pedidos
```

### Subir PostgreSQL
```bash
docker run -d --name postgres-pedidos --network network-pedidos -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=pedidos -p 5432:5432 postgres:15
```

### Subir MongoDB
```bash
docker run -d --name mongo-pedidos --network network-pedidos -p 27017:27017 mongo:6
```

### Subir RabbitMQ
```bash
docker run -d --name rabbitmq-pedidos --network network-pedidos -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=guest -e RABBITMQ_DEFAULT_PASS=guest rabbitmq:3-management
```
Acesse o painel de administração em: http://localhost:15672
Usuário: guest
Senha: guest

---

## Execução dos Testes

Este projeto possui testes unitários e de integração separados por perfis Maven. A cobertura é medida com Jacoco.

### ️Testes Unitários

```bash
mvn clean test -Punit-tests
```

### Testes de Integração

```bash
mvn clean verify -Pintegration-tests
```

### Cobertura de Testes

```bash
mvn clean verify site
```
Observações:<br>
Gera o relatório de cobertura em: target/site/jacoco/index.html<br>
Parar visualizar o relatório necessário abrir via real path ou script:<br>
- file:///C:/caminho/do/seu/projeto/pedido/target/site/jacoco/index.html
- via bat no windows: start "" "target\site\jacoco\index.html"
- run no arquivo abrir-relatorio.bat na raiz do projeto.

---

## Execução via Docker Compose

Com todos os serviços prontos, você pode subir o sistema completo com:

```bash
docker-compose up --build
```

## Validar quem está na rede do docker
```bash
docker network inspect network-pedidos
```

## Criar o container da aplicação

```bash
docker build -t pedido-service .
```

## Executar container

```bash
docker run -p 8080:8080 --network network-pedidos pedido-service
```

## Executar o projeto completo manualmente:

```bash
docker run -p 8080:8080 --name pedido-service --network network-pedidos -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-pedidos:5432/pedidos -e SPRING_DATASOURCE_USERNAME=postgres -e SPRING_DATASOURCE_PASSWORD=postgres -e SPRING_DATA_MONGODB_URI=mongodb://mongo-pedidos:27017/pedidos -e SPRING_RABBITMQ_HOST=rabbitmq-pedidos pedido-service
```

## Executar o projeto via docker compose:
```bash
docker-compose up --build
```
Observações:<br>
Não pode ter os containers com o mesmo nome, ele recria tudo num único pacote.

## Parar containers
```bash
docker stop postgres-pedidos mongo-pedidos rabbitmq-pedidos
```

## Remover containers
```bash
docker rm postgres-pedidos mongo-pedidos rabbitmq-pedidos
```