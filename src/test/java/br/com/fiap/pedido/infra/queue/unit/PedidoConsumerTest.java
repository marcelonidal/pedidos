package br.com.fiap.pedido.infra.queue.unit;

import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseMongoDTO;
import br.com.fiap.pedido.app.mapper.PedidoMongoMapper;
import br.com.fiap.pedido.core.domain.model.PedidoMongo;
import br.com.fiap.pedido.infra.queue.PedidoConsumer;
import br.com.fiap.pedido.infra.repository.mongo.PedidoMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoConsumerTest {

    @Mock
    PedidoMongoRepository repository;

    @Mock
    PedidoMongoMapper mapper;

    @InjectMocks
    PedidoConsumer consumer;

    @Test
    void deveSalvarPedidoNoMongoAoConsumirEventoDeCriacao() {
        PedidoResponseDTO dto = mock(PedidoResponseDTO.class);
        PedidoResponseMongoDTO mongoDTO = mock(PedidoResponseMongoDTO.class);
        PedidoMongo pedidoMongo = mock(PedidoMongo.class);

        // Simula conversao DTO -> PedidoMongo
        when(dto.id()).thenReturn(UUID.randomUUID());
        when(mapper.toPedidoMongo(any())).thenReturn(pedidoMongo);

        consumer.consumir(dto);

        verify(mapper).toPedidoMongo(any());
        verify(repository).save(pedidoMongo);
    }

    @Test
    void deveAtualizarPedidoExistenteAoConsumirEventoDeAtualizacao() {
        UUID id = UUID.randomUUID();
        PedidoResponseMongoDTO dto = mock(PedidoResponseMongoDTO.class);
        PedidoMongo existente = mock(PedidoMongo.class);

        when(dto.id()).thenReturn(id);
        when(dto.status()).thenReturn("CRIADO");
        when(dto.itens()).thenReturn(List.of());
        when(dto.pagamento()).thenReturn(null);

        when(repository.findByIdPedido(id)).thenReturn(Optional.of(existente));
        when(mapper.toItens(any())).thenReturn(List.of());

        consumer.atualizarPedido(dto);

        verify(repository).save(existente);
    }

    @Test
    void deveCriarPedidoCasoNaoExistaAoAtualizar() {
        UUID id = UUID.randomUUID();
        PedidoResponseMongoDTO dto = mock(PedidoResponseMongoDTO.class);
        PedidoMongo novo = mock(PedidoMongo.class);

        when(dto.id()).thenReturn(id);
        when(repository.findByIdPedido(id)).thenReturn(Optional.empty());
        when(mapper.toPedidoMongo(dto)).thenReturn(novo);

        consumer.atualizarPedido(dto);

        verify(repository).save(novo);
    }
}
