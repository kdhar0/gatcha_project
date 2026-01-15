package com.gatcha.invocation.controller;

import com.gatcha.invocation.model.InvocationRecord;
import com.gatcha.invocation.service.AuthClientService;
import com.gatcha.invocation.service.InvocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller pour l'API Invocations
 */
@RestController
@RequestMapping("/api/invocation")
@CrossOrigin(origins = "*")
public class InvocationController {

    @Autowired
    private InvocationService invocationService;

    @Autowired
    private AuthClientService authClientService;

    /**
     * Effectue une invocation
     * POST /api/invocation/invoke
     */
    @PostMapping("/invoke")
    public ResponseEntity<?> invoke(@RequestHeader("Authorization") String token) {
        try {
            String username = authClientService.validateToken(token);
            Map<String, Object> result = invocationService.invoke(username);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Token")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Recupere l'historique des invocations
     * GET /api/invocation/history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestHeader("Authorization") String token) {
        try {
            String username = authClientService.validateToken(token);
            List<InvocationRecord> history = invocationService.getInvocationHistory(username);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Re-execute les invocations echouees
     * POST /api/invocation/retry
     */
    @PostMapping("/retry")
    public ResponseEntity<?> retryFailed() {
        try {
            int retried = invocationService.retryFailedInvocations();
            return ResponseEntity.ok(Map.of(
                "message", "Invocations re-executees",
                "count", retried
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
