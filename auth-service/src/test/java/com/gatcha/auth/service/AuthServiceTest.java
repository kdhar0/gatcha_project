package com.gatcha.auth.service;

import com.gatcha.auth.model.Token;
import com.gatcha.auth.model.User;
import com.gatcha.auth.repository.TokenRepository;
import com.gatcha.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("john", "password");
    }


    @Test
    void authenticate_ok_generatesToken_andSavesIt() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String tokenValue = authService.authenticate("john", "password");

        assertNotNull(tokenValue);
        verify(tokenRepository).deleteByUsername("john");
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void authenticate_userNotFound_throws() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.authenticate("unknown", "pwd"));

        assertEquals("Utilisateur non trouve", ex.getMessage());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void authenticate_badPassword_throws() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.authenticate("john", "wrong"));

        assertEquals("Mot de passe incorrect", ex.getMessage());
        verify(tokenRepository, never()).save(any());
    }


    @Test
    void validateToken_ok_returnsUsername_andExtendsExpiration() {
        Token token = new Token("abc", "john", LocalDateTime.now().plusMinutes(10));
        // suppose que Token.isValid() vérifie expirationDate > now
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String username = authService.validateToken("abc");

        assertEquals("john", username);
        verify(tokenRepository).save(token);
    }

    @Test
    void validateToken_notFound_throws() {
        when(tokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.validateToken("unknown"));

        assertEquals("Token non trouve", ex.getMessage());
    }

    @Test
    void validateToken_expired_throws_andDeletesToken() {
        Token token = new Token("abc", "john", LocalDateTime.now().minusMinutes(1));
        // isValid() doit renvoyer false dans ce cas
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.validateToken("abc"));

        assertEquals("Token expire", ex.getMessage());
        verify(tokenRepository).delete(token);
        verify(tokenRepository, never()).save(any());
    }


    @Test
    void createUser_ok_savesUser() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = authService.createUser("john", "password");

        assertEquals("john", created.getUsername());
        assertEquals("password", created.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_alreadyExists_throws() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.createUser("john", "password"));

        assertEquals("Cet utilisateur existe deja", ex.getMessage());
        verify(userRepository, never()).save(any());
    }


    @Test
    void revokeToken_tokenPresent_deletesIt() {
        Token token = new Token("abc", "john", LocalDateTime.now().plusMinutes(10));
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        authService.revokeToken("abc");

        verify(tokenRepository).delete(token);
    }

    @Test
    void revokeToken_tokenAbsent_doesNothing() {
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.empty());

        authService.revokeToken("abc");

        verify(tokenRepository, never()).delete(any());
    }
}
