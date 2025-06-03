package br.com.fiap.pedido.infra.client.integration;

import br.com.fiap.pedido.core.domain.exception.ClienteNaoEncontradoException;
import br.com.fiap.pedido.infra.client.ClienteClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(classes = {ClienteClient.class, br.com.fiap.pedido.infra.config.AppConfig.class})
@TestPropertySource(properties = "hosts.cliente=localhost:8081")
class ClienteClientTest {

    @Autowired
    private ClienteClient clienteClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void deveValidarClienteComSucesso() {
        String cpf = "12345678900";
        mockServer.expect(requestTo("http://localhost:8081/customer/v1/cliente/" + cpf))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> clienteClient.validarCliente(cpf));
        mockServer.verify();
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {
        String cpf = "00000000000";
        mockServer.expect(requestTo("http://localhost:8081/customer/v1/cliente/" + cpf))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(ClienteNaoEncontradoException.class, () -> clienteClient.validarCliente(cpf));
    }

}
