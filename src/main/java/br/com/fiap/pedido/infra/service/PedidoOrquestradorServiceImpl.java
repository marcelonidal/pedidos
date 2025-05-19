package br.com.fiap.pedido.infra.service;

import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.core.domain.usecase.PedidoOrquestradorService;
import br.com.fiap.pedido.infra.client.PedidoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoOrquestradorServiceImpl implements PedidoOrquestradorService {

    private final PedidoClient pedidoClient;

    /* Cria o pedido no service, que publica o evento internamente */
    @Override
    public PedidoResponseDTO criarPedido(PedidoRequestDTO dto) {
        log.info("Orquestrador: criando pedido para cliente {}", dto.clienteId());
        return pedidoClient.criarPedido(dto);
    }

    /* Busca o pedido no service, que atualiza o status e publica evento se necessário */
    @Override
    public PedidoResponseDTO buscarPedidoPorId(UUID id) {
        log.info("Orquestrador: consultando pedido {}", id);
        return pedidoClient.buscarPorId(id);
    }

}