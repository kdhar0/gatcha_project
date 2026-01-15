package com.gatcha.auth.repository;

import com.gatcha.auth.model.Token;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour acceder aux tokens dans MongoDB
 */
@Repository
public interface TokenRepository extends MongoRepository<Token, String> {

    /**
     * Trouve un token par sa valeur
     */
    Optional<Token> findByToken(String token);

    /**
     * Trouve un token par le nom d'utilisateur
     */
    Optional<Token> findByUsername(String username);

    /**
     * Supprime tous les tokens d'un utilisateur
     */
    void deleteByUsername(String username);
}
