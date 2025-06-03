package br.com.fiap.pedido.infra.client.integration;

import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.core.domain.model.StatusPagamento;
import br.com.fiap.pedido.infra.client.PedidoClient;
import br.com.fiap.pedido.infra.config.AppConfig;
import br.com.fiap.pedido.util.GeradorUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(classes = {PedidoClient.class, AppConfig.class})
@TestPropertySource(properties = "hosts.pedido=localhost:8080")
class PedidoClientTest {

    @Autowired
    private PedidoClient pedidoClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private static final UUID PEDIDO_ID_FIXO = UUID.randomUUID();
    private static final UUID PRODUTO_ID_FIXO = UUID.randomUUID();

    @BeforeEach
    void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void deveCriarPedidoComSucesso() {
        PedidoRequestDTO dto = new PedidoRequestDTO(
                GeradorUtil.gerarCpfValido(),
                List.of(new ItemPedidoDTO(PRODUTO_ID_FIXO, 2, new BigDecimal("25.00"))),
                GeradorUtil.gerarNumeroCartaoValido()
        );

        String body = """
            {
              "id": "%s",
              "clienteCpf": "%s",
              "dataCriacao": "2025-06-02T22:00:00",
              "status": "CRIADO",
              "valorTotal": 50.00,
              "itens": [
                {
                  "produtoId": "%s",
                  "quantidade": 2,
                  "precoUnitario": 25.00
                }
              ],
              "pagamento": {
                "idPedido": "%s",
                "idCartao": "%s",
                "valor": 50.00,
                "status": "AGUARDANDO",
                "dataAprovacao": null
              }
            }
            """.formatted(
                PEDIDO_ID_FIXO,
                dto.clienteCpf(),
                PRODUTO_ID_FIXO,
                PEDIDO_ID_FIXO,
                dto.idCartao()
        );

        mockServer.expect(once(), requestTo("http://localhost:8080/pedido/internal/api/v1/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Internal-Call", "internal-secret"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        PedidoResponseDTO resposta = pedidoClient.criarPedido(dto);

        assertThat(resposta, is(notNullValue()));
        assertThat(resposta.id(), is(PEDIDO_ID_FIXO));
        assertThat(resposta.valorTotal(), is(new BigDecimal("50.00")));
        assertThat(resposta.pagamento().status(), is(StatusPagamento.AGUARDANDO));
    }

    @Test
    void deveBuscarPedidoPorIdComSucesso() {
        String url = "http://localhost:8080/pedido/internal/api/v1/" + PEDIDO_ID_FIXO;

        String body = """
            {
              "id": "%s",
              "clienteCpf": "12345678909",
              "dataCriacao": "2025-06-02T22:00:00",
              "status": "CRIADO",
              "valorTotal": 50.00,
              "itens": [],
              "pagamento": {
                "idPedido": "%s",
                "idCartao": "1234567890123456",
                "valor": 50.00,
                "status": "APROVADO",
                "dataAprovacao": "2025-06-02T23:00:00"
              }
            }
            """.formatted(PEDIDO_ID_FIXO, PEDIDO_ID_FIXO);

        mockServer.expect(once(), requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Internal-Call", "internal-secret"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        PedidoResponseDTO resposta = pedidoClient.buscarPorId(PEDIDO_ID_FIXO);

        assertThat(resposta, is(notNullValue()));
        assertThat(resposta.id(), is(PEDIDO_ID_FIXO));
        assertThat(resposta.pagamento().status(), is(StatusPagamento.APROVADO));
    }

}
