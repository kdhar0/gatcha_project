package com.gatcha.auth.controller;

import com.gatcha.auth.model.LoginRequest;
import com.gatcha.auth.model.LoginResponse;
import com.gatcha.auth.model.User;
import com.gatcha.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour l'API d'authentification
 * Gere la connexion et la validation des tokens
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Endpoint pour se connecter
     * POST /api/auth/login
     * Body: { "username": "...", "password": "..." }
     * Retourne: { "token": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.authenticate(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Erreur d'authentification: " + e.getMessage());
        }
    }

    /**
     * Endpoint pour valider un token
     * GET /api/auth/validate?token=...
     * Retourne: le username si valide, erreur 401 sinon
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            String username = authService.validateToken(token);
            return ResponseEntity.ok(username);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token invalide: " + e.getMessage());
        }
    }

    /**
     * Endpoint pour creer un nouvel utilisateur
     * POST /api/auth/register
     * Body: { "username": "...", "password": "..." }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        try {
            User user = authService.createUser(request.getUsername(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Utilisateur cree avec succes: " + user.getUsername());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur lors de la creation: " + e.getMessage());
        }
    }

    /**
     * Endpoint pour revoquer un token (deconnexion)
     * POST /api/auth/logout?token=...
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String token) {
        try {
            authService.revokeToken(token);
            return ResponseEntity.ok("Deconnexion reussie");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur: " + e.getMessage());
        }
    }
}
