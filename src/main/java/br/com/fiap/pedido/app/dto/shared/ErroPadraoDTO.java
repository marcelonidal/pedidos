package br.com.fiap.pedido.app.dto.shared;

import java.time.LocalDateTime;
import java.util.UUID;

public record ErroPadraoDTO(
        UUID idErro,
        LocalDateTime timestamp,
        String erro,
        String detalhe
) {}