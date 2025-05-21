package br.com.fiap.pedido.infra.queue;

import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseMongoDTO;
import br.com.fiap.pedido.app.mapper.PedidoMongoMapper;
import br.com.fiap.pedido.core.domain.model.PedidoMongo;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
import br.com.fiap.pedido.infra.repository.mongo.PedidoMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoConsumer {

    private final PedidoMongoRepository pedidoMongoRepository;
    private final PedidoMongoMapper pedidoMongoMapper;

    /**
     * Ouve eventos de criação de pedidos e salva no MongoDB
     */
    @RabbitListener(queues = "pedido.criado")
    public void consumir(PedidoResponseDTO dto) {
        log.info("Recebido pedido da fila: {}", dto.id());

        PedidoResponseMongoDTO mongoDTO = new PedidoResponseMongoDTO(
                dto.id(),
                dto.clienteCpf(),
                dto.dataCriacao(),
                dto.status(),
                dto.valorTotal(),
                dto.itens(),
                dto.pagamento()
        );

        PedidoMongo pedidoMongo = pedidoMongoMapper.toPedidoMongo(mongoDTO);
        pedidoMongoRepository.save(pedidoMongo);

        log.info("Pedido salvo no MongoDB com ID: {}", pedidoMongo.getId());
    }

    /**
     * Ouve eventos de atualizacao de pedidos e atualiza o documento no MongoDB
     */
    @RabbitListener(queues = "pedido.atualizado")
    public void atualizarPedido(PedidoResponseMongoDTO dto) {
        log.info("Atualizando pedido no Mongo: {}", dto.id());

        Optional<PedidoMongo> existenteOpt = pedidoMongoRepository.findByIdPedido(dto.id());

        if (existenteOpt.isEmpty()) {
            log.info("Pedido nao encontrado no Mongo, criando novo documento.");
            PedidoMongo novo = pedidoMongoMapper.toPedidoMongo(dto);
            pedidoMongoRepository.save(novo);
            log.info("Pedido criado no Mongo: {}", novo.getId());
            return;
        }

        PedidoMongo existente = existenteOpt.get();
        atualizarCampos(existente, dto);
        pedidoMongoRepository.save(existente);
        log.info("Pedido atualizado com sucesso no Mongo: {}", existente.getId());
    }

    /**
     * Atualiza os campos do documento Mongo com base no DTO recebido
     */
    private void atualizarCampos(PedidoMongo existente, PedidoResponseMongoDTO dto) {
        existente.setClienteCpf(dto.clienteCpf());
        existente.setDataCriacao(dto.dataCriacao());
        existente.setStatus(PedidoStatus.valueOf(dto.status()));
        existente.setValorTotal(dto.valorTotal());

        existente.setItens(pedidoMongoMapper.toItens(dto.itens()));

        if (dto.pagamento() != null) {
            existente.setPagamento(pedidoMongoMapper.toPagamento(dto.pagamento()));
        }
    }

}