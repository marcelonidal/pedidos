package br.com.fiap.pedido.infra.client.integration;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pagamento.PagamentoRequestDTO;
import br.com.fiap.pedido.core.domain.exception.PagamentoException;
import br.com.fiap.pedido.core.domain.model.StatusPagamento;
import br.com.fiap.pedido.infra.client.PagamentoClient;
import br.com.fiap.pedido.infra.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(classes = {PagamentoClient.class, AppConfig.class})
@TestPropertySource(properties = "hosts.pagamento=localhost:8083")
class PagamentoClientTest {

    @Autowired
    private PagamentoClient pagamentoClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void deveSolicitarPagamentoComSucesso() {
        UUID idPedido = UUID.randomUUID();

        PagamentoRequestDTO dto = new PagamentoRequestDTO(
                idPedido, "1234567890123456", BigDecimal.valueOf(200));

        mockServer.expect(requestTo("http://localhost:8083/pagamentos"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess("""
                    {
                      "idPedido": "%s",
                      "idCartao": "1234567890123456",
                      "valor": 200,
                      "status": "AGUARDANDO",
                      "dataAprovacao": "2025-05-21T15:00:00"
                    }
                    """.formatted(idPedido), MediaType.APPLICATION_JSON));

        PagamentoDTO retorno = pagamentoClient.solicitarPagamento(dto);

        assertNotNull(retorno);
        assertEquals("1234567890123456", retorno.idCartao());
        assertEquals(BigDecimal.valueOf(200), retorno.valor());
        assertEquals(StatusPagamento.AGUARDANDO, retorno.status());
        assertEquals(idPedido, retorno.idPedido());
    }

    @Test
    void deveConsultarStatusComSucesso() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");

        mockServer.expect(requestTo("http://localhost:8083/pagamentos/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
            {
              "idPedido": "%s",
              "idCartao": "1234567890123456",
              "valor": 25.00,
              "status": "AGUARDANDO",
              "dataAprovacao": "2025-06-02T12:00:00"
            }
            """.formatted(id), MediaType.APPLICATION_JSON));

        PagamentoDTO retorno = pagamentoClient.consultarStatus(id);

        assertNotNull(retorno);
        assertEquals("1234567890123456", retorno.idCartao());
        assertEquals(StatusPagamento.AGUARDANDO, retorno.status());
        assertEquals(id, retorno.idPedido());
    }

    @Test
    void deveRetornarNullQuandoErroNaConsultaStatus() {
        UUID pedidoId = UUID.randomUUID();

        mockServer.expect(requestTo("http://localhost:8083/pagamentos/" + pedidoId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        PagamentoDTO resultado = pagamentoClient.consultarStatus(pedidoId);
        assertNull(resultado);
    }

    @Test
    void deveLancarExcecaoQuandoSolicitacaoFalhar() {
        PagamentoRequestDTO dto = new PagamentoRequestDTO(
                UUID.randomUUID(), "0000000000000000", BigDecimal.ZERO);

        mockServer.expect(requestTo("http://localhost:8083/pagamentos"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThrows(PagamentoException.class, () -> pagamentoClient.solicitarPagamento(dto));
    }

}
