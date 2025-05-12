package br.com.fiap.pedido.app.controller.unit;

import br.com.fiap.pedido.app.controller.PedidoController;
import br.com.fiap.pedido.app.dto.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.PedidoResponseDTO;
import br.com.fiap.pedido.core.domain.usecase.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
@Import(PedidoControllerTest.PedidoServiceMockConfig.class)
class PedidoControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final PedidoService pedidoService;

    @Autowired
    PedidoControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, PedidoService pedidoService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.pedidoService = pedidoService;
    }

    @TestConfiguration
    static class PedidoServiceMockConfig {
        @Bean
        public PedidoService pedidoService() {
            return Mockito.mock(PedidoService.class);
        }
    }

    @Test
    void deveCriarPedido() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(
                UUID.randomUUID(),
                List.of(new ItemPedidoDTO(UUID.randomUUID(), 2, new BigDecimal("10.00")))
        );

        PedidoResponseDTO resposta = new PedidoResponseDTO(
                UUID.randomUUID(),
                dto.clienteId(),
                LocalDateTime.now(),
                "CRIADO",
                new BigDecimal("20.00"),
                dto.itens()
        );

        Mockito.when(pedidoService.criarPedido(any())).thenReturn(resposta);

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.valorTotal").value(20.00));
    }

    @Test
    void deveBuscarPedidoPorId() throws Exception {
        UUID id = UUID.randomUUID();

        PedidoResponseDTO resposta = new PedidoResponseDTO(
                id,
                UUID.randomUUID(),
                LocalDateTime.now(),
                "CRIADO",
                new BigDecimal("100.00"),
                Collections.emptyList()
        );

        Mockito.when(pedidoService.buscarPorId(id)).thenReturn(resposta);

        mockMvc.perform(get("/api/v1/pedidos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("CRIADO"));
    }

    @Test
    void deveListarPedidos() throws Exception {
        PedidoResponseDTO pedido = new PedidoResponseDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                "CRIADO",
                new BigDecimal("50.00"),
                Collections.emptyList()
        );

        Mockito.when(pedidoService.listarTodos()).thenReturn(Collections.singletonList(pedido));

        mockMvc.perform(get("/api/v1/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CRIADO"));
    }

    @Test
    void deveCancelarPedido() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/pedidos/" + id + "/cancelar"))
                .andExpect(status().isNoContent());

        Mockito.verify(pedidoService).cancelarPedido(id);
    }

}