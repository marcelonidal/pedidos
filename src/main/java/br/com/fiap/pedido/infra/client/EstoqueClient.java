package br.com.fiap.pedido.infra.client;

import br.com.fiap.pedido.app.dto.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.ReservaEstoqueRequestDTO;
import br.com.fiap.pedido.core.domain.exception.EstoqueInsuficienteException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class EstoqueClient {

    private final RestTemplate restTemplate;

    public EstoqueClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void abaterEstoque(List<ItemPedidoDTO> itens) {
        List<ReservaEstoqueRequestDTO> requisicao = itens.stream()
                .map(item -> new ReservaEstoqueRequestDTO(item.produtoId(), item.quantidade()))
                .toList();

        try {
            HttpEntity<List<ReservaEstoqueRequestDTO>> request = new HttpEntity<>(requisicao);
            restTemplate.postForEntity("http://localhost:8083/api/estoque/reservar", request, Void.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                throw new EstoqueInsuficienteException("Estoque insuficiente para um ou mais produtos");
            }
            throw new RuntimeException("Erro ao reservar estoque: " + e.getMessage());
        }
    }

}
