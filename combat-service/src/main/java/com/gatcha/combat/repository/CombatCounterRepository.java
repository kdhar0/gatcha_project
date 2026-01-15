package com.gatcha.combat.repository;

import com.gatcha.combat.model.CombatCounter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository pour le compteur de combats
 */
@Repository
public interface CombatCounterRepository extends MongoRepository<CombatCounter, String> {
}
