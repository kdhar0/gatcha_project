// Configuration des URLs des APIs
const API_BASE = {
    auth: 'http://localhost:8081/api/auth',
    player: 'http://localhost:8082/api/player',
    monsters: 'http://localhost:8083/api/monsters',
    invocation: 'http://localhost:8084/api/invocation',
    combat: 'http://localhost:8085/api/combat'
};

// Variables globales
let authToken = null;
let currentUsername = null;
let currentMonsterId = null;
let monstersData = [];
let lastCombatNumber = null;
let replayData = null;
let replayCurrentTurn = 0;
let replayAutoInterval = null;

// Variables pour l'arene PvP
let arenaSelectedMonsters = { 1: null, 2: null };
let arenaPlayersMonsters = { 1: [], 2: [] };

// Elements du DOM
const loginSection = document.getElementById('login-section');
const gameSection = document.getElementById('game-section');
const loginForm = document.getElementById('login-form');
const loginMessage = document.getElementById('login-message');
const monsterModal = document.getElementById('monster-modal');
const replayModal = document.getElementById('replay-modal');

// ==================== INITIALISATION ====================

document.addEventListener('DOMContentLoaded', () => {
    const savedToken = localStorage.getItem('authToken');
    const savedUsername = localStorage.getItem('username');

    if (savedToken && savedUsername) {
        authToken = savedToken;
        currentUsername = savedUsername;
        showGameSection();
    }
});

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    await login();
});

// ==================== AUTHENTIFICATION ====================

async function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${API_BASE.auth}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const data = await response.json();
            authToken = data.token;
            currentUsername = username;
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('username', username);
            showMessage('Connexion reussie!', 'success');
            showGameSection();
        } else {
            const error = await response.text();
            showMessage('Erreur: ' + error, 'error');
        }
    } catch (error) {
        showMessage('Erreur de connexion au serveur', 'error');
    }
}

async function register() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (!username || !password) {
        showMessage('Veuillez remplir tous les champs', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE.auth}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            showMessage('Compte cree! Vous pouvez vous connecter.', 'success');
        } else {
            const error = await response.text();
            showMessage('Erreur: ' + error, 'error');
        }
    } catch (error) {
        showMessage('Erreur de connexion au serveur', 'error');
    }
}

async function logout() {
    // Revoquer le token cote serveur
    if (authToken) {
        try {
            await fetch(`${API_BASE.auth}/logout?token=${encodeURIComponent(authToken)}`, {
                method: 'POST'
            });
        } catch (error) {
            console.error('Erreur lors de la revocation du token:', error);
        }
    }

    // Nettoyer cote client
    authToken = null;
    currentUsername = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    loginSection.classList.remove('hidden');
    gameSection.classList.add('hidden');
    document.getElementById('login-form').reset();
}

// ==================== NAVIGATION ====================

function showTab(tabName) {
    // Cacher tous les contenus
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.add('hidden'));
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));

    // Afficher le tab selectionne
    document.getElementById(`tab-${tabName}`).classList.remove('hidden');
    event.target.classList.add('active');

    // Charger les donnees specifiques au tab
    if (tabName === 'arena') {
        loadPlayersForArena();
    } else if (tabName === 'history') {
        loadCombatHistory();
    }
}

async function showGameSection() {
    loginSection.classList.add('hidden');
    gameSection.classList.remove('hidden');
    document.getElementById('player-name').textContent = currentUsername;
    await loadPlayerData();
    await loadMonsters();
}

// ==================== DONNEES JOUEUR ====================

async function loadPlayerData() {
    try {
        const response = await fetch(`${API_BASE.player}/profile`, {
            headers: { 'Authorization': authToken }
        });

        if (response.ok) {
            const player = await response.json();
            document.getElementById('player-level').textContent = player.level;
            document.getElementById('player-exp').textContent = player.experience;
            document.getElementById('player-exp-max').textContent = player.experienceToNextLevel;
            document.getElementById('player-monsters-count').textContent = player.monsterIds ? player.monsterIds.length : 0;
            document.getElementById('player-monsters-max').textContent = player.level + 10;

            const xpPercent = (player.experience / player.experienceToNextLevel) * 100;
            document.getElementById('player-xp-bar').style.width = xpPercent + '%';
        } else if (response.status === 401) {
            logout();
        }
    } catch (error) {
        console.error('Erreur:', error);
    }
}

