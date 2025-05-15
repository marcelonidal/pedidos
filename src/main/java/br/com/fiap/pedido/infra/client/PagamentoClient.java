package br.com.fiap.pedido.infra.client;

import br.com.fiap.pedido.app.dto.StatusPagamentoResponseDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class PagamentoClient {

    private final RestTemplate restTemplate;

    public PagamentoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public StatusPagamentoResponseDTO consultarStatus(UUID pedidoId) {
        String url = "http://localhost:8084/api/pagamentos/pedido/" + pedidoId;

        try {
            return restTemplate.getForObject(url, StatusPagamentoResponseDTO.class);
        } catch (Exception e) {
            return null; // pagamento ainda nao processado ou erro temporario
        }
    }

}
