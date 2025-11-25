package Game.Api;

import java.io.*;
import java.net.*;
import java.util.stream.Collectors;
import com.google.gson.Gson;

import Model.MazeData;
import Game.Action;
import Game.GameStateResponse;

/**
 * Client HTTP dédié pour interagir avec l'API Pac-Man sur le Render
 * Il encapsule toutes les requêtes réseau
 */
public class GameClient {

    private static final String API_BASE_URL = "https://pacmaz-s1-j.onrender.com/api";
    private static final String API_MAZE_URL = API_BASE_URL + "/labyrinthe";
    private static final String API_RATING_URL = API_BASE_URL + "/labyrinthe/note"; 
    private static final String API_GAME_START_URL = API_BASE_URL + "/game/start"; 
    private static final String API_GAME_STEP_URL = API_BASE_URL + "/game/%s/step"; 

    private final Gson gson = new Gson();

    /**
     * Effectue une requête HTTP GET vers l'API Render pour récupérer un labyrinthe
     */
    public MazeData fetchMazeData() throws IOException, URISyntaxException {
        System.out.println("Requête à l'API Render : " + API_MAZE_URL);
        String jsonResponse = sendGet(API_MAZE_URL);
        return gson.fromJson(jsonResponse, MazeData.class);
    }

    /**
     * Envoie la note d'évaluation à l'API Render
     */
    public int sendRating(String ident, int note) throws IOException, URISyntaxException {
        System.out.println("Envoi de la note " + note + " pour l'ID " + ident + "...");
        String jsonInputString = String.format("{\"ident\": \"%s\", \"note\": %d}", ident, note);
        sendPost(API_RATING_URL, jsonInputString, null); 
        return 200;
    }

    /**
     * Démarre une nouvelle session de jeu sur le Render et retourne l'état initial du jeu
     */
    public GameStateResponse startGame() throws IOException, URISyntaxException {
        System.out.println("Démarrage de la session de jeu sur le Cloud...");
        String jsonResponse = sendPost(API_GAME_START_URL, "", null);
        return gson.fromJson(jsonResponse, GameStateResponse.class);
    }

    /**
     * Envoie l'action souhaitée au Render et récupère le nouvel état du jeu.
     * @return L'état du jeu après l'action
     */
    public GameStateResponse sendActionAndGetState(String gameId, Action action) throws IOException, URISyntaxException {
        String urlString = String.format(API_GAME_STEP_URL, gameId);
        String jsonInputString = gson.toJson(action.toString());
        String jsonResponse = sendPost(urlString, jsonInputString, null);
        return gson.fromJson(jsonResponse, GameStateResponse.class);
    }

    // --- Méthodes Utilitaire de Connexion HTTP ---

    private String sendGet(String urlString) throws IOException, URISyntaxException {
        URL url = new URI(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return readResponse(connection.getInputStream());
        } else {
            handleHttpError(connection, responseCode, "GET");
            throw new IOException("Erreur HTTP lors de la requête GET: " + responseCode);
        }
    }

    // Retourne le corps de la réponse en cas de succès
    private String sendPost(String urlString, String jsonInput, String gameId) throws IOException, URISyntaxException {
        URL url = new URI(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json"); 
        connection.setDoOutput(true); 
        
        // Envoi du JSON (même s'il est vide pour startGame)
        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);			
        }
        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode <= 299) {
            // Succès
            return readResponse(connection.getInputStream());
        } else {
            handleHttpError(connection, responseCode, "POST");
            throw new IOException("Erreur HTTP lors de la requête POST: " + responseCode);
        }
    }
    
    private String readResponse(InputStream stream) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
            return in.lines().collect(Collectors.joining("\n"));
        }
    }

    private void handleHttpError(HttpURLConnection connection, int responseCode, String method) throws IOException {
        System.err.println("Erreur HTTP ("+method+") : " + responseCode);
        // Tente de lire le message d'erreur du serveur
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
            String error = in.readLine();
            if (error != null) {
                System.err.println("Message du serveur: " + error);
            }
        }
    }
}