/**
 * Ajoute de l'experience au joueur
 */
async function addPlayerXP(amount) {
    console.log('addPlayerXP appelé avec amount:', amount);
    console.log('Token:', authToken);
    try {
        const response = await fetch(`${API_BASE.player}/experience?amount=${amount}`, {
            method: 'POST',
            headers: { 'Authorization': authToken }
        });

        console.log('Response status:', response.status);
        if (response.ok) {
            const player = await response.json();
            console.log('Player updated:', player);
            await loadPlayerData();
            alert('XP ajouté! Nouveau total: ' + player.experience + '/' + player.experienceToNextLevel);
        } else if (response.status === 401) {
            alert('Session expirée, reconnexion...');
            logout();
        } else {
            const error = await response.text();
            alert('Erreur: ' + error);
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur de connexion: ' + error.message);
    }
}

/**
 * Force le gain d'un niveau pour le joueur
 */
async function playerLevelUp() {
    console.log('playerLevelUp appelé');
    try {
        const response = await fetch(`${API_BASE.player}/levelup`, {
            method: 'POST',
            headers: { 'Authorization': authToken }
        });

        console.log('Response status:', response.status);
        if (response.ok) {
            const player = await response.json();
            console.log('Player updated:', player);
            await loadPlayerData();
            alert('Level up! Nouveau niveau: ' + player.level);
        } else if (response.status === 401) {
            alert('Session expirée, reconnexion...');
            logout();
        } else {
            const error = await response.text();
            alert('Erreur: ' + error);
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur de connexion: ' + error.message);
    }
}

// ==================== MONSTRES ====================

async function loadMonsters() {
    try {
        const response = await fetch(`${API_BASE.monsters}`, {
            headers: { 'Authorization': authToken }
        });

        if (response.ok) {
            monstersData = await response.json();
            displayMonsters(monstersData);
        }
    } catch (error) {
        console.error('Erreur:', error);
    }
}

function displayMonsters(monsters) {
    const container = document.getElementById('monsters-container');

    if (monsters.length === 0) {
        container.innerHTML = '<p>Vous n\'avez pas encore de monstres. Invoquez-en un!</p>';
        return;
    }

    container.innerHTML = monsters.map(monster => `
        <div class="monster-card" onclick="openMonsterModal('${monster.id}')">
            <div class="element ${monster.element}">${getElementEmoji(monster.element)}</div>
            <div class="name">Monstre #${monster.baseId}</div>
            <div class="level">Niveau ${monster.level}</div>
            <div class="stats">HP: ${monster.hp} | ATK: ${monster.atk}</div>
        </div>
    `).join('');
}

// ==================== INVOCATION ====================

async function invokeMonster() {
    const invokeBtn = document.getElementById('invoke-btn');
    invokeBtn.disabled = true;
    invokeBtn.textContent = 'Invocation...';

    try {
        const response = await fetch(`${API_BASE.invocation}/invoke`, {
            method: 'POST',
            headers: { 'Authorization': authToken }
        });

        if (response.ok) {
            const result = await response.json();
            displayInvocationResult(result);
            await loadPlayerData();
            await loadMonsters();
        } else if (response.status === 401) {
            logout();
        } else {
            const error = await response.text();
            alert('Erreur: ' + error);
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur de connexion');
    } finally {
        invokeBtn.disabled = false;
        invokeBtn.textContent = 'Invoquer!';
    }
}

function displayInvocationResult(result) {
    const resultDiv = document.getElementById('invocation-result');
    const detailsDiv = document.getElementById('monster-details');
    resultDiv.classList.remove('hidden');

    const monster = result.monster;
    detailsDiv.innerHTML = `
        <p class="${monster.element}"><strong>Element:</strong> ${getElementEmoji(monster.element)} ${monster.element}</p>
        <p><strong>HP:</strong> ${monster.hp}</p>
        <p><strong>ATK:</strong> ${monster.atk}</p>
        <p><strong>DEF:</strong> ${monster.def}</p>
        <p><strong>VIT:</strong> ${monster.vit}</p>
    `;
}

// ==================== MODAL MONSTRE ====================

function openMonsterModal(monsterId) {
    currentMonsterId = monsterId;
    const monster = monstersData.find(m => m.id === monsterId);
    if (!monster) return;

    document.getElementById('modal-title').textContent = `Monstre #${monster.baseId}`;
    document.getElementById('modal-element').innerHTML = `${getElementEmoji(monster.element)} ${monster.element}`;
    document.getElementById('modal-element').className = `stat-value ${monster.element}`;
    document.getElementById('modal-level').textContent = monster.level;
    document.getElementById('modal-hp').textContent = monster.hp;
    document.getElementById('modal-atk').textContent = monster.atk;
    document.getElementById('modal-def').textContent = monster.def;
    document.getElementById('modal-vit').textContent = monster.vit;

    document.getElementById('modal-xp').textContent = monster.experience;
    document.getElementById('modal-xp-max').textContent = monster.experienceToNextLevel;
    document.getElementById('modal-skill-points').textContent = monster.skillPoints;
    const xpPercent = (monster.experience / monster.experienceToNextLevel) * 100;
    document.getElementById('modal-xp-bar').style.width = xpPercent + '%';

    displaySkills(monster.skills, monster.skillPoints);
    monsterModal.classList.remove('hidden');
}

function closeModal() {
    monsterModal.classList.add('hidden');
    currentMonsterId = null;
}

monsterModal.addEventListener('click', (e) => {
    if (e.target === monsterModal) closeModal();
});

function displaySkills(skills, skillPoints) {
    const container = document.getElementById('skills-container');
    if (!skills || skills.length === 0) {
        container.innerHTML = '<p>Aucune competence</p>';
        return;
    }

    container.innerHTML = skills.map(skill => `
        <div class="skill-card">
            <div class="skill-info">
                <div class="skill-name">Competence ${skill.num}</div>
                <div class="skill-details">
                    Degats: ${skill.dmg} | Ratio: ${skill.ratio.percent}% ${skill.ratio.stat.toUpperCase()} | CD: ${skill.cooldown}
                </div>
            </div>
            <span class="skill-level">Niv. ${skill.level}/${skill.lvlMax}</span>
            <button class="btn btn-small btn-success" onclick="upgradeSkill(${skill.num})"
                ${skillPoints <= 0 || skill.level >= skill.lvlMax ? 'disabled' : ''}>+</button>
        </div>
    `).join('');
}

async function addMonsterXP(amount) {
    if (!currentMonsterId) return;
    try {
        const response = await fetch(`${API_BASE.monsters}/${currentMonsterId}/experience?amount=${amount}`, {
            method: 'POST',
            headers: { 'Authorization': authToken }
        });
        if (response.ok) {
            await loadMonsters();
            openMonsterModal(currentMonsterId);
        }
    } catch (error) {
        console.error('Erreur:', error);
    }
}

async function upgradeSkill(skillNum) {
    if (!currentMonsterId) return;
    try {
        const response = await fetch(`${API_BASE.monsters}/${currentMonsterId}/skills/${skillNum}/upgrade`, {
            method: 'POST',
            headers: { 'Authorization': authToken }
        });
        if (response.ok) {
            await loadMonsters();
            openMonsterModal(currentMonsterId);
        }
    } catch (error) {
        console.error('Erreur:', error);
    }
}

async function deleteMonster() {
    if (!currentMonsterId || !confirm('Supprimer ce monstre?')) return;
    try {
        const response = await fetch(`${API_BASE.monsters}/${currentMonsterId}`, {
            method: 'DELETE',
            headers: { 'Authorization': authToken }
        });
        if (response.ok) {
            closeModal();
            await loadPlayerData();
            await loadMonsters();
        }
    } catch (error) {
        console.error('Erreur:', error);
    }
}

// ==================== ARENA / COMBAT ====================

function populateFighterSelects() {
    const select1 = document.getElementById('fighter1-select');
    const select2 = document.getElementById('fighter2-select');

    const options = monstersData.map(m => `
        <option value="${m.id}">${getElementEmoji(m.element)} #${m.baseId} - Niv.${m.level} (HP:${m.hp})</option>
    `).join('');

    select1.innerHTML = '<option value="">-- Selectionnez --</option>' + options;
    select2.innerHTML = '<option value="">-- Selectionnez --</option>' + options;

    document.getElementById('fighter1-preview').innerHTML = '';
    document.getElementById('fighter2-preview').innerHTML = '';
}

function updateFighterPreview(fighterNum, monsterId) {
    const preview = document.getElementById(`fighter${fighterNum}-preview`);

    if (!monsterId) {
        preview.innerHTML = '';
        return;
    }

    const monster = monstersData.find(m => m.id === monsterId);
    if (!monster) return;

    preview.innerHTML = `
        <div class="element ${monster.element}" style="font-size: 2em">${getElementEmoji(monster.element)}</div>
        <p><strong>HP:</strong> ${monster.hp}</p>
        <p><strong>ATK:</strong> ${monster.atk}</p>
        <p><strong>DEF:</strong> ${monster.def}</p>
        <p><strong>VIT:</strong> ${monster.vit}</p>
    `;
}

async function startCombat() {
    const fighter1Id = document.getElementById('fighter1-select').value;
    const fighter2Id = document.getElementById('fighter2-select').value;

    if (!fighter1Id || !fighter2Id) {
        alert('Selectionnez deux monstres!');
        return;
    }

    if (fighter1Id === fighter2Id) {
        alert('Selectionnez deux monstres differents!');
        return;
    }

    const monster1 = monstersData.find(m => m.id === fighter1Id);
    const monster2 = monstersData.find(m => m.id === fighter2Id);

    if (!monster1 || !monster2) {
        alert('Monstre introuvable');
        return;
    }

    const fightBtn = document.getElementById('fight-btn');
    fightBtn.disabled = true;
    fightBtn.textContent = 'Combat en cours...';

    try {
        const response = await fetch(`${API_BASE.combat}/fight`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                monster1: {
                    playerName: monster1.ownerUsername,
                    element: monster1.element,
                    hp: monster1.hp,
                    atk: monster1.atk,
                    def: monster1.def,
                    vit: monster1.vit,
                    skills: monster1.skills.map(s => ({
                        num: s.num,
                        dmg: s.dmg,
                        ratioStat: s.ratio.stat,
                        ratioPercent: s.ratio.percent,
                        cooldown: s.cooldown
                    }))
                },
                monster2: {
                    playerName: monster2.ownerUsername,
                    element: monster2.element,
                    hp: monster2.hp,
                    atk: monster2.atk,
                    def: monster2.def,
                    vit: monster2.vit,
                    skills: monster2.skills.map(s => ({
                        num: s.num,
                        dmg: s.dmg,
                        ratioStat: s.ratio.stat,
                        ratioPercent: s.ratio.percent,
                        cooldown: s.cooldown
                    }))
                }
            })
        });

        if (response.ok) {
            const result = await response.json();
            lastCombatNumber = result.combatNumber;
            displayCombatResult(result);
            await loadMonsters();
        } else {
            const error = await response.json();
            alert('Erreur: ' + (error.message || 'Combat echoue'));
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur de connexion');
    } finally {
        fightBtn.disabled = false;
        fightBtn.textContent = 'COMBAT!';
    }
}

function displayCombatResult(result) {
    const resultDiv = document.getElementById('combat-result');
    const summaryDiv = document.getElementById('combat-summary');

    const f1Won = result.fighter1.winner;

    summaryDiv.innerHTML = `
        <div class="combat-winner">
            ${getElementEmoji(f1Won ? result.fighter1.element : result.fighter2.element)}
            Vainqueur: ${result.winnerName}!
        </div>
        <p>Combat #${result.combatNumber} - ${result.totalTurns} tours</p>
        <div class="combat-stats">
            <div class="fighter-result ${f1Won ? 'winner' : 'loser'}">
                <div style="font-size: 2em">${getElementEmoji(result.fighter1.element)}</div>
                <p>${result.fighter1.playerName}</p>
                <p>HP: ${result.fighter1.hpStart} → ${result.fighter1.hpEnd}</p>
            </div>
            <div class="fighter-result ${!f1Won ? 'winner' : 'loser'}">
                <div style="font-size: 2em">${getElementEmoji(result.fighter2.element)}</div>
                <p>${result.fighter2.playerName}</p>
                <p>HP: ${result.fighter2.hpStart} → ${result.fighter2.hpEnd}</p>
            </div>
        </div>
    `;

    resultDiv.classList.remove('hidden');
}

// ==================== ARENA PVP ====================

async function loadPlayersForArena() {
    try {
        const response = await fetch(`${API_BASE.player}/public/list`);
        if (response.ok) {
            const players = await response.json();
            const options = players.map(p => `<option value="${p.username}">${p.username} (Niv.${p.level})</option>`).join('');
            document.getElementById('p1-player-select').innerHTML = '<option value="">-- Choisir un joueur --</option>' + options;
            document.getElementById('p2-player-select').innerHTML = '<option value="">-- Choisir un joueur --</option>' + options;
        }
    } catch (error) {
        console.error('Erreur chargement joueurs:', error);
    }
}

async function loadPlayerMonstersArena(playerNum) {
    const username = document.getElementById(`p${playerNum}-player-select`).value;
    const container = document.getElementById(`p${playerNum}-monsters-list`);

    if (!username) {
        container.innerHTML = '';
        arenaPlayersMonsters[playerNum] = [];
        arenaSelectedMonsters[playerNum] = null;
        return;
    }

    container.innerHTML = '<p style="color: #888; text-align: center; padding: 15px;">Chargement...</p>';

    try {
        const response = await fetch(`${API_BASE.monsters}/public/player/${username}`);
        if (response.ok) {
            const monsters = await response.json();
            arenaPlayersMonsters[playerNum] = monsters;

            if (monsters.length === 0) {
                container.innerHTML = '<p style="color: #888; text-align: center; padding: 15px;">Aucun monstre</p>';
                return;
            }

            container.innerHTML = monsters.map((m, i) => `
                <div class="arena-monster-card" id="p${playerNum}-monster-${i}" onclick="selectArenaMonster(${playerNum}, ${i})">
                    <span class="monster-element">${getElementEmoji(m.element)}</span>
                    <strong>Monstre #${m.baseId}</strong> - Niv.${m.level}
                    <div class="monster-stats">HP: ${m.hp} | ATK: ${m.atk} | DEF: ${m.def} | VIT: ${m.vit}</div>
                </div>
            `).join('');
        }
    } catch (error) {
        container.innerHTML = '<p style="color: #e74c3c; text-align: center;">Erreur de chargement</p>';
    }
}

function selectArenaMonster(playerNum, index) {
    document.querySelectorAll(`#p${playerNum}-monsters-list .arena-monster-card`).forEach(card => card.classList.remove('selected'));
    document.getElementById(`p${playerNum}-monster-${index}`).classList.add('selected');
    arenaSelectedMonsters[playerNum] = arenaPlayersMonsters[playerNum][index];
}

async function startArenaCombat() {
    if (!arenaSelectedMonsters[1] || !arenaSelectedMonsters[2]) {
        alert('Selectionnez un monstre pour chaque joueur!');
        return;
    }

    const monster1 = arenaSelectedMonsters[1];
    const monster2 = arenaSelectedMonsters[2];

    if (monster1.ownerUsername === monster2.ownerUsername) {
        alert('Les deux joueurs doivent etre differents!');
        return;
    }

    const btn = document.getElementById('arena-fight-btn');
    btn.disabled = true;
    btn.textContent = 'Combat en cours...';

    try {
        const response = await fetch(`${API_BASE.combat}/fight`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                monster1: formatMonsterForCombat(monster1),
                monster2: formatMonsterForCombat(monster2)
            })
        });

        if (response.ok) {
            const result = await response.json();
            displayArenaCombatResult(result);
        } else {
            const error = await response.json();
            alert('Erreur: ' + (error.message || 'Combat echoue'));
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur de connexion: ' + error.message);
    } finally {
        btn.disabled = false;
        btn.textContent = 'COMBAT!';
    }
}

