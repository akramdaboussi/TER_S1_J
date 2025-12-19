# TER_S1_J
Projet TER Groupe J
Bensallah Younes
Daboussi Akram

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

## Test de performance -> Lancer les Benchmarks (sans interfaces)

### Lancer le benchmark IA vs IA (100 itérations par config)
mvn exec:java -Dexec.mainClass="IABenchmark"

### Lancer le benchmark Replay (nécessite un fichier trajectoire_joueur.txt)
mvn exec:java -Dexec.mainClass="ReplayBenchmark"

## Générer les graphiques (Python)
Une fois les fichiers csv générés par les benchmarks :

### Installer les dépendances 
pip install pandas matplotlib seaborn

### Générer les courbes (sauvegardés en .png)
python metrics/graphique.py



## Fonctionnalités Clés et Architecture

### Génération de Labyrinthes
* **Algorithme Hybride :** Utilisation de l'algorithme de Kruskal modifié pour garantir une symétrie horizontale parfaite.
* **Post-traitement :** Algorithme de suppression des culs-de-sac pour créer des boucles et fluidifier le gameplay.
* **Structure Pac-Man :** Application d'un gabarit fixe (Ghost House, Tunnels de téléportation, bordures) avant la génération.

### Intelligence Artificielle (Pac-Man & Fantômes)
Le projet se concentre sur l'implémentation d'une IA robuste capable de naviguer dans un environnement hostile :

* **IA Pac-Man :**
    * **Minimax avec Élagage Alpha-Beta :** Algorithme unique de prise de décision. L'IA anticipe les mouvements adverses sur une profondeur (Horizon) de 4 coups pour maximiser son score et sa survie.
    * **Fonction d'Évaluation Avancée :** Prend en compte la distance aux pellets, la position des fantômes, et les états spéciaux (Power Pellets / Frightened).
    * **Sécurités :** Mécanismes anti-boucle (pénalité de répétition de position) et anti-tremblement pour fluidifier le mouvement.

* **IA Fantômes :**
    * **Configuration Hybride :** Chaque fantôme peut être configuré individuellement en mode **A* (A-Star)** (trajectoire optimale) ou **Glouton** (choix local) via l'interface.
    * **Personnalités Uniques :** Implémentation fidèle des comportements originaux (Blinky, Pinky, Inky, Clyde) avec gestion des états *Chase* et *Frightened*.

### Benchmarking & Analyse de Données
Le projet inclut des outils pour mesurer scientifiquement la performance de l'algorithme Minimax face à différentes difficultés :

* **Simulations Automatisées :**
    * **IA vs IA :** Exécution rapide de centaines de parties sans rendu graphique pour tester l'IA Pac-Man contre les 16 configurations possibles de fantômes.
    * **Replay vs IA :** Rejoue une trajectoire humaine enregistrée face à différentes configurations de fantômes pour évaluer la robustesse d'un parcours.
* **Visualisation des Données :**
    * Export automatique des résultats en **csv** et des trajectoires en fichiers texte.
    * Script **Python (Matplotlib/Seaborn)** inclus pour générer les courbes de taux de victoire et de score moyen.

### Modes de Jeu
L'application cliente (`LocalClient`) propose trois expériences :
1.  **Jeu Direct :** Joueur humain contre Fantômes (configurables).
2.  **IA vs IA (Visuel) :** Observez l'algorithme Minimax jouer en temps réel.
3.  **Mode Record & Replay :** Enregistrez une partie sans fantômes puis lancez une simulation pour voir si cette trajectoire survit aux fantômes actifs.


## Architecture Technique
* **Back-end et Interface HTTP:** Un service basé sur **Spark Java** qui fournit une interface HTTP afin d'obtenir et évaluer les labyrinthes générés.

* **Base de Données:** Utilisation de **MongoDB Atlas** pour stocker les labyrinthes générés et leur notation.

* **Client:** Une application **LocalClient** (basée sur Swing) permet de récupérer le labyrinthe généré via l'API, d'afficher sa visualisation graphique et de lui attribuer une note entre 0 et 5 et puis lancer le jeu.

* **CI/CD:** Un workflow **GitHub Actions** gère les tests unitaires et la construction (CI). L'application est conteneurisée via **Docker** pour le déploiement (CD).


### Structure du projet
* `Generator/` : Logique de génération du labyrinthe.
* `Game/` : Moteur physique, Logique de jeu (IA (Glouton/A*/Minimax), États du jeu).
* `Model/` : Structures de données (Maze, CellState, MazeData).
* `View/` : Interface graphique Swing (Rendu rétro, HUD, Sprites, Animations).
* `Database/` : Couche d'accès aux données (MongoDB).
* `Api/` : Client HTTP pour la communication avec le serveur.
* `metrics/` : Scripts Python et fichiers de résultats (csv, JSON).

### Dépendances
Les principales dépendances du projet, gérées par Maven, sont:
* **Spark Core (2.9.4)** (Serveur HTTP)
* **Gson (2.10.1)** (Gestion du format JSON)
* **MongoDB Driver Sync (5.2.1)** (Connexion à la base de données Atlas)
* **JUnit Jupiter (5.10.1)** (Tests)


