package Integration;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.*;
import java.util.Random;
import static spark.Spark.*;

import Generator.MazeGenerator;
import Model.Maze;

/**
 * Test d'intégration de l'API: Démarre le serveur Spark en mémoire pour tester
 * les endpoints HTTP, assurant la porte de qualité CI/CD
 */
public class ApiIntegrationTest {

    private static final int PORT = 5000;
    private static final int WIDTH = 28;
    private static final int HEIGHT = 31;
    private static final String API_ENDPOINT = "/api/labyrinthe"; 
    private static final String BASE_URL = "http://localhost:" + PORT;


    private static URL createUrl(String urlString) throws IOException, URISyntaxException {
        // Utilise URI pour éviter l'avertissement de dépréciation sur URL(String)
        return new URI(urlString).toURL();
    }

    // Configuration du serveur avant les tests
    @BeforeAll
    public static void setUp() {
        stop(); 
        awaitStop();
        port(PORT);        
        get(API_ENDPOINT, (request, response) -> {
            // Lecture et parsing des paramètres de la requête
            int width = getQueryInt(request.queryParams("width"), WIDTH);
            int height = getQueryInt(request.queryParams("height"), HEIGHT);
            long seed = System.currentTimeMillis();

            // Validation de base pour la symétrie
            if (width % 2 != 0) {
                 response.status(400); // Bad Request
                 return "{\"error\": \"La largeur doit être paire pour une symétrie parfaite.\"}";
            }

            // Logique de Génération 
            Random random = new Random(seed);
            Maze maze = new Maze(width, height); 
            maze.applyTemplate();
            
            //MazeGenerator generator = new MazeGenerator();
            //generator.generate(maze, random); 

            response.type("application/json");
            return maze.toJsonString(); 
        });

        awaitInitialization();
    }

    
    @AfterAll
    public static void tearDown() {
        stop();
        awaitStop();
    }

    // Tests d'intégration HTTP

    @Test
    void testSuccessfulGenerationReturns200AndJson() throws Exception {
        URL url = createUrl(BASE_URL + API_ENDPOINT + "?width=28&height=30");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        int responseCode = connection.getResponseCode();
        
        assertEquals(200, responseCode, "Le code de statut doit être 200 OK.");
        assertEquals("application/json", connection.getContentType(), "Le contenu doit être du JSON.");
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String jsonResponse = in.readLine();
            assertTrue(jsonResponse.contains("\"width\":28"), "Le JSON doit contenir la largeur demandée (28).");
            assertTrue(jsonResponse.contains("\"ident\":"), "Le JSON doit contenir l'identifiant unique du labyrinthe (ident).");
        }
    }

    @Test
    void testInvalidWidthReturns400BadRequest() throws Exception {
        URL url = createUrl(BASE_URL + API_ENDPOINT + "?width=29&height=30");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        int responseCode = connection.getResponseCode();
        
        assertEquals(400, responseCode, "Le code de statut doit être 400 Bad Request.");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
            String errorContent = in.readLine();
            assertTrue(errorContent.contains("symétrie parfaite"), "Le message d'erreur doit mentionner le problème de symétrie.");
        }
    }

    @Test
    void testNonNumericInputUsesDefaultValue() throws Exception {
        URL url = createUrl(BASE_URL + API_ENDPOINT + "?width=ABC&height=30");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        int responseCode = connection.getResponseCode();
        
        assertEquals(200, responseCode, "Le code de statut doit être 200 OK (valeur par défaut utilisée).");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String jsonResponse = in.readLine();
            
            assertTrue(jsonResponse.contains("\"width\":" + WIDTH), "Le JSON doit contenir la largeur par défaut (" + WIDTH + ").");
        }
    }

    // Copies des méthodes utilitaires du Main.java pour parser les paramètres
    private static int getQueryInt(String param, int defaultValue) {
        try {
            return param != null ? Integer.parseInt(param) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}