function formatMonsterForCombat(monster) {
    return {
        playerName: monster.ownerUsername,
        element: monster.element,
        hp: monster.hp,
        atk: monster.atk,
        def: monster.def,
        vit: monster.vit,
        skills: monster.skills.map(s => ({
            num: s.num,
            dmg: s.dmg,
            ratioStat: s.ratio.stat,
            ratioPercent: s.ratio.percent,
            cooldown: s.cooldown
        }))
    };
}

function displayArenaCombatResult(result) {
    const resultDiv = document.getElementById('arena-combat-result');
    const summaryDiv = document.getElementById('arena-combat-summary');
    const logsDiv = document.getElementById('arena-turn-logs');

    const f1Won = result.fighter1.winner;

    summaryDiv.innerHTML = `
        <div class="combat-winner">
            ${getElementEmoji(f1Won ? result.fighter1.element : result.fighter2.element)}
            ${result.winnerName} remporte le combat!
        </div>
        <p style="color: #888; margin-bottom: 15px;">Combat #${result.combatNumber} - ${result.totalTurns} tours</p>
        <div class="combat-stats">
            <div class="fighter-result ${f1Won ? 'winner' : 'loser'}">
                <div style="font-size: 2em">${getElementEmoji(result.fighter1.element)}</div>
                <p><strong>${result.fighter1.playerName}</strong></p>
                <p>HP: ${result.fighter1.hpStart} → ${result.fighter1.hpEnd}</p>
                <p class="${f1Won ? 'arena-winner-badge' : 'arena-loser-badge'}">${f1Won ? '🏆 VAINQUEUR' : '💀 VAINCU'}</p>
            </div>
            <div class="fighter-result ${!f1Won ? 'winner' : 'loser'}">
                <div style="font-size: 2em">${getElementEmoji(result.fighter2.element)}</div>
                <p><strong>${result.fighter2.playerName}</strong></p>
                <p>HP: ${result.fighter2.hpStart} → ${result.fighter2.hpEnd}</p>
                <p class="${!f1Won ? 'arena-winner-badge' : 'arena-loser-badge'}">${!f1Won ? '🏆 VAINQUEUR' : '💀 VAINCU'}</p>
            </div>
        </div>
    `;

    logsDiv.innerHTML = `
        <h4>Deroulement du combat</h4>
        ${result.turnLogs.map(log => `
            <div class="arena-log-entry">
                <span class="turn-num">T${log.turn}</span>${log.description}
            </div>
        `).join('')}
    `;

    resultDiv.classList.remove('hidden');
}

