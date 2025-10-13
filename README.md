# TER_S1_J
Projet TER Groupe J
Toudert Tarik
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

# Pour nettoyer, compiler et juste créer le fichier .jar
mvn clean package

# Compiler et executer le projet 
mvn compile exec:java



