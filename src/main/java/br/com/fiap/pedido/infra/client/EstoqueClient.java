package br.com.fiap.pedido.infra.client;

import br.com.fiap.pedido.app.dto.estoque.EstoqueRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.core.domain.exception.EstoqueInsuficienteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class EstoqueClient {

    private final RestTemplate restTemplate;

    @Value("${hosts.estoque}")
    private String hostEstoque;

    @Autowired
    public EstoqueClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void abaterEstoque(List<ItemPedidoDTO> itens) {
        for (ItemPedidoDTO item : itens) {
            try {
                String url = "http://" + hostEstoque + "/stock/api/produtos/" + item.produtoId();
                EstoqueRequestDTO requestBody = new EstoqueRequestDTO(item.produtoId(), item.quantidade());

                restTemplate.put(url, requestBody); //nao retorna resposta

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                    throw new EstoqueInsuficienteException("Estoque insuficiente para o produto " + item.produtoId());
                }
                throw new RuntimeException("Erro ao reservar estoque: " + e.getMessage());
            }
        }
    }

}