function closeArenaResult() {
    document.getElementById('arena-combat-result').classList.add('hidden');
}

// ==================== HISTORIQUE ====================

async function loadCombatHistory() {
    try {
        const response = await fetch(`${API_BASE.combat}/history`);

        if (response.ok) {
            const history = await response.json();
            displayCombatHistory(history);
        }
    } catch (error) {
        console.error('Erreur:', error);
    }
}

function displayCombatHistory(history) {
    const container = document.getElementById('combat-history-list');

    if (history.length === 0) {
        container.innerHTML = '<p>Aucun combat enregistre.</p>';
        return;
    }

    container.innerHTML = history.map(c => `
        <div class="history-item" onclick="loadReplay(${c.combatNumber})">
            <span class="combat-id">#${c.combatNumber}</span>
            <span class="fighters">${c.player1} vs ${c.player2}</span>
            <span class="result ${c.winnerName === currentUsername ? 'won' : 'lost'}">
                ${c.winnerName === currentUsername ? 'Victoire' : c.winnerName}
            </span>
            <span>${c.totalTurns} tours</span>
        </div>
    `).join('');
}

// ==================== REPLAY ====================

async function viewReplay() {
    if (lastCombatNumber) {
        await loadReplay(lastCombatNumber);
    }
}

async function loadReplay(combatNumber) {
    try {
        const response = await fetch(`${API_BASE.combat}/replay/${combatNumber}`);

        if (response.ok) {
            replayData = await response.json();
            replayCurrentTurn = 0;
            openReplayModal();
        }
    } catch (error) {
        console.error('Erreur:', error);
    }
}

