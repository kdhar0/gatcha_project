package com.gatcha.invocation.repository;

import com.gatcha.invocation.model.InvocationRecord;
import com.gatcha.invocation.model.InvocationRecord.InvocationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour les enregistrements d'invocation
 */
@Repository
public interface InvocationRecordRepository extends MongoRepository<InvocationRecord, String> {

    /**
     * Trouve toutes les invocations d'un joueur
     */
    List<InvocationRecord> findByUsername(String username);

    /**
     * Trouve les invocations par statut
     */
    List<InvocationRecord> findByStatus(InvocationStatus status);

    /**
     * Trouve les invocations incompletes (pour la recreation)
     */
    List<InvocationRecord> findByStatusNot(InvocationStatus status);
}
