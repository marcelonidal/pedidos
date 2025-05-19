package br.com.fiap.pedido.infra.config;

import br.com.fiap.pedido.app.dto.shared.ErroPadraoDTO;
import br.com.fiap.pedido.core.domain.exception.PedidoNaoEncontradoException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PedidoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroPadraoDTO handlePedidoNaoEncontrado(PedidoNaoEncontradoException ex) {
        return new ErroPadraoDTO(
                UUID.randomUUID(),
                LocalDateTime.now(),
                "Pedido nao encontrado",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroPadraoDTO handleValidacao(MethodArgumentNotValidException ex) {
        String detalhe = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Requisicao invalida");

        return new ErroPadraoDTO(
                UUID.randomUUID(),
                LocalDateTime.now(),
                "Erro de validacao",
                detalhe
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroPadraoDTO handleJsonInvalido(HttpMessageNotReadableException ex) {
        return new ErroPadraoDTO(
                UUID.randomUUID(),
                LocalDateTime.now(),
                "Corpo da requisicao invalido",
                ex.getMostSpecificCause().getMessage()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErroPadraoDTO handleViolacaoRegra(ConstraintViolationException ex) {
        return new ErroPadraoDTO(
                UUID.randomUUID(),
                LocalDateTime.now(),
                "Violacao de regra de negocio",
                ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroPadraoDTO handleErroGeral(Exception ex) {
        return new ErroPadraoDTO(
                UUID.randomUUID(),
                LocalDateTime.now(),
                "Erro interno do servidor",
                ex.getMessage()
        );
    }

}
