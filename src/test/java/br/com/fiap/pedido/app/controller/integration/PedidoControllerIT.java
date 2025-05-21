package br.com.fiap.pedido.app.controller.integration;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.produto.ProdutoResponseDTO;
import br.com.fiap.pedido.core.domain.model.StatusPagamento;
import br.com.fiap.pedido.infra.client.ClienteClient;
import br.com.fiap.pedido.infra.client.EstoqueClient;
import br.com.fiap.pedido.infra.client.PagamentoClient;
import br.com.fiap.pedido.infra.client.ProdutoClient;
import br.com.fiap.pedido.util.GeradorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PedidoControllerIT.MockClientsConfig.class)
@TestPropertySource(properties = "mock.api.externas=true")
@TestPropertySource(properties = "server.servlet.context-path=/pedido")
class PedidoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    static final UUID PRODUTO_ID_FIXO = UUID.fromString("94c0da08-07d7-4304-861b-19846a00d933");

    @TestConfiguration
    static class MockClientsConfig {

        @Bean
        @ConditionalOnProperty(name = "mock.api.externas", havingValue = "true")
        public ClienteClient clienteClient() {
            ClienteClient mock = Mockito.mock(ClienteClient.class);
            Mockito.doNothing().when(mock).validarCliente(any());
            return mock;
        }

        @Bean
        @ConditionalOnProperty(name = "mock.api.externas", havingValue = "true")
        public ProdutoClient produtoClient() {
            ProdutoClient mock = Mockito.mock(ProdutoClient.class);

            ProdutoResponseDTO produto = new ProdutoResponseDTO(
                    PRODUTO_ID_FIXO,
                    "Camisa Azul",
                    "Nike",
                    "Azul",
                    "M",
                    "Adulto",
                    100,
                    new BigDecimal("25.00"),
                    "SKU123"
            );

            Mockito.when(mock.buscarProdutos(any()))
                    .thenReturn(Map.of(PRODUTO_ID_FIXO, produto));

            return mock;
        }

        @Bean
        @ConditionalOnProperty(name = "mock.api.externas", havingValue = "true")
        public EstoqueClient estoqueClient() {
            EstoqueClient mock = Mockito.mock(EstoqueClient.class);
            Mockito.doNothing().when(mock).abaterEstoque(any());
            return mock;
        }

        @Bean
        @ConditionalOnProperty(name = "mock.api.externas", havingValue = "true")
        public PagamentoClient pagamentoClient() {
            PagamentoClient mock = Mockito.mock(PagamentoClient.class);

            PagamentoDTO pagamentoSimulado = new PagamentoDTO(
                    UUID.randomUUID(),
                    GeradorUtil.gerarNumeroCartaoValido(),
                    BigDecimal.valueOf(25),
                    StatusPagamento.AGUARDANDO,
                    LocalDateTime.now()
            );

            Mockito.when(mock.consultarStatus(any())).thenReturn(pagamentoSimulado);
            Mockito.when(mock.solicitarPagamento(any())).thenReturn(pagamentoSimulado);

            return mock;
        }
    }

    @Test
    void shouldCreatePedidoSuccessfullyAndReturn200() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(
                GeradorUtil.gerarCpfValido(),
                List.of(new ItemPedidoDTO(PRODUTO_ID_FIXO, 2, new BigDecimal("25.00"))),
                GeradorUtil.gerarNumeroCartaoValido()
        );

        mockMvc.perform(post("/internal/api/v1/")
                        .headers(headersInternos())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteCpf", is(dto.clienteCpf())))
                .andExpect(jsonPath("$.valorTotal", is(50.00)))
                .andExpect(jsonPath("$.status", Matchers.anyOf(is("ENVIADO"), is("CRIADO"))));
    }

    @Test
    void shouldReturn404WhenPedidoDoesNotExist() throws Exception {
        UUID idInexistente = UUID.randomUUID();

        mockMvc.perform(get("/internal/api/v1/" + idInexistente)
                        .headers(headersInternos())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Pedido nao encontrado"));
    }

    @Test
    void shouldReturnPedidoById() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(
                GeradorUtil.gerarCpfValido(),
                List.of(new ItemPedidoDTO(PRODUTO_ID_FIXO, 1, new BigDecimal("25.00"))),
                GeradorUtil.gerarNumeroCartaoValido()
        );

        String response = mockMvc.perform(post("/internal/api/v1/")
                        .headers(headersInternos())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        mockMvc.perform(get("/internal/api/v1/" + id)
                        .headers(headersInternos()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value(Matchers.anyOf(is("CRIADO"), is("ENVIADO"))));
    }

    @Test
    void shouldListAllPedidos() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(
                GeradorUtil.gerarCpfValido(),
                List.of(new ItemPedidoDTO(PRODUTO_ID_FIXO, 1, new BigDecimal("33.00"))),
                GeradorUtil.gerarNumeroCartaoValido()
        );

        mockMvc.perform(post("/internal/api/v1/")
                        .headers(headersInternos())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/internal/api/v1/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(Matchers.anyOf(
                        is("CRIADO"), is("ENVIADO"), is("CANCELADO"))));
    }

    @Test
    void shouldCancelPedidoById() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(
                GeradorUtil.gerarCpfValido(),
                List.of(new ItemPedidoDTO(PRODUTO_ID_FIXO, 1, new BigDecimal("45.00"))),
                GeradorUtil.gerarNumeroCartaoValido()
        );

        String response = mockMvc.perform(post("/internal/api/v1/")
                        .headers(headersInternos())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();

        UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        mockMvc.perform(put("/internal/api/v1/" + id + "/cancelar")
                        .headers(headersInternos()))
                .andExpect(status().isNoContent());

        Thread.sleep(100);

        mockMvc.perform(get("/internal/api/v1/" + id)
                        .headers(headersInternos()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    private HttpHeaders headersInternos() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Call", "internal-secret");
        return headers;
    }

}