package br.com.fiap.pedido.infra.client.integration;

import br.com.fiap.pedido.app.dto.estoque.EstoqueRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.core.domain.exception.EstoqueInsuficienteException;
import br.com.fiap.pedido.infra.client.EstoqueClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(classes = {EstoqueClient.class, br.com.fiap.pedido.infra.config.AppConfig.class})
@TestPropertySource(properties = "hosts.estoque=localhost:8082")
class EstoqueClientTest {

    @Autowired
    private EstoqueClient estoqueClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void deveAbaterEstoqueComSucesso() {
        UUID produtoId = UUID.randomUUID();
        ItemPedidoDTO item = new ItemPedidoDTO(produtoId, 2, BigDecimal.TEN);

        mockServer.expect(requestTo("http://localhost:8082/stock/api/produtos/" + produtoId))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json("""
                        {
                          "produtoId": "%s",
                          "quantidade": 2
                        }
                        """.formatted(produtoId)))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> estoqueClient.abaterEstoque(List.of(item)));
        mockServer.verify();
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        UUID produtoId = UUID.randomUUID();
        ItemPedidoDTO item = new ItemPedidoDTO(produtoId, 10, BigDecimal.TEN);

        mockServer.expect(requestTo("http://localhost:8082/stock/api/produtos/" + produtoId))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThrows(EstoqueInsuficienteException.class,
                () -> estoqueClient.abaterEstoque(List.of(item)));
    }

}
