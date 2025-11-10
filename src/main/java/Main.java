import java.util.Random;

import com.google.gson.Gson;

import Database.MongoDBService;
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
    private static final String API_RATING_ENDPOINT = "/api/labyrinthe/note";

    // Instance MongoDB
    private static MongoDBService dbService;

    private record RatingRequest(String ident, int note) {}

    public static void main(String[] args) {
        // Configuration du Port pour le déploiement Cloud (Render)
        // Lit la variable d'environnement PORT (fournie par Render) ou utilise 4567 par défaut
        String portStr = System.getenv("PORT");
        int port = portStr != null ? Integer.parseInt(portStr) : 4567;
        port(port);

        // Initialisation du service MongoDB
        dbService = new MongoDBService();
        
        System.out.println("Démarrage du Maze Generator sur le port: " + port);

        // Définition de l'API: GET /api/labyrinthe (Génération et stockage)
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

            MazeData dataForJson = maze.getMazeData(); // Contient maintenant l'ident
            
            // Stocke le labyrinthe dans MongoDB
            if (dbService.est_connecté()) {
                dbService.saveMaze(dataForJson);
            } 

            // Configuration et Retour de la Réponse JSON
            response.type("application/json");
            // Utilise la méthode toJsonString() pour créer le JSON
            return maze.toJsonString(); 
        });

        // Endpoint POST pour la notation des labyrinthes
        post(API_RATING_ENDPOINT, (request, response) -> {
            response.type("application/json");
            if (!dbService.est_connecté()) {
                response.status(503); 
                return "{\"error\": \"Le service de base de données n'est pas disponible.\"}";
            }

            try {
                // Parsing du JSON entrant (ident et note)
                Gson gson = new Gson();
                RatingRequest rating = gson.fromJson(request.body(), RatingRequest.class); 
                
                if (rating.ident() == null || rating.note() < 0 || rating.note() > 5) {
                    response.status(400);
                    return "{\"error\": \"Identifiant ou note (0-5) invalide.\"}";
                }

                long updatedCount = dbService.updateRating(rating.ident(), rating.note());
                
                if (updatedCount > 0) {
                    response.status(200);
                    return "{\"message\": \"Notation enregistrée avec succès.\", \"ident\": \"" + rating.ident() + "\", \"note\": " + rating.note() + "}";
                } else {
                    response.status(404);
                    return "{\"error\": \"Labyrinthe non trouvé avec l'identifiant: " + rating.ident() + "\"}";
                }

            } catch (Exception e) {
                response.status(500);
                return "{\"error\": \"Erreur interne du serveur lors de la notation.\"}";
            }
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
