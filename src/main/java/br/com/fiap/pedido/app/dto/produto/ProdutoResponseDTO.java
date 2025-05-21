package br.com.fiap.pedido.app.dto.produto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponseDTO(
        UUID produtoId,
        String nome,
        String marca,
        String cor,
        String tamanho,
        String faixaEtaria,
        int quantidadePecas,
        BigDecimal preco,
        String skuDoProduto
) {}
