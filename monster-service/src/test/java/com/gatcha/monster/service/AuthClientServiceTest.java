package com.gatcha.monster.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthClientService authClientService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authClientService, "authServiceUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(authClientService, "restTemplate", restTemplate);
    }

    @Test
    void validateToken_ok_returnsUsername() {
        String token = "validToken123";
        String expectedUsername = "testUser";
        String url = "http://localhost:8081/api/auth/validate?token=" + token;

        when(restTemplate.getForObject(url, String.class)).thenReturn(expectedUsername);

        String result = authClientService.validateToken(token);

        assertEquals(expectedUsername, result);
        verify(restTemplate).getForObject(url, String.class);
    }

    @Test
    void validateToken_unauthorized_throws() {
        String token = "invalidToken";
        String url = "http://localhost:8081/api/auth/validate?token=" + token;

        when(restTemplate.getForObject(url, String.class))
                .thenThrow(HttpClientErrorException.Unauthorized.class);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authClientService.validateToken(token));

        assertEquals("Token invalide ou expire", ex.getMessage());
    }

    @Test
    void validateToken_communicationError_throws() {
        String token = "someToken";
        String url = "http://localhost:8081/api/auth/validate?token=" + token;

        when(restTemplate.getForObject(url, String.class))
                .thenThrow(new RuntimeException("Connection refused"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authClientService.validateToken(token));

        assertTrue(ex.getMessage().contains("Erreur de communication avec l'API auth"));
    }

    @Test
    void validateToken_nullToken_callsApi() {
        String url = "http://localhost:8081/api/auth/validate?token=null";

        when(restTemplate.getForObject(url, String.class)).thenReturn("user");

        String result = authClientService.validateToken("null");

        assertEquals("user", result);
    }

    @Test
    void validateToken_emptyToken_callsApi() {
        String url = "http://localhost:8081/api/auth/validate?token=";

        when(restTemplate.getForObject(url, String.class))
                .thenThrow(HttpClientErrorException.Unauthorized.class);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authClientService.validateToken(""));

        assertEquals("Token invalide ou expire", ex.getMessage());
    }
}
