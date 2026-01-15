package com.gatcha.monster.repository;

import com.gatcha.monster.model.Monster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour acceder aux monstres dans MongoDB
 */
@Repository
public interface MonsterRepository extends MongoRepository<Monster, String> {

    /**
     * Trouve tous les monstres d'un joueur
     */
    List<Monster> findByOwnerUsername(String ownerUsername);

    /**
     * Supprime tous les monstres d'un joueur
     */
    void deleteByOwnerUsername(String ownerUsername);
}
