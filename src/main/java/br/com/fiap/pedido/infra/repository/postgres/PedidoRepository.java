package br.com.fiap.pedido.infra.repository.postgres;

import br.com.fiap.pedido.core.domain.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.itens WHERE p.id = :id")
    Optional<Pedido> buscarItensPorId(@Param("id") UUID id);

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.itens")
    List<Pedido> buscarItens();
}