function openReplayModal() {
    document.getElementById('replay-combat-number').textContent = replayData.combatNumber;

    document.getElementById('replay-fighter1').innerHTML = `
        <div class="element ${replayData.fighter1.element}">${getElementEmoji(replayData.fighter1.element)}</div>
        <p><strong>${replayData.fighter1.playerName}</strong></p>
        <div class="hp-display">HP: <span id="replay-hp1">${replayData.fighter1.hpStart}</span>/${replayData.fighter1.hpStart}</div>
    `;

    document.getElementById('replay-fighter2').innerHTML = `
        <div class="element ${replayData.fighter2.element}">${getElementEmoji(replayData.fighter2.element)}</div>
        <p><strong>${replayData.fighter2.playerName}</strong></p>
        <div class="hp-display">HP: <span id="replay-hp2">${replayData.fighter2.hpStart}</span>/${replayData.fighter2.hpStart}</div>
    `;

    updateReplayDisplay();
    replayModal.classList.remove('hidden');
}

function closeReplayModal() {
    replayModal.classList.add('hidden');
    if (replayAutoInterval) {
        clearInterval(replayAutoInterval);
        replayAutoInterval = null;
    }
}

replayModal.addEventListener('click', (e) => {
    if (e.target === replayModal) closeReplayModal();
});

