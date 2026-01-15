package com.gatcha.player.service;

import com.gatcha.player.model.Player;
import com.gatcha.player.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service gerant la logique metier des joueurs
 */
@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * Recupere le profil complet d'un joueur
     */
    public Player getPlayer(String username) {
        return playerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Joueur non trouve: " + username));
    }

    /**
     * Cree un nouveau joueur
     */
    public Player createPlayer(String username) {
        if (playerRepository.existsByUsername(username)) {
            throw new RuntimeException("Ce joueur existe deja");
        }
        Player player = new Player(username);
        return playerRepository.save(player);
    }

    /**
     * Recupere la liste des IDs de monstres du joueur
     */
    public List<String> getMonsterIds(String username) {
        Player player = getPlayer(username);
        return player.getMonsterIds();
    }

    /**
     * Recupere le niveau du joueur
     */
    public int getLevel(String username) {
        Player player = getPlayer(username);
        return player.getLevel();
    }

    /**
     * Ajoute de l'experience au joueur
     * Gere automatiquement le level up si necessaire
     */
    public Player addExperience(String username, int amount) {
        Player player = getPlayer(username);
        player.setExperience(player.getExperience() + amount);

        // Verifier si level up
        while (player.getExperience() >= player.getExperienceToNextLevel() && player.getLevel() < 50) {
            levelUp(player);
        }

        return playerRepository.save(player);
    }

    /**
     * Fait monter le joueur de niveau
     */
    private void levelUp(Player player) {
        // Soustraire l'XP necessaire
        player.setExperience(player.getExperience() - player.getExperienceToNextLevel());

        // Augmenter le niveau
        player.setLevel(player.getLevel() + 1);

        // Calculer le nouveau seuil (multiplier par 1.1)
        int newThreshold = (int) Math.round(player.getExperienceToNextLevel() * 1.1);
        player.setExperienceToNextLevel(newThreshold);
    }

    /**
     * Force le gain d'un niveau (pour tests ou admin)
     */
    public Player forceLevelUp(String username) {
        Player player = getPlayer(username);

        if (player.getLevel() >= 50) {
            throw new RuntimeException("Niveau maximum atteint");
        }

        player.setExperience(0);
        player.setLevel(player.getLevel() + 1);
        int newThreshold = (int) Math.round(player.getExperienceToNextLevel() * 1.1);
        player.setExperienceToNextLevel(newThreshold);

        return playerRepository.save(player);
    }

    /**
     * Ajoute un monstre au joueur
     */
    public Player addMonster(String username, String monsterId) {
        Player player = getPlayer(username);

        if (!player.canAddMonster()) {
            throw new RuntimeException("Liste de monstres pleine. Max: " + player.getMaxMonsters());
        }

        player.getMonsterIds().add(monsterId);
        return playerRepository.save(player);
    }

    /**
     * Supprime un monstre du joueur
     */
    public Player removeMonster(String username, String monsterId) {
        Player player = getPlayer(username);

        if (!player.getMonsterIds().contains(monsterId)) {
            throw new RuntimeException("Ce monstre n'appartient pas au joueur");
        }

        player.getMonsterIds().remove(monsterId);
        return playerRepository.save(player);
    }

    /**
     * Verifie si le joueur existe, sinon le cree
     */
    public Player getOrCreatePlayer(String username) {
        Optional<Player> playerOpt = playerRepository.findByUsername(username);
        if (playerOpt.isPresent()) {
            return playerOpt.get();
        }
        return createPlayer(username);
    }

    /**
     * Recupere tous les joueurs (pour l'arene)
     */
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    /**
     * Recupere un joueur par username (pour l'arene)
     */
    public Player getPlayerByUsername(String username) {
        return playerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Joueur non trouve: " + username));
    }
}
