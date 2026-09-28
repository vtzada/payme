package br.com.vitortheof.payme.shared.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LoginRateLimitFilterTest {

    private final LoginRateLimitFilter filter = new LoginRateLimitFilter();

    @Test
    @DisplayName("Deve bloquear a 11ª tentativa de login no mesmo minuto com 429")
    void deveBloquearAposLimite() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        int blocked = 0;
        for (int i = 0; i < LoginRateLimitFilter.MAX_ATTEMPTS_PER_MINUTE + 1; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
            request.setRemoteAddr("127.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, chain);

            if (response.getStatus() == 429) {
                blocked++;
            }
        }

        assertEquals(1, blocked);
        verify(chain, times(LoginRateLimitFilter.MAX_ATTEMPTS_PER_MINUTE)).doFilter(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Não deve limitar rotas diferentes de /auth/login")
    void naoDeveLimitarOutrasRotas() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < LoginRateLimitFilter.MAX_ATTEMPTS_PER_MINUTE + 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/accounts");
            request.setRemoteAddr("127.0.0.2");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, chain);
            assertEquals(200, response.getStatus());
        }

        verify(chain, times(LoginRateLimitFilter.MAX_ATTEMPTS_PER_MINUTE + 5)).doFilter(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
