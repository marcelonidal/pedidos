package br.com.fiap.pedido.infra.config.unit;

import br.com.fiap.pedido.app.dto.shared.ErroPadraoDTO;
import br.com.fiap.pedido.core.domain.exception.PedidoNaoEncontradoException;
import br.com.fiap.pedido.infra.config.GlobalExceptionHandler;
import br.com.fiap.pedido.util.TestUtil;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandlePedidoNaoEncontrado() {
        PedidoNaoEncontradoException ex = new PedidoNaoEncontradoException("ID 999 nao encontrado");
        ErroPadraoDTO erro = handler.handlePedidoNaoEncontrado(ex);

        assertEquals("Pedido nao encontrado", erro.erro());
        assertEquals("ID 999 nao encontrado", erro.detalhe());
        assertNotNull(erro.timestamp());
        assertNotNull(erro.idErro());
    }

    @Test
    void shouldHandleValidacao() throws NoSuchMethodException {
        BindingResult bindingResult = TestUtil.mockBindingResult();

        // Criar MethodParameter fake usando reflexão
        Method method = String.class.getMethod("toString");
        MethodParameter methodParameter = new MethodParameter(method, -1);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ErroPadraoDTO erro = handler.handleValidacao(ex);

        assertEquals("Erro de validacao", erro.erro());
        assertTrue(erro.detalhe().contains("campo"));

    }

    @Test
    void shouldHandleJsonInvalido() {
        Throwable cause = new RuntimeException("JSON mal formatado");
        HttpInputMessage input = mock(HttpInputMessage.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Erro de parse", cause, input);

        ErroPadraoDTO erro = handler.handleJsonInvalido(ex);
        assertEquals("Corpo da requisicao invalido", erro.erro());
        assertEquals("JSON mal formatado", erro.detalhe());
    }

    @Test
    void shouldHandleViolacaoRegra() {
        ConstraintViolationException ex = new ConstraintViolationException("Regra violada", null);
        ErroPadraoDTO erro = handler.handleViolacaoRegra(ex);

        assertEquals("Violacao de regra de negocio", erro.erro());
        assertEquals("Regra violada", erro.detalhe());
    }

    @Test
    void shouldHandleErroGeral() {
        Exception ex = new Exception("Erro inesperado");
        ErroPadraoDTO erro = handler.handleErroGeral(ex);

        assertEquals("Erro interno do servidor", erro.erro());
        assertEquals("Erro inesperado", erro.detalhe());
    }

}
