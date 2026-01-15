# Gatcha Game - Projet Web API

Jeu de type gacha composé de plusieurs microservices API (Spring Boot) et d'un frontend web. L'objectif : authentifier des joueurs, invoquer des monstres, gérer leur progression et (en bonus) les faire combattre.

## Sommaire
- [Aperçu](#aperçu)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Démarrage rapide](#démarrage-rapide)
- [Comptes de test](#comptes-de-test)
- [Documentation API](#documentation-api)
- [Structure des données](#structure-des-données)
- [Arrêt et nettoyage](#arrêt-et-nettoyage)
- [Technologies](#technologies)
- [Structure du projet](#structure-du-projet)

## Aperçu
- 5 APIs Spring Boot + 1 frontend
- Authentification via token (header `Authorization`)
- Données MongoDB initialisées automatiquement

## Architecture

| Service | Port | Description |
|---------|------|-------------|
| **auth-service** | 8081 | Authentification, tokens |
| **player-service** | 8082 | Gestion des joueurs |
| **monster-service** | 8083 | Gestion des monstres |
| **invocation-service** | 8084 | Invocation (gacha) |
| **combat-service** | 8085 | Combat (BONUS) |
| **frontend** | 8080 | Interface web |

## Prérequis
- Docker + Docker Compose
- Ports 8080 à 8085 disponibles

## Démarrage rapide

1) Cloner le projet
```bash
git clone <url-du-repo>
cd Gatcha_projet
```

2) Lancer tous les services
```bash
docker-compose up --build
```

Ce lancement :
- crée l'image MongoDB et initialise les bases
- compile et démarre les 5 APIs Spring Boot
- lance le frontend Nginx

3) Ouvrir l'application

http://localhost:8080

## Comptes de test
- **bob** / bob
- **alice** / alice

## Fonctionnalités du Frontend

Le frontend (http://localhost:8080) propose :

| Fonctionnalité | Description |
|----------------|-------------|
| Connexion / Inscription | Authentification avec token |
| **Bouton Déconnexion** | Révoque le token via `POST /api/auth/logout` |
| Profil joueur | Niveau, XP, nombre de monstres |
| Invocation | Bouton pour invoquer un monstre aléatoire |
| Gestion des monstres | Voir stats, ajouter XP, améliorer compétences |
| Arène PvP | Combat entre 2 joueurs différents |
| Historique combats | Liste et rediffusion des combats |

## Documentation API
- Collection Postman : `Gatcha_API.postman_collection.json` (Le fichier JSON Postman est disponible a la racine du projet)
- PDF : `Gatcha APIs.pdf` (sujet du projet)


### Règles communes
- Authentification requise pour toutes les APIs (sauf `auth` et endpoints publics)
- Header : `Authorization: <token>`
- Token valide 1 heure, renouvelé à chaque requête valide

### Base URLs

| Service | Base URL |
|---------|----------|
| Auth | `http://localhost:8081/api/auth` |
| Player | `http://localhost:8082/api/player` |
| Monster | `http://localhost:8083/api/monsters` |
| Invocation | `http://localhost:8084/api/invocation` |
| Combat | `http://localhost:8085/api/combat` |

### Endpoints (détail)

#### Authentification (8081)
Base URL : `http://localhost:8081/api/auth`

- `POST /login` - Connexion
- `POST /register` - Inscription
- `GET /validate` - Validation d'un token
- `POST /logout` - Déconnexion

Exemple - login :
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "bob",
  "password": "bob"
}
```

Réponse (200) :
```json
{
  "token": "U2FsdGVkX1...",
  "username": "bob",
  "expiresAt": "2024-01-15T15:30:00"
}
```

#### Joueur (8082)
Base URL : `http://localhost:8082/api/player`

- `GET /profile` - Profil complet
- `GET /monsters` - Liste des IDs de monstres
- `GET /level` - Niveau du joueur
- `POST /experience?amount=100` - Ajouter de l'XP
- `POST /levelup` - Monter de niveau (force)
- `POST /monsters/{id}` - Ajouter un monstre
- `DELETE /monsters/{id}` - Supprimer un monstre
- `GET /public/list` - Liste des joueurs (public)

Réponse - profil :
```json
{
  "username": "bob",
  "level": 1,
  "experience": 0,
  "experienceToNextLevel": 50,
  "monsterIds": [],
  "maxMonsters": 11
}
```

#### Monstres (8083)
Base URL : `http://localhost:8083/api/monsters`

- `GET /` - Tous les monstres du joueur
- `GET /{id}` - Un monstre spécifique
- `POST /{id}/experience?amount=100` - Ajouter de l'XP
- `POST /{id}/skills/{num}/upgrade` - Améliorer une compétence
- `DELETE /{id}` - Supprimer un monstre
- `GET /public/player/{username}` - Monstres d'un joueur (public)

Réponse - liste :
```json
[
  {
    "id": "abc123",
    "ownerUsername": "bob",
    "baseId": 1,
    "element": "fire",
    "level": 1,
    "experience": 0,
    "experienceToNextLevel": 100,
    "hp": 1200,
    "atk": 450,
    "def": 300,
    "vit": 85,
    "skillPoints": 0,
    "skills": [
      {
        "num": 1,
        "dmg": 100,
        "ratio": { "stat": "atk", "percent": 50.0 },
        "cooldown": 0,
        "level": 1,
        "lvlMax": 5
      }
    ]
  }
]
```

#### Invocation (8084)
Base URL : `http://localhost:8084/api/invocation`

- `POST /invoke` - Invoquer un monstre
- `GET /history` - Historique des invocations
- `POST /retry` - Réexécuter les invocations échouées

Réponse - invocation :
```json
{
  "invocationId": "inv123",
  "username": "bob",
  "monster": {
    "id": "abc123",
    "element": "fire",
    "hp": 1200,
    "atk": 450,
    "def": 300,
    "vit": 85
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

#### Combat (8085) - BONUS
Base URL : `http://localhost:8085/api/combat`

- `POST /fight` - Lancer un combat
- `GET /history` - Historique des combats
- `GET /replay/{combatNumber}` - Rediffusion d'un combat

Exemple - combat :
```http
POST /api/combat/fight
Content-Type: application/json

{
  "monster1": {
    "playerName": "bob",
    "element": "fire",
    "hp": 1200,
    "atk": 450,
    "def": 300,
    "vit": 85,
    "skills": [
      { "num": 1, "dmg": 100, "ratioStat": "atk", "ratioPercent": 50.0, "cooldown": 0 },
      { "num": 2, "dmg": 150, "ratioStat": "atk", "ratioPercent": 75.0, "cooldown": 2 },
      { "num": 3, "dmg": 200, "ratioStat": "atk", "ratioPercent": 100.0, "cooldown": 4 }
    ]
  },
  "monster2": {
    "playerName": "alice",
    "element": "water",
    "hp": 1400,
    "atk": 350,
    "def": 400,
    "vit": 75,
    "skills": [
      { "num": 1, "dmg": 80, "ratioStat": "def", "ratioPercent": 40.0, "cooldown": 0 },
      { "num": 2, "dmg": 120, "ratioStat": "def", "ratioPercent": 60.0, "cooldown": 2 },
      { "num": 3, "dmg": 180, "ratioStat": "hp", "ratioPercent": 10.0, "cooldown": 3 }
    ]
  }
}
```

Réponse (200) :
```json
{
  "combatNumber": 1,
  "timestamp": "2024-01-15T14:35:00",
  "winnerName": "bob",
  "totalTurns": 12,
  "fighter1": {
    "playerName": "bob",
    "element": "fire",
    "hpStart": 1200,
    "hpEnd": 450,
    "winner": true
  },
  "fighter2": {
    "playerName": "alice",
    "element": "water",
    "hpStart": 1400,
    "hpEnd": 0,
    "winner": false
  },
  "turnLogs": [
    {
      "turn": 1,
      "attacker": "bob",
      "defender": "alice",
      "skillUsed": 3,
      "damage": 250,
      "defenderHpBefore": 1400,
      "defenderHpAfter": 1150,
      "description": "bob utilise compétence 3 et inflige 250 dégâts"
    }
  ]
}
```

## Structure des données

### Monstre de base (invocation)
```json
{
  "_id": 1,
  "element": "fire",
  "hp": 1200,
  "atk": 450,
  "def": 300,
  "vit": 85,
  "skills": [
    {
      "num": 1,
      "dmg": 100,
      "ratio": { "stat": "atk", "percent": 50.0 },
      "cooldown": 0,
      "lvlMax": 5
    }
  ],
  "lootRate": 0.3
}
```

### Taux d'invocation

| Monstre | Élément | Taux |
|---------|---------|------|
| Monstre 1 | Feu | 30% |
| Monstre 2 | Vent | 30% |
| Monstre 3 | Eau | 30% |
| Monstre 4 | Eau (rare) | 10% |

## Arrêt et nettoyage

Arrêter les services :
```bash
docker-compose down
```

Supprimer aussi les volumes (bases de données) :
```bash
docker-compose down -v
```

## Technologies
- **Backend** : Spring Boot 3.2, Java 17
- **Base de données** : MongoDB 7.0
- **Frontend** : HTML, CSS, JavaScript (Vanilla)
- **Conteneurisation** : Docker, Docker Compose

## Structure du projet

```
Gatcha_projet/
├── docker-compose.yml
├── README.md
├── auth-service/           # API Authentification (8081)
├── player-service/         # API Joueur (8082)
├── monster-service/        # API Monstres (8083)
├── invocation-service/     # API Invocations (8084)
├── combat-service/         # API Combat BONUS (8085)
├── frontend/               # Interface web (8080)
├── mongo-init/             # Scripts d'initialisation MongoDB
├── monstres-with-loot-rate.json
└── Gatcha_API.postman_collection.json
```
