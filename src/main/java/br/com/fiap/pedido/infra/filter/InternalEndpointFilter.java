package br.com.fiap.pedido.infra.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class InternalEndpointFilter extends OncePerRequestFilter {

    private static final String INTERNAL_HEADER = "X-Internal-Call";
    private static final String INTERNAL_SECRET = "internal-secret";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/pedido/internal/")
                && !INTERNAL_SECRET.equals(request.getHeader(INTERNAL_HEADER))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
