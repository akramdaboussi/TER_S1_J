# Utilise une image de base qui contient Maven et Java 21 (votre version cible).
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Crée le répertoire de travail dans le conteneur
WORKDIR /app

# Copie tous les fichiers du dépôt local dans le répertoire de travail du conteneur
COPY . /app

# Exécute la compilation et le packaging du projet, en sautant les tests pour accélérer le processus
RUN mvn clean package -DskipTests

# ----------------------------------------------------------------------

# Utilise une image JRE 21 (Java Runtime Environment) légère basée sur Alpine Linux
FROM eclipse-temurin:21-jre-alpine

# Crée le répertoire d'exécution dans le conteneur
WORKDIR /app

# Copie le JAR final compilé depuis la phase de 'build' dans l'image finale
COPY --from=build /app/target/pac-man-generator-1.0-SNAPSHOT-jar-with-dependencies.jar /app/app.jar

# Définit la commande de démarrage du conteneur
# Elle lance le JAR, ce qui démarre le serveur Spark Java
ENTRYPOINT ["java", "-jar", "app.jar"]