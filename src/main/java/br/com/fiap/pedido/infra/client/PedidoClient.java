package br.com.fiap.pedido.infra.client;

import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class PedidoClient {

    private final RestTemplate restTemplate;

    public PedidoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PedidoResponseDTO criarPedido(PedidoRequestDTO dto) {
        String url = "http://localhost:8080/pedido/internal/api/v1/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Call", "internal-secret");

        HttpEntity<PedidoRequestDTO> request = new HttpEntity<>(dto, headers);

        ResponseEntity<PedidoResponseDTO> response =
                restTemplate.exchange(url, HttpMethod.POST, request, PedidoResponseDTO.class);

        return response.getBody();
    }

    public PedidoResponseDTO buscarPorId(UUID id) {
        String url = "http://localhost:8080/pedido/internal/api/v1/" + id;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Call", "internal-secret");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PedidoResponseDTO> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, PedidoResponseDTO.class);

        return response.getBody();
    }

}