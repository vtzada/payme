package br.com.vitortheof.payme.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate-limit simples em memória para o endpoint de login.
 * Limite padrão: 10 tentativas por minuto por IP. Excedeu -> 429.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    static final int MAX_ATTEMPTS_PER_MINUTE = 10;
    static final long WINDOW_MS = 60_000L;

    private final Map<String, List<Long>> attemptsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isLoginRequest(request) && !allow(request)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Muitas tentativas de login. Tente novamente em instantes.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod()) && "/auth/login".equals(request.getRequestURI());
    }

    private boolean allow(HttpServletRequest request) {
        String key = clientIp(request);
        long now = System.currentTimeMillis();
        List<Long> attempts = attemptsByIp.computeIfAbsent(key, k -> new ArrayList<>());
        synchronized (attempts) {
            attempts.removeIf(ts -> now - ts > WINDOW_MS);
            if (attempts.size() >= MAX_ATTEMPTS_PER_MINUTE) {
                return false;
            }
            attempts.add(now);
            return true;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
