# TER_S1_J
Projet TER Groupe J
Bensallah Younes
Daboussi Akram

### Installation (Pour Ubuntu / WSL)

# Mettre à jour la liste des paquets
sudo apt update

# Installer Java 21
sudo apt install -y openjdk-21-jdk

# Configurer les variables d'environnement (pour la session actuelle)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Vérifier que tout est bien configuré
mvn -v

# Pour une compilation rapide (sans tests)
mvn -q -DskipTests=true compile

# Pour nettoyer, compiler et juste créer le fichier .jar
mvn clean package

# Executer le JAR
Lance le service sur le port 4567 (par défaut) ou le port spécifié par la variable PORT.
java -jar target/pac-man-generator-1.0-SNAPSHOT-jar-with-dependencies.jar

# Pour lancer sur un port spécifique (ex: 8000) en local :
PORT=8000 java -jar target/pac-man-generator-1.0-SNAPSHOT-jar-with-dependencies.jar

# Tester l'API après le lancement du serveur
Ouvrir dans le navigateur : http://localhost:<votre_port>/api/labyrinthe

Exemple avec des paramètres :
http://localhost:8080/api/labyrinthe?width=30&height=30&imperfection=0.1