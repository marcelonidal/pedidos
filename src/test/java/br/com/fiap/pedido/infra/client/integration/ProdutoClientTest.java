package br.com.fiap.pedido.infra.client.integration;

import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.produto.ProdutoResponseDTO;
import br.com.fiap.pedido.infra.client.ProdutoClient;
import br.com.fiap.pedido.infra.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(classes = {ProdutoClient.class, AppConfig.class})
@TestPropertySource(properties = "hosts.produto=localhost:8084")
class ProdutoClientTest {

    @Autowired
    private ProdutoClient produtoClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private static final UUID PRODUTO_ID_FIXO = UUID.fromString("94c0da08-07d7-4304-861b-19846a00d933");

    @BeforeEach
    void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void deveBuscarProdutosComSucesso() {
        String responseBody = """
            {
               "produtoId": "%s",
               "nome": "Camisa Azul",
               "marca": "Nike",
               "cor": "Azul",
               "tamanho": "M",
               "faixaEtaria": "Adulto",
               "quantidadePecas": 100,
               "preco": 25.00,
               "skuDoProduto": "SKU123"
             }
            """.formatted(PRODUTO_ID_FIXO);

        mockServer.expect(requestTo("http://localhost:8084/produtos/" + PRODUTO_ID_FIXO))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        ItemPedidoDTO item = new ItemPedidoDTO(PRODUTO_ID_FIXO, 1, new BigDecimal("25.00"));
        Map<UUID, ProdutoResponseDTO> resposta = produtoClient.buscarProdutos(List.of(item));

        assertThat(resposta, is(notNullValue()));
        assertThat(resposta.containsKey(PRODUTO_ID_FIXO), is(true));
        assertThat(resposta.get(PRODUTO_ID_FIXO).nome(), is("Camisa Azul"));
        assertThat(resposta.get(PRODUTO_ID_FIXO).skuDoProduto(), is("SKU123"));

        mockServer.verify();
    }

}