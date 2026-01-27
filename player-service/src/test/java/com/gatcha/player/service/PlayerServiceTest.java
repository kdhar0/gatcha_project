package com.gatcha.player.service;

import com.gatcha.player.model.Player;
import com.gatcha.player.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("testUser");
        player.setId("player123");
    }

    // ========== Tests pour getPlayer ==========

    @Test
    void getPlayer_ok_returnsPlayer() {
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));

        Player result = playerService.getPlayer("testUser");

        assertEquals("testUser", result.getUsername());
        verify(playerRepository).findByUsername("testUser");
    }

    @Test
    void getPlayer_notFound_throws() {
        when(playerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> playerService.getPlayer("unknown"));

        assertEquals("Joueur non trouve: unknown", ex.getMessage());
    }

    // ========== Tests pour createPlayer ==========

    @Test
    void createPlayer_ok_savesAndReturnsPlayer() {
        when(playerRepository.existsByUsername("newUser")).thenReturn(false);
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player created = playerService.createPlayer("newUser");

        assertEquals("newUser", created.getUsername());
        assertEquals(0, created.getLevel());
        assertEquals(0, created.getExperience());
        assertEquals(50, created.getExperienceToNextLevel());
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void createPlayer_alreadyExists_throws() {
        when(playerRepository.existsByUsername("testUser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> playerService.createPlayer("testUser"));

        assertEquals("Ce joueur existe deja", ex.getMessage());
        verify(playerRepository, never()).save(any());
    }

    // ========== Tests pour getMonsterIds ==========

    @Test
    void getMonsterIds_ok_returnsList() {
        player.getMonsterIds().add("monster1");
        player.getMonsterIds().add("monster2");
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));

        List<String> ids = playerService.getMonsterIds("testUser");

        assertEquals(2, ids.size());
        assertTrue(ids.contains("monster1"));
        assertTrue(ids.contains("monster2"));
    }

    @Test
    void getMonsterIds_emptyList_returnsEmpty() {
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));

        List<String> ids = playerService.getMonsterIds("testUser");

        assertTrue(ids.isEmpty());
    }

    // ========== Tests pour getLevel ==========

    @Test
    void getLevel_ok_returnsLevel() {
        player.setLevel(5);
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));

        int level = playerService.getLevel("testUser");

        assertEquals(5, level);
    }

    // ========== Tests pour addExperience ==========

    @Test
    void addExperience_noLevelUp_addsXpOnly() {
        player.setExperience(10);
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = playerService.addExperience("testUser", 20);

        assertEquals(30, result.getExperience());
        assertEquals(0, result.getLevel());
        verify(playerRepository).save(player);
    }

    @Test
    void addExperience_withLevelUp_increasesLevel() {
        player.setExperience(0);
        player.setExperienceToNextLevel(50);
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = playerService.addExperience("testUser", 60);

        assertEquals(1, result.getLevel());
        assertEquals(10, result.getExperience());
        assertEquals(55, result.getExperienceToNextLevel()); // 50 * 1.1 = 55
        verify(playerRepository).save(player);
    }

    @Test
    void addExperience_multipleLevelUps_increasesMultipleLevels() {
        player.setExperience(0);
        player.setExperienceToNextLevel(50);
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 50 pour niveau 1, 55 pour niveau 2 = 105 total
        Player result = playerService.addExperience("testUser", 110);

        assertEquals(2, result.getLevel());
        verify(playerRepository).save(player);
    }

    @Test
    void addExperience_maxLevel_stopsAt50() {
        player.setLevel(49);
        player.setExperience(0);
        player.setExperienceToNextLevel(100);
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = playerService.addExperience("testUser", 500);

        assertEquals(50, result.getLevel());
        verify(playerRepository).save(player);
    }

    // ========== Tests pour forceLevelUp ==========

    @Test
    void forceLevelUp_ok_increasesLevel() {
        player.setLevel(5);
        player.setExperience(30);
        player.setExperienceToNextLevel(100);
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = playerService.forceLevelUp("testUser");

        assertEquals(6, result.getLevel());
        assertEquals(0, result.getExperience());
        assertEquals(110, result.getExperienceToNextLevel()); // 100 * 1.1
        verify(playerRepository).save(player);
    }

    @Test
    void forceLevelUp_maxLevel_throws() {
        player.setLevel(50);
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> playerService.forceLevelUp("testUser"));

        assertEquals("Niveau maximum atteint", ex.getMessage());
        verify(playerRepository, never()).save(any());
    }

    // ========== Tests pour addMonster ==========

    @Test
    void addMonster_ok_addsMonsterToList() {
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = playerService.addMonster("testUser", "monster123");

        assertTrue(result.getMonsterIds().contains("monster123"));
        verify(playerRepository).save(player);
    }

    @Test
    void addMonster_listFull_throws() {
        // Niveau 0 = max 10 monstres
        player.setLevel(0);
        for (int i = 0; i < 10; i++) {
            player.getMonsterIds().add("monster" + i);
        }
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> playerService.addMonster("testUser", "newMonster"));

        assertEquals("Liste de monstres pleine. Max: 10", ex.getMessage());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void addMonster_higherLevel_allowsMoreMonsters() {
        player.setLevel(5); // max = 10 + 5 = 15
        for (int i = 0; i < 10; i++) {
            player.getMonsterIds().add("monster" + i);
        }
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = playerService.addMonster("testUser", "newMonster");

        assertEquals(11, result.getMonsterIds().size());
    }

    // ========== Tests pour removeMonster ==========

    @Test
    void removeMonster_ok_removesMonsterFromList() {
        player.getMonsterIds().add("monster123");
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = playerService.removeMonster("testUser", "monster123");

        assertFalse(result.getMonsterIds().contains("monster123"));
        verify(playerRepository).save(player);
    }

    @Test
    void removeMonster_notOwned_throws() {
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> playerService.removeMonster("testUser", "unknownMonster"));

        assertEquals("Ce monstre n'appartient pas au joueur", ex.getMessage());
        verify(playerRepository, never()).save(any());
    }

    // ========== Tests pour getOrCreatePlayer ==========

    @Test
    void getOrCreatePlayer_exists_returnsExisting() {
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));

        Player result = playerService.getOrCreatePlayer("testUser");

        assertEquals("testUser", result.getUsername());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void getOrCreatePlayer_notExists_createsNew() {
        when(playerRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(playerRepository.existsByUsername("newUser")).thenReturn(false);
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = playerService.getOrCreatePlayer("newUser");

        assertEquals("newUser", result.getUsername());
        verify(playerRepository).save(any(Player.class));
    }

    // ========== Tests pour getAllPlayers ==========

    @Test
    void getAllPlayers_ok_returnsAllPlayers() {
        Player player2 = new Player("user2");
        List<Player> players = Arrays.asList(player, player2);
        when(playerRepository.findAll()).thenReturn(players);

        List<Player> result = playerService.getAllPlayers();

        assertEquals(2, result.size());
        verify(playerRepository).findAll();
    }

    @Test
    void getAllPlayers_empty_returnsEmptyList() {
        when(playerRepository.findAll()).thenReturn(new ArrayList<>());

        List<Player> result = playerService.getAllPlayers();

        assertTrue(result.isEmpty());
    }

    // ========== Tests pour getPlayerByUsername ==========

    @Test
    void getPlayerByUsername_ok_returnsPlayer() {
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));

        Player result = playerService.getPlayerByUsername("testUser");

        assertEquals("testUser", result.getUsername());
    }

    @Test
    void getPlayerByUsername_notFound_throws() {
        when(playerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> playerService.getPlayerByUsername("unknown"));

        assertEquals("Joueur non trouve: unknown", ex.getMessage());
    }
}
