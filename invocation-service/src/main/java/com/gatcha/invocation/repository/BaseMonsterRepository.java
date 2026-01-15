package com.gatcha.invocation.repository;

import com.gatcha.invocation.model.BaseMonster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour les monstres de base
 */
@Repository
public interface BaseMonsterRepository extends MongoRepository<BaseMonster, Integer> {

    /**
     * Trouve un monstre de base par son ID numerique
     */
    Optional<BaseMonster> findByBaseId(Integer baseId);
}
