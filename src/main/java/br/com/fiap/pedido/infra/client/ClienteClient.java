package br.com.fiap.pedido.infra.client;

import br.com.fiap.pedido.core.domain.exception.ClienteNaoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class ClienteClient {

    private final RestTemplate restTemplate;

    @Autowired
    public ClienteClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void validarCliente(String clienteCpf) {
        try {
            restTemplate.getForEntity("http://localhost:8083/customer/v1/cliente/" + clienteCpf, Void.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ClienteNaoEncontradoException("Cliente com CPF " + clienteCpf + " nao encontrado");
            }
            throw new RuntimeException("Erro ao consultar cliente: " + e.getMessage());
        }
    }

}