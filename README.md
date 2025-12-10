# TER_S1_J
Projet TER Groupe J
Bensallah Younes
Daboussi Akram

## Fonctionnalités Clés et Architecture

### Génération de Labyrinthes
* **Algorithme Hybride :** Utilisation de l'algorithme de Kruskal modifié pour garantir une symétrie horizontale parfaite.
* **Post-traitement :** Algorithme de suppression des culs-de-sac pour créer des boucles et fluidifier le gameplay.
* **Structure Pac-Man :** Application d'un gabarit fixe (Ghost House, Tunnels de téléportation, bordures) avant la génération.

### Moteur de Jeu & IA
Le projet intègre une logique de jeu se rapprochant de l'original :
* **Comportement des Fantômes :** Implémentation des 4 personnalités distinctes :
    * *Blinky (Rouge) :* Chasse directe.
    * *Pinky (Rose) :* Embuscade (vise 4 cases devant).
    * *Inky (Cyan) :* Comportement complexe basé sur la position de Blinky.
    * *Clyde (Orange) :* Peureux (chasse puis fuit si trop proche).
* **Algorithme de Déplacement :** Utilisation d'une approche Gloutonne. À chaque intersection, le fantôme choisit la direction qui minimise la distance euclidienne directe vers sa case cible, sans recalculer le chemin complet.
* **États de Jeu :** Gestion des modes *Chase* et *Frightened* (quand une Power Pellet est mangée).
* **Collisions & Score :** Gestion précise des hitboxes et du scoring.

### Client & Modes de Simulation
L'application cliente (`LocalClient`) propose deux phases d'utilisation :
* **Mode Jeu Direct :** Jouez à Pac-Man contre l'IA dans le labyrinthe généré.
* **Mode Enregistrement + Simulation :** Le joueur parcourt le labyrinthe seul pour enregistrer une trajectoire optimale. Une fois enregistrée, l'ordinateur rejoue la trajectoire enregistrée en activant les fantômes.

### Architecture Cloud & Data
* **Back-end et Interface HTTP:** Un service basé sur **Spark Java** qui fournit une interface HTTP afin d'obtenir et évaluer les labyrinthes générés.

* **Base de Données:** Utilisation de **MongoDB Atlas** pour stocker les labyrinthes générés et leur notation.

* **Client:** Une application **LocalClient** (basée sur Swing) permet de récupérer le labyrinthe généré via l'API, d'afficher sa visualisation graphique et de lui attribuer une note entre 0 et 5 et puis lancer le jeu.

* **CI/CD:** Un workflow **GitHub Actions** gère les tests unitaires et la construction (CI). L'application est conteneurisée via **Docker** pour le déploiement (CD).


## Architecture Technique

Le projet suit une architecture modulaire (MVC) :
* `Generator/` : Logique de génération du labyrinthe.
* `Game/` : Logique de jeu (IA de recherche, États du jeu).
* `Model/` : Structures de données (Maze, CellState, MazeData).
* `View/` : Interface graphique Swing (Rendu rétro, HUD).
* `Database/` : Couche d'accès aux données (MongoDB).
* `Api/` : Client HTTP pour la communication avec le serveur.

### Dépendances
Les principales dépendances du projet, gérées par Maven, sont:
* **Spark Core** (Serveur HTTP)
* **Gson** (Gestion du format JSON)
* **MongoDB Driver Sync** (Connexion à la base de données Atlas)
* **JUnit Jupiter** (Tests)


## Commandes d'installation (Pour Ubuntu / WSL)

### Mettre à jour la liste des paquets
sudo apt update

### Installer Java 21
sudo apt install -y openjdk-21-jdk

### Configurer les variables d'environnement (pour la session actuelle)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

### Vérifier que tout est bien configuré
mvn -v

## Compilation et Tests

### Pour nettoyer, compiler et créer le fichier .jar (sans lancer les tests)
mvn clean package -DskipTests=true

### Execution de tous les tests
mvn clean test

## Execution du Script de Test de l'API déployée
### Rendre le script exécutable 
chmod +x tests/api_tests.sh

### Lancement du script de tests
./tests/api_tests.sh


## Lancement du client de visualisation 

### Lancement avec dimensions standards (Par défaut 28*31)
mvn exec:java -Dexec.mainClass="LocalClient"

### Lancement avec dimensions personnalisées (largeur paire et hauteur impaire)
mvn exec:java -Dexec.mainClass="LocalClient" -Dw=24 -Dh=27 (par exemple)
