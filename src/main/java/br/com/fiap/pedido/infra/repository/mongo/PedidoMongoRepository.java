package br.com.fiap.pedido.infra.repository.mongo;

import br.com.fiap.pedido.core.domain.model.PedidoMongo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface PedidoMongoRepository extends MongoRepository<PedidoMongo, String> {
    Optional<PedidoMongo> findByIdPedido(UUID idPedido);
}