function updateReplayDisplay() {
    const totalTurns = replayData.turnLogs.length;
    document.getElementById('replay-turn-indicator').textContent = `Tour ${replayCurrentTurn} / ${totalTurns}`;

    // Calculer les HP a ce tour
    let hp1 = replayData.fighter1.hpStart;
    let hp2 = replayData.fighter2.hpStart;

    for (let i = 0; i < replayCurrentTurn; i++) {
        const log = replayData.turnLogs[i];
        if (log.defender === replayData.fighter1.playerName) {
            hp1 = log.defenderHpAfter;
        } else {
            hp2 = log.defenderHpAfter;
        }
    }

    document.getElementById('replay-hp1').textContent = Math.max(0, hp1);
    document.getElementById('replay-hp2').textContent = Math.max(0, hp2);

    // Afficher les logs
    const logContainer = document.getElementById('replay-log');
    logContainer.innerHTML = replayData.turnLogs.slice(0, replayCurrentTurn).map((log, i) => `
        <div class="log-entry ${i === replayCurrentTurn - 1 ? 'current' : ''}">
            <span class="turn-num">T${log.turn}</span>
            ${log.description}
        </div>
    `).join('');

    logContainer.scrollTop = logContainer.scrollHeight;
}

function replayNext() {
    if (replayCurrentTurn < replayData.turnLogs.length) {
        replayCurrentTurn++;
        updateReplayDisplay();
    }
}

function replayPrev() {
    if (replayCurrentTurn > 0) {
        replayCurrentTurn--;
        updateReplayDisplay();
    }
}

function replayAuto() {
    if (replayAutoInterval) {
        clearInterval(replayAutoInterval);
        replayAutoInterval = null;
        return;
    }

    replayAutoInterval = setInterval(() => {
        if (replayCurrentTurn < replayData.turnLogs.length) {
            replayCurrentTurn++;
            updateReplayDisplay();
        } else {
            clearInterval(replayAutoInterval);
            replayAutoInterval = null;
        }
    }, 500);
}

// ==================== UTILITAIRES ====================

function getElementEmoji(element) {
    switch (element) {
        case 'fire': return '🔥';
        case 'water': return '💧';
        case 'wind': return '🌪️';
        default: return '⭐';
    }
}

function showMessage(text, type) {
    loginMessage.textContent = text;
    loginMessage.className = 'message ' + type;
}
