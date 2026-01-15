package com.gatcha.auth.repository;

import com.gatcha.auth.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour acceder aux utilisateurs dans MongoDB
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Trouve un utilisateur par son nom d'utilisateur
     */
    Optional<User> findByUsername(String username);

    /**
     * Verifie si un utilisateur existe
     */
    boolean existsByUsername(String username);
}
