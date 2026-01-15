package com.gatcha.player.repository;

import com.gatcha.player.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour acceder aux joueurs dans MongoDB
 */
@Repository
public interface PlayerRepository extends MongoRepository<Player, String> {

    /**
     * Trouve un joueur par son nom d'utilisateur
     */
    Optional<Player> findByUsername(String username);

    /**
     * Verifie si un joueur existe
     */
    boolean existsByUsername(String username);
}
