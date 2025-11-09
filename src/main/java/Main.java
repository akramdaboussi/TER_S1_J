import java.util.Random;
import Generator.*;
import Model.*;
import static spark.Spark.*;

/*
 * Classe principale de l'application.
 * Configure et lance le serveur HTTP Spark Java.
*/
public class Main {

    // Configuration par défaut
    private static final int WIDTH = 28;
    private static final int HEIGHT = 31;
    private static final String API_ENDPOINT = "/api/labyrinthe"; 


    public static void main(String[] args) {
        // 1. Configuration du Port pour le déploiement Cloud (Render)
        // Lit la variable d'environnement PORT (fournie par Render) ou utilise 4567 par défaut
        String portStr = System.getenv("PORT");
        int port = portStr != null ? Integer.parseInt(portStr) : 4567;
        port(port);
        
        System.out.println("Démarrage du Maze Generator sur le port: " + port);
        // Définition de l'API: GET /api/labyrinthe
        get( API_ENDPOINT, (request, response) -> {
            // Lecture et parsing des paramètres de la requête
            // Largeur (width)
            int width = getQueryInt(request.queryParams("width"), WIDTH);
            // Hauteur (height)
            int height = getQueryInt(request.queryParams("height"), HEIGHT);
            // Seed (pour reproductibilité)
            long seed = System.currentTimeMillis();

            // Validation de base pour la symétrie
            if (width % 2 != 0) {
                 response.status(400); // Bad Request
                 return "{\"error\": \"La largeur doit être paire pour une symétrie parfaite.\"}";
            }
            System.out.println("Génération du labyrinthe: HEIGHT=" + height + ", WIDTH=" + width);

            // Logique de Génération 
            System.out.println("Génération du labyrinthe...");
            Random random = new Random(seed);
            // Crée un labyrinthe vide (que des murs)
            Maze maze = new Maze(width, height);
            // Applique la structure de base (murs extérieurs, maison des fantômes)
            maze.applyTemplate();
            // Instancie un générateur et lance l'algorithme sur le labyrinthe
            MazeGenerator generator = new MazeGenerator();
            generator.generate(maze, random);

            // Configuration et Retour de la Réponse JSON
            response.type("application/json");
            // Utilise la méthode toJsonString() pour créer le JSON
            return maze.toJsonString(); 
        });

        // Route d'accueil simple
        get("/", (request, response) -> "Bienvenue à tous ! Utilisez /api/labyrinthe pour générer un labyrinthe.");
    }

    // Méthodes utilitaires pour parser les paramètres de requête avec valeurs par défaut
    private static int getQueryInt(String param, int defaultValue) {
        try {
            return param != null ? Integer.parseInt(param) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
