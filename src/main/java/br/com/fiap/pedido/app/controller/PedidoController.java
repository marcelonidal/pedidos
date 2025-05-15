package br.com.fiap.pedido.app.controller;

import br.com.fiap.pedido.app.dto.AtualizacaoStatusPedidoDTO;
import br.com.fiap.pedido.app.dto.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.PedidoResponseDTO;
import br.com.fiap.pedido.core.domain.usecase.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;

    @Operation(summary = "Cria um novo pedido")
    @ApiResponse(responseCode = "200", description = "Pedido criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criar(@RequestBody @Valid PedidoRequestDTO dto) {
        return ResponseEntity.ok(service.criarPedido(dto));
    }

    @Operation(summary = "Busca um pedido pelo ID")
    @ApiResponse(responseCode = "200", description = "Pedido encontrado")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Lista todos os pedidos")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/paginado")
    @Operation(summary = "Listar pedidos paginados", description = "Retorna uma lista paginada de pedidos cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pedidos paginada retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Page<PedidoResponseDTO>> listarPaginado(
            @Parameter(description = "Informações de paginação") Pageable pageable) {
        return ResponseEntity.ok(service.listarPaginado(pageable));
    }

    @Operation(summary = "Atualiza o status de um pedido")
    @ApiResponse(responseCode = "204", description = "Status atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @ApiResponse(responseCode = "400", description = "Status inválido")
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(
            @PathVariable UUID id,
            @RequestBody @Valid AtualizacaoStatusPedidoDTO dto) {

        service.atualizarStatus(id, dto.status());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancela um pedido existente")
    @ApiResponse(responseCode = "204", description = "Pedido cancelado com sucesso")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        service.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

}