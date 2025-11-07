import java.io.*;
import java.net.*;

import javax.swing.*;

import com.google.gson.Gson;
import Model.MazeData; 
import View.MazeVisualizerPanel; 

/**
 * Client local qui se connecte à l'API déployée sur Render pour récupérer le JSON
 * et afficher le labyrinthe graphiquement 
 */
public class LocalClient {

    private static final String API_URL = "https://pacmaz-s1-j.onrender.com/api/labyrinthe";

    public static void main(String[] args) {
        String mazeJson = fetchMazeData(API_URL);
        if (mazeJson != null) {
            MazeData data = parseMazeData(mazeJson);
            if (data != null) {
                displayMaze(data);
            }
        }
    }

    /**
     * Effectue une requête HTTP GET vers l'API Render
     */
    private static String fetchMazeData(String urlString) {
        System.out.println("Requête à l'API Render : " + urlString);
        try {
            // Utilisation de URI pour éviter l'avertissement de dépréciation
            URL url = new URI(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Succès (200 OK): Lire la réponse JSON
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    return response.toString();
                }
            } else {
                // Erreur (400 Bad Request, 500 Internal Server Error)
                System.err.println("Erreur HTTP lors de la récupération du labyrinthe: " + responseCode);
                // Tente de lire le message d'erreur du serveur
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    String error = in.readLine();
                    if (error != null) {
                        System.err.println("Message du serveur: " + error);
                    }
                }
                return null;
            }
        } catch (URISyntaxException e) {
            System.err.println("Erreur de syntaxe d'URL : " + e.getMessage());
            return null;
        } catch (IOException e) {
             System.err.println("Erreur réseau (vérifiez si Render est démarré) : " + e.getMessage());
            return null;
        }
    }

    /**
     * Utilise Gson pour parser la chaîne JSON en objet MazeData
     */
    private static MazeData parseMazeData(String json) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, MazeData.class);
        } catch (Exception e) {
            System.err.println("Erreur de parsing JSON (la structure MazeData est peut-être erronée) : " + e.getMessage());
            return null;
        }
    }

    /**
     * Affiche la grille du labyrinthe dans une fenêtre Swing
     */
    private static void displayMaze(MazeData data) {
        System.out.println("Lancement de la visualisation graphique (W=" + data.width() + ", H=" + data.height() + ")");
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Labyrinthe récupéré de Render");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Utilise la grille int[][] récupérée via JSON pour le dessin
            frame.add(new MazeVisualizerPanel(data.grid())); 
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}