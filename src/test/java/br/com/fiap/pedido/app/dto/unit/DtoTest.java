package br.com.fiap.pedido.app.dto.unit;

import br.com.fiap.pedido.app.dto.estoque.EstoqueRequestDTO;
import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pagamento.PagamentoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseMongoDTO;
import br.com.fiap.pedido.app.dto.produto.ProdutoResponseDTO;
import br.com.fiap.pedido.app.dto.shared.ErroPadraoDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DtoTest {

    @Test
    void shouldCreateEstoqueRequestDTOSuccessfully() {
        assertDoesNotThrow(() -> {
            // Simulacao de criacao do DTO (dados de exemplo podem ser ajustados)
            new EstoqueRequestDTO(
                    null, 0
            );
        });
    }

    @Test
    void shouldCreatePagamentoDTOSuccessfully() {
        assertDoesNotThrow(() -> {
            // Simulacao de criacao do DTO (dados de exemplo podem ser ajustados)
            new PagamentoDTO(
                    null, null, null, null, null
            );
        });
    }

    @Test
    void shouldCreatePagamentoRequestDTOSuccessfully() {
        assertDoesNotThrow(() -> {
            // Simulacao de criacao do DTO (dados de exemplo podem ser ajustados)
            new PagamentoRequestDTO(
                    null, null, null
            );
        });
    }

    @Test
    void shouldCreateItemPedidoDTOSuccessfully() {
        assertDoesNotThrow(() -> {
            // Simulacao de criacao do DTO (dados de exemplo podem ser ajustados)
            new ItemPedidoDTO(
                    null, 0, null
            );
        });
    }

    @Test
    void shouldCreatePedidoRequestDTOSuccessfully() {
        assertDoesNotThrow(() -> {
            // Simulacao de criacao do DTO (dados de exemplo podem ser ajustados)
            new PedidoRequestDTO(
                    null, null, null
            );
        });
    }

    @Test
    void shouldCreatePedidoResponseDTOSuccessfully() {
        assertDoesNotThrow(() -> {
            // Simulacao de criacao do DTO (dados de exemplo podem ser ajustados)
            new PedidoResponseDTO(
                    null, null, null, null, null, null, null
            );
        });
    }

    @Test
    void shouldCreatePedidoResponseMongoDTOSuccessfully() {
        assertDoesNotThrow(() -> {
            // Simulacao de criacao do DTO (dados de exemplo podem ser ajustados)
            new PedidoResponseMongoDTO(
                    null, null, null, null, null, null, null
            );
        });
    }

    @Test
    void shouldCreateProdutoResponseDTOSuccessfully() {
        assertDoesNotThrow(() -> {
            // Simulacao de criacao do DTO (dados de exemplo podem ser ajustados)
            new ProdutoResponseDTO(
                    null, null, null, null, null, null, 0, null, null
            );
        });
    }

    @Test
    void shouldCreateErroPadraoDTOSuccessfully() {
        assertDoesNotThrow(() -> {
            // Simulacao de criacao do DTO (dados de exemplo podem ser ajustados)
            new ErroPadraoDTO(
                    null, null, null, null
            );
        });
    }

}
