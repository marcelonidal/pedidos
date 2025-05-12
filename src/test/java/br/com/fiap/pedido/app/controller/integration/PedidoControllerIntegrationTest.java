package br.com.fiap.pedido.app.controller.integration;

import br.com.fiap.pedido.app.dto.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.PedidoRequestDTO;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
import br.com.fiap.pedido.infra.repository.postgres.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final PedidoRepository pedidoRepository;
    private final ObjectMapper objectMapper;

    public PedidoControllerIntegrationTest(MockMvc mockMvc,
                                           PedidoRepository pedidoRepository,
                                           ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.pedidoRepository = pedidoRepository;
        this.objectMapper = objectMapper;
    }

    @Test
    void deveCriarPedidoComSucessoERetornar201() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(
                UUID.randomUUID(),
                List.of(new ItemPedidoDTO(UUID.randomUUID(), 2, new BigDecimal("10.50")))
        );

        mockMvc.perform(post("/pedido/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId", is(dto.clienteId().toString())))
                .andExpect(jsonPath("$.valorTotal", is(21.00)))
                .andExpect(jsonPath("$.status", is(PedidoStatus.CRIADO.name())));
    }

    @Test
    void deveRetornar404SePedidoNaoExistir() throws Exception {
        mockMvc.perform(get("/pedido/api/v1/pedidos/" + UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro", is("Pedido não encontrado")));
    }

}
