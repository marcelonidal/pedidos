package br.com.fiap.pedido.app.controller.unit;

import br.com.fiap.pedido.app.controller.PedidoController;
import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.core.domain.model.StatusPagamento;
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
    void shouldCreatePedidoSuccessfully() throws Exception {
        UUID clienteId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        PedidoRequestDTO dto = new PedidoRequestDTO(
                clienteId,
                List.of(new ItemPedidoDTO(produtoId, 2, new BigDecimal("10.00"))),
                pagamentoId
        );

        PagamentoDTO pagamento = new PagamentoDTO(
                pagamentoId,
                StatusPagamento.AGUARDANDO,
                "CARTAO_CREDITO",
                LocalDateTime.now()
        );

        PedidoResponseDTO resposta = new PedidoResponseDTO(
                UUID.randomUUID(),
                clienteId,
                LocalDateTime.now(),
                "CRIADO",
                new BigDecimal("20.00"),
                dto.itens(),
                pagamento
        );

        Mockito.when(pedidoService.criarPedido(any())).thenReturn(resposta);

        mockMvc.perform(post("/internal/api/v1/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.valorTotal").value(20.00))
                .andExpect(jsonPath("$.pagamento.statusPagamento").value("AGUARDANDO"))
                .andExpect(jsonPath("$.pagamento.metodoPagamento").value("CARTAO_CREDITO"));
    }

    @Test
    void shouldReturnPedidoById() throws Exception {
        UUID pedidoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();

        List<ItemPedidoDTO> itens = List.of(
                new ItemPedidoDTO(UUID.randomUUID(), 1, new BigDecimal("50.00"))
        );

        PagamentoDTO pagamento = new PagamentoDTO(
                pagamentoId,
                StatusPagamento.AGUARDANDO,
                "CARTAO_CREDITO",
                LocalDateTime.now()
        );

        PedidoResponseDTO resposta = new PedidoResponseDTO(
                pedidoId,
                clienteId,
                LocalDateTime.now(),
                "CRIADO",
                new BigDecimal("50.00"),
                itens,
                pagamento
        );

        Mockito.when(pedidoService.buscarPorId(pedidoId)).thenReturn(resposta);

        mockMvc.perform(get("/internal/api/v1/" + pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId.toString()))
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.valorTotal").value(50.00))
                .andExpect(jsonPath("$.pagamento.statusPagamento").value("AGUARDANDO"))
                .andExpect(jsonPath("$.pagamento.metodoPagamento").value("CARTAO_CREDITO"));
    }

    @Test
    void shouldListAllPedidos() throws Exception {
        UUID pedidoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        List<ItemPedidoDTO> itens = List.of(
                new ItemPedidoDTO(produtoId, 1, new BigDecimal("50.00"))
        );

        PagamentoDTO pagamento = new PagamentoDTO(
                pagamentoId,
                StatusPagamento.AGUARDANDO,
                "CARTAO_CREDITO",
                LocalDateTime.now()
        );

        PedidoResponseDTO pedido = new PedidoResponseDTO(
                pedidoId,
                clienteId,
                LocalDateTime.now(),
                "CRIADO",
                new BigDecimal("50.00"),
                itens,
                pagamento
        );

        Mockito.when(pedidoService.listarTodos()).thenReturn(List.of(pedido));

        mockMvc.perform(get("/internal/api/v1/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pedidoId.toString()))
                .andExpect(jsonPath("$[0].status").value("CRIADO"))
                .andExpect(jsonPath("$[0].valorTotal").value(50.00))
                .andExpect(jsonPath("$[0].pagamento.statusPagamento").value("AGUARDANDO"))
                .andExpect(jsonPath("$[0].pagamento.metodoPagamento").value("CARTAO_CREDITO"));
    }

    @Test
    void shouldCancelPedidoById() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.doNothing().when(pedidoService).cancelarPedido(id);

        mockMvc.perform(put("/internal/api/v1/" + id + "/cancelar"))
                .andExpect(status().isNoContent());

        Mockito.verify(pedidoService).cancelarPedido(id);
    }

}