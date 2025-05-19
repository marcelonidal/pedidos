package br.com.fiap.pedido.app.controller.integration;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.core.domain.model.StatusPagamento;
import br.com.fiap.pedido.infra.client.ClienteClient;
import br.com.fiap.pedido.infra.client.EstoqueClient;
import br.com.fiap.pedido.infra.client.PagamentoClient;
import br.com.fiap.pedido.infra.client.ProdutoClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PedidoControllerIntegrationTest.MockClientsConfig.class)
@TestPropertySource(properties = "mock.api.externas=true")
class PedidoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockClientsConfig {

        @Bean
        @ConditionalOnProperty(name = "mock.api.externas", havingValue = "true", matchIfMissing = false)
        public ClienteClient clienteClient() {
            ClienteClient mock = Mockito.mock(ClienteClient.class);
            Mockito.doNothing().when(mock).validarCliente(any());
            return mock;
        }

        @Bean
        @ConditionalOnProperty(name = "mock.api.externas", havingValue = "true", matchIfMissing = false)
        public ProdutoClient produtoClient() {
            ProdutoClient mock = Mockito.mock(ProdutoClient.class);
            Mockito.doNothing().when(mock).validarProdutos(any());
            return mock;
        }

        @Bean
        @ConditionalOnProperty(name = "mock.api.externas", havingValue = "true", matchIfMissing = false)
        public EstoqueClient estoqueClient() {
            EstoqueClient mock = Mockito.mock(EstoqueClient.class);
            Mockito.doNothing().when(mock).abaterEstoque(any());
            return mock;
        }

        @Bean
        @ConditionalOnProperty(name = "mock.api.externas", havingValue = "true", matchIfMissing = false)
        public PagamentoClient pagamentoClient() {
            PagamentoClient mock = Mockito.mock(PagamentoClient.class);
            Mockito.when(mock.consultarStatus(any())).thenReturn(
                    new PagamentoDTO(UUID.randomUUID(), StatusPagamento.AGUARDANDO, "CARTAO_CREDITO", LocalDateTime.now())
            );
            return mock;
        }
    }

    @Test
    void shouldCreatePedidoSuccessfullyAndReturn200() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(
                UUID.randomUUID(),
                List.of(new ItemPedidoDTO(UUID.randomUUID(), 2, new BigDecimal("10.50"))),
                UUID.randomUUID()
        );

        mockMvc.perform(post("/internal/api/v1/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId", is(dto.clienteId().toString())))
                .andExpect(jsonPath("$.valorTotal", is(21.00)))
                .andExpect(jsonPath("$.status", is("CRIADO")));
    }

    @Test
     void shouldReturn404WhenPedidoDoesNotExist() throws Exception {
        UUID idInexistente = UUID.randomUUID();

        mockMvc.perform(get("/internal/api/v1/" + idInexistente)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Pedido nao encontrado"));
    }

    @Test
    void shouldReturnPedidoById() throws Exception {
        // primeiro, cria um pedido
        PedidoRequestDTO dto = new PedidoRequestDTO(
                UUID.randomUUID(),
                List.of(new ItemPedidoDTO(UUID.randomUUID(), 1, new BigDecimal("25.00"))),
                UUID.randomUUID()
        );

        String response = mockMvc.perform(post("/internal/api/v1/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // extrai o ID do pedido retornado
        UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        mockMvc.perform(get("/internal/api/v1/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("CRIADO"));
    }

    @Test
    void shouldListAllPedidos() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(
                UUID.randomUUID(),
                List.of(new ItemPedidoDTO(UUID.randomUUID(), 1, new BigDecimal("33.00"))),
                UUID.randomUUID()
        );

        mockMvc.perform(post("/internal/api/v1/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/internal/api/v1/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CRIADO"));
    }

    @Test
    void shouldCancelPedidoById() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(
                UUID.randomUUID(),
                List.of(new ItemPedidoDTO(UUID.randomUUID(), 1, new BigDecimal("45.00"))),
                UUID.randomUUID()
        );

        String response = mockMvc.perform(post("/internal/api/v1/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();

        UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        mockMvc.perform(put("/internal/api/v1/" + id + "/cancelar"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/internal/api/v1/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

}