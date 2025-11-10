import java.io.*;
import java.net.*;
import java.util.List;
import java.util.stream.Collectors;
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
    private static final String API_RATING_URL = "https://pacmaz-s1-j.onrender.com/api/labyrinthe/note"; 
    public static void main(String[] args) {
        String mazeJson = fetchMazeData(API_URL);
        if (mazeJson != null) {
            MazeData data = parseMazeData(mazeJson);
            if (data != null) {
                displayMaze(data);
                promptAndSendRating(data.ident());
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
        List<List<Integer>> gridList = data.grid();
        int height = gridList.size();
        int width = gridList.get(0).size();
        int[][] gridArray = new int[height][width];

        for (int y = 0; y < height; y++) {
            List<Integer> row = gridList.get(y);
            for (int x = 0; x < width; x++) {
                gridArray[y][x] = row.get(x);
            }
        }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Labyrinthe récupéré de Render(ID: " + data.ident() + ")");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Utilise la grille int[][] récupérée via JSON pour le dessin
            frame.add(new MazeVisualizerPanel(gridArray)); 
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * Demande à l'utilisateur une note (0-5) et envoie la notation à l'API Render via POST
     */
    private static void promptAndSendRating(String ident) {
        String input = JOptionPane.showInputDialog(
            null, 
            "Entrez votre note pour le labyrinthe (0: bad, 5: good):", 
            "Évaluation du Labyrinthe", 
            JOptionPane.QUESTION_MESSAGE
        );
        if (input != null && !input.trim().isEmpty()) {
            try {
                int note = Integer.parseInt(input.trim());
                if (note >= 0 && note <= 5) {
                    sendRating(ident, note);
                } else {
                    JOptionPane.showMessageDialog(null, "La note doit être entre 0 et 5.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Veuillez entrer un nombre valide.", "Erreur de format", JOptionPane.ERROR_MESSAGE);
            }
        } else if (input != null) {
            System.out.println("Notation annulée par l'utilisateur.");
        }
    }

    /**
     * Envoie la notation à l'API Render via une requête HTTP POST
     */
    private static void sendRating(String ident, int note) {
        System.out.println("Envoi de la note " + note + " pour l'ID " + ident + "...");
        try {
            // Utilisation de URI pour éviter l'avertissement de dépréciation
            URL url = new URI(API_RATING_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Configuration de la connexion POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json"); // Indique que nous envoyons du JSON
            connection.setDoOutput(true); // Permet d'écrire dans le corps de la requête
            
            // Corps de la requête JSON
            String jsonInputString = String.format("{\"ident\": \"%s\", \"note\": %d}", ident, note);
            
            // Envoi du JSON
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);			
            }

            // Lecture de la réponse du serveur
            int responseCode = connection.getResponseCode();
            System.out.println("Réponse du serveur (Notation) : " + responseCode);

            InputStream inputStream = (responseCode >= 200 && responseCode <= 299) ? connection.getInputStream() : connection.getErrorStream();
            
            try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
                // Lit toute la réponse
                String responseText = br.lines().collect(Collectors.joining("\n"));
                System.out.println("Message du serveur : " + responseText);

                if (responseCode == 200) {
                     JOptionPane.showMessageDialog(null, "Notation enregistrée (Note: " + note + ")!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } else {
                     JOptionPane.showMessageDialog(null, "Erreur de notation (" + responseCode + "): " + responseText, "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (URISyntaxException e) {
            System.err.println("Erreur de syntaxe d'URL : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur réseau lors de l'envoi de la note : " + e.getMessage());
        }
    }

}