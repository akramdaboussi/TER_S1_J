# TER_S1_J
Projet TER Groupe J
Bensallah Younes
Daboussi Akram

## Fonctionnalités Clés et Architecture
Ce projet implémente un générateur de labyrinthes de type Pac-Man en Java. Ce générateur est basé sur l'algorithme de Kruskal modifié pour garantir une symétrie horizontale parfaite et éviter les culs-de sac.

L'application est architecturée comme suit :
* **Back-end et Interface HTTP:** Un service basé sur **Spark Java** qui fournit une interface HTTP afin d'obtenir et évaluer les labyrinthes générés.

* **Base de Données:** Utilisation de **MongoDB Atlas** pour stocker les labyrinthes générés et leur notation.

* **Client:** Une application **LocalClient** (basée sur Swing) permet de récupérer le labyrinthe généré via l'API, d'afficher sa visualisation graphique et de lui attribuer une note entre 0 et 5.

* **CI/CD:** Un workflow **GitHub Actions** gère les tests unitaires et la construction (CI). L'application est conteneurisée via **Docker** pour le déploiement (CD).

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
mvn exec:java -Dexec.mainClass="LocalClient"

## Lancement du jeu en local (sera modifié par la suite)
mvn exec:java -Dexec.mainClass="Game.GameLauncher"