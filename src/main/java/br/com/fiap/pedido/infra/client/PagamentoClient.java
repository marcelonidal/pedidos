package br.com.fiap.pedido.infra.client;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pagamento.PagamentoRequestDTO;
import br.com.fiap.pedido.core.domain.exception.PagamentoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class PagamentoClient {

    private final RestTemplate restTemplate;

    @Autowired
    public PagamentoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PagamentoDTO solicitarPagamento(PagamentoRequestDTO dto) {
        String url = "http://localhost:8081/pagamentos/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PagamentoRequestDTO> request = new HttpEntity<>(dto, headers);

        try {
            ResponseEntity<PagamentoDTO> response = restTemplate.postForEntity(url, request, PagamentoDTO.class);
            return response.getBody();
        } catch (Exception e) {
            throw new PagamentoException("Erro ao solicitar pagamento: " + e.getMessage());
        }
    }

    public PagamentoDTO consultarStatus(UUID pedidoId) {
        String url = "http://localhost:8081/pagamentos/pedido/" + pedidoId;

        try {
            return restTemplate.getForObject(url, PagamentoDTO.class);
        } catch (Exception e) {
            return null; // pagamento ainda nao processado ou erro temporario
        }
    }

}
