package br.com.fiap.pedido.infra.client;

import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.produto.ProdutoResponseDTO;
import br.com.fiap.pedido.core.domain.exception.ProdutoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ProdutoClient {

    private final RestTemplate restTemplate;

    @Value("${hosts.produto}")
    private String hostProduto;

    @Autowired
    public ProdutoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProdutoResponseDTO buscarProduto(UUID produtoId) {
        String url = "http://" + hostProduto + "/produtos/" + produtoId;

        try {
            return restTemplate.getForObject(url, ProdutoResponseDTO.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ProdutoNaoEncontradoException("Produto com ID " + produtoId + " não encontrado");
            }
            throw new RuntimeException("Erro ao buscar produto: " + e.getMessage());
        }
    }

    public Map<UUID, ProdutoResponseDTO> buscarProdutos(List<ItemPedidoDTO> itens) {
        Map<UUID, ProdutoResponseDTO> produtos = new HashMap<>();

        for (ItemPedidoDTO item : itens) {
            ProdutoResponseDTO produto = buscarProduto(item.produtoId());
            produtos.put(item.produtoId(), produto);
        }

        return produtos;
    }

}