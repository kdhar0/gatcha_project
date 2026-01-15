package com.gatcha.combat.repository;

import com.gatcha.combat.model.Combat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour les combats
 */
@Repository
public interface CombatRepository extends MongoRepository<Combat, String> {

    /**
     * Trouve un combat par son numero
     */
    Optional<Combat> findByCombatNumber(int combatNumber);

    /**
     * Trouve tous les combats d'un joueur
     */
    List<Combat> findByFighter1OwnerOrFighter2Owner(String owner1, String owner2);

    /**
     * Trouve les combats gagnes par un joueur
     */
    List<Combat> findByWinnerOwner(String owner);

    /**
     * Tous les combats tries par date
     */
    List<Combat> findAllByOrderByTimestampDesc();
}
