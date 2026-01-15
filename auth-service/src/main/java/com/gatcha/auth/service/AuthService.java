package com.gatcha.auth.service;

import com.gatcha.auth.model.Token;
import com.gatcha.auth.model.User;
import com.gatcha.auth.repository.TokenRepository;
import com.gatcha.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

/**
 * Service gerant l'authentification des utilisateurs
 * Genere et valide les tokens
 */
@Service
public class AuthService {

    // Cle secrete pour l'encryption AES (doit faire 16 caracteres)
    private static final String SECRET_KEY = "GatchaSecretKey!";
    private static final String ALGORITHM = "AES";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    /**
     * Authentifie un utilisateur et genere un token
     * Format du token: username-date(YYYY/MM/DD)-heure(HH:mm:ss) encrypte en AES
     */
    public String authenticate(String username, String password) {
        // Verifier que l'utilisateur existe et que le mot de passe est correct
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouve");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        // Supprimer les anciens tokens de cet utilisateur
        tokenRepository.deleteByUsername(username);

        // Generer le nouveau token
        String tokenValue = generateToken(username);

        // Sauvegarder le token avec une expiration d'1 heure
        Token token = new Token(
            tokenValue,
            username,
            LocalDateTime.now().plusHours(1)
        );
        tokenRepository.save(token);

        return tokenValue;
    }

    /**
     * Valide un token et retourne le username associe
     * Met a jour la date d'expiration si le token est valide
     */
    public String validateToken(String tokenValue) {
        Optional<Token> tokenOpt = tokenRepository.findByToken(tokenValue);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Token non trouve");
        }

        Token token = tokenOpt.get();

        // Verifier si le token n'est pas expire
        if (!token.isValid()) {
            tokenRepository.delete(token);
            throw new RuntimeException("Token expire");
        }

        // Mettre a jour la date d'expiration (maintenant + 1 heure)
        token.setExpirationDate(LocalDateTime.now().plusHours(1));
        tokenRepository.save(token);

        return token.getUsername();
    }

    /**
     * Cree un nouvel utilisateur
     */
    public User createUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Cet utilisateur existe deja");
        }

        User user = new User(username, password);
        return userRepository.save(user);
    }

    /**
     * Revoque un token (deconnexion)
     */
    public void revokeToken(String tokenValue) {
        Optional<Token> tokenOpt = tokenRepository.findByToken(tokenValue);
        if (tokenOpt.isPresent()) {
            tokenRepository.delete(tokenOpt.get());
        }
    }

    /**
     * Genere un token encrypte avec le format: username-date-heure
     */
    private String generateToken(String username) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss");
        String tokenData = username + "-" + now.format(formatter);

        try {
            return encrypt(tokenData);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la generation du token", e);
        }
    }

    /**
     * Encrypte une chaine avec AES
     * Utilise Base64 URL-safe pour eviter les problemes avec les caracteres +, / et =
     */
    private String encrypt(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);
    }

    /**
     * Decrypte une chaine avec AES
     * Utilise Base64 URL-safe pour eviter les problemes avec les caracteres +, / et =
     */
    private String decrypt(String encryptedData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getUrlDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }
}
