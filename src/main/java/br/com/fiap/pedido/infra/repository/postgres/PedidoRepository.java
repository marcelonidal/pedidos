package br.com.fiap.pedido.infra.repository.postgres;

import br.com.fiap.pedido.core.domain.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
}
