package br.com.fiap.pedido.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtil {

    public static BindingResult mockBindingResult() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "campo", "mensagem de erro");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        return bindingResult;
    }

}
