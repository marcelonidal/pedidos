package br.com.fiap.pedido.infra.client;

import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.core.domain.exception.ProdutoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Component
public class ProdutoClient {

    private final RestTemplate restTemplate;

    @Autowired
    public ProdutoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void validarProdutos(List<ItemPedidoDTO> itens) {
        for (ItemPedidoDTO item : itens) {
            UUID produtoId = item.produtoId();
            try {
                restTemplate.getForEntity(
                        "http://localhost:8082/api/produtos/" + produtoId, Void.class);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw new ProdutoNaoEncontradoException("Produto com ID " + produtoId + " nao encontrado");
                }
                throw new RuntimeException("Erro ao consultar produto: " + e.getMessage());
            }
        }
    }

}