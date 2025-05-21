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
docker network create toystorerede
```

### Subir PostgreSQL
```bash
docker run -d --name postgres-toy-store --network toystorerede -e DB_HOST=postgres-toy-store -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres
```

### Subir MongoDB
```bash
docker run -d --name mongo-toy-store --network toystorerede -e MONGO_HOST=mongo-toy-store -p 27017:27017 mongo
```

### Subir RabbitMQ
```bash
docker run -d --name rabbitmq-toy-store --network toystorerede -e RABBIT_HOST=rabbitmq-toy-store -p 5672:5672 -p 15672:15672 rabbitmq
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

## Docker

### Validar quem está na rede do docker
```bash
docker network inspect toystorerede
```

### Criar o imagem da aplicação

```bash
docker build -t pedido-service .
```

### Executar container

```bash
docker run -p 8080:8080 --network toystorerede pedido-service
```

### Executar o projeto completo manualmente:

```bash
docker run -d --name pedido-service -p 8080:8080 --network toystorerede -e DB_HOST=postgres-toy-store -e MONGO_HOST=mongo-toy-store -e RABBIT_HOST=rabbitmq-toy-store pedido-service
```

### Executar o projeto via docker compose:
Com todos os serviços prontos, você pode subir o sistema completo com:
```bash
docker-compose up --build
```

### Parar containers
```bash
docker stop postgres-toy-store mongo-toy-store rabbitmq-toy-store
```

### Remover containers
```bash
docker rm postgres-toy-store mongo-toy-store rabbitmq-toy-store
```

---

## Publicar no Docker Hub

### Criar imagem

```bash
docker build -t pedido-service .
```

### Fazer Login

```bash
docker login
```

### Tagear imagem

```bash
docker tag pedido-service marcelonidal/pedido-service:latest
```

### Subir no DockerHub

```bash
docker push marcelonidal/pedido-service:latest
```

### Rodar do DockerHub

```bash
docker run -d --name pedido-service -p 8080:8080 --network toystorerede -e DB_HOST=postgres-toy-store -e MONGO_HOST=mongo-toy-store -e RABBIT_HOST=rabbitmq-toy-store marcelonidal/pedido-service:latest
```