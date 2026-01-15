package com.gatcha.monster.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Service client pour communiquer avec l'API d'authentification
 */
@Service
public class AuthClientService {

    @Value("${AUTH_SERVICE_URL:http://localhost:8081}")
    private String authServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Valide un token aupres de l'API d'authentification
     * Retourne le username si valide, lance une exception sinon
     */
    public String validateToken(String token) {
        try {
            String url = authServiceUrl + "/api/auth/validate?token=" + token;
            return restTemplate.getForObject(url, String.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new RuntimeException("Token invalide ou expire");
        } catch (Exception e) {
            throw new RuntimeException("Erreur de communication avec l'API auth: " + e.getMessage());
        }
    }
}
