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
## Banco de Dados e Fila — Setup com Docker

### Criar rede Docker
```bash
docker network create network-pedidos
```

### Subir PostgreSQL
```bash
docker run -d --network network-pedidos -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=pedidos -p 5432:5432 postgres:15
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