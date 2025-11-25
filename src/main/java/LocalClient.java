import java.io.*;
import java.net.*;
import java.util.List;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import Model.MazeData; 
import View.MazeVisualizerPanel; 
import Game.Action;
import Game.Api.GameClient;
import Game.GameStateResponse;

/**
 * Client Cloud pour Pac-Man
 * Gère le flux d'exécution, l'interface utilisateur et les événements clavier
 * Délègue toutes les opérations réseau à GameClient
 */
public class LocalClient {

    private static String currentGameId = null;
    private static Action desiredAction = Action.NONE; // Action souhaitée par l'utilisateur
    private static final GameClient API_CLIENT = new GameClient();

    public static void main(String[] args) {
        try {
            MazeData data = API_CLIENT.fetchMazeData();
            JFrame frame = setupDisplay(data);
            promptAndSendRating(data.ident());
            startGame(frame);
        } catch (URISyntaxException e) {
            System.err.println("Erreur de syntaxe d'URL critique : " + e.getMessage());
        } catch (IOException e) {
             System.err.println("Erreur de communication critique (vérifiez le serveur Cloud) : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
        }
    }

    /**
     * Affiche la grille du labyrinthe dans une fenêtre Swing
     */
    private static JFrame setupDisplay(MazeData data) {
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

        JFrame frame = new JFrame("Labyrinthe récupéré de Render(ID: " + data.ident() + ")");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
        MazeVisualizerPanel panel = new MazeVisualizerPanel(gridArray);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Gestion des événements clavier pour capturer les actions de l'utilisateur
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                desiredAction = switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> Action.UP;
                    case KeyEvent.VK_DOWN -> Action.DOWN;
                    case KeyEvent.VK_LEFT -> Action.LEFT;
                    case KeyEvent.VK_RIGHT -> Action.RIGHT;
                    default -> Action.NONE;
                };
            }
        });
        panel.requestFocusInWindow();
        return frame;
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
                    API_CLIENT.sendRating(ident, note);
                    JOptionPane.showMessageDialog(null, "Notation enregistrée (Note: " + note + ")!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "La note doit être entre 0 et 5.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Veuillez entrer un nombre valide.", "Erreur de format", JOptionPane.ERROR_MESSAGE);
            } catch (IOException | URISyntaxException e) { 
                 JOptionPane.showMessageDialog(null, "Erreur réseau lors de la notation : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else if (input != null) {
            System.out.println("Notation annulée par l'utilisateur.");
        }
    }

    /**
     * Démarre la session de jeu sur le Cloud et lance la boucle de jeu
     */
    private static void startGame(JFrame frame) throws Exception {
        // Utilise GameClient
        GameStateResponse initialState = API_CLIENT.startGame();
        currentGameId = initialState.gameId();
        System.out.println("Partie démarrée. ID de la session : " + currentGameId);

        MazeVisualizerPanel panel = (MazeVisualizerPanel) frame.getContentPane().getComponent(0);
        updatePanel(panel, initialState);

        startCloudGameLoop(panel);
    }

    /**
     * Boucle principale de jeu : envoie les actions au Cloud et met à jour l'affichage
     */
    private static void startCloudGameLoop(MazeVisualizerPanel panel) {
        // Timer Swing pour gérer la boucle de jeu
        Timer timer = new Timer(120, ev -> {
            try {
                // Utilise GameClient pour envoyer l'action et recevoir le nouvel état
                GameStateResponse newState = API_CLIENT.sendActionAndGetState(currentGameId, desiredAction);
                if (newState != null) {
                    updatePanel(panel, newState);
                    // On réinitialise l'action à NONE
                    desiredAction = Action.NONE;
                    if (newState.levelCleared()) {
                        ((Timer)ev.getSource()).stop();
                        JOptionPane.showMessageDialog(null, "Niveau terminé! Score final: " + newState.score(), "Fin de Partie", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                     // Arrête le timer en cas d'erreur
                     throw new IOException("Réponse du serveur vide ou erreur lors du step.");
                }
            } catch (Exception e) {
                 ((Timer)ev.getSource()).stop();
                 JOptionPane.showMessageDialog(null, "Erreur critique dans la boucle de jeu : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        timer.start();
    }

    /**
     * Met à jour le panneau de visualisation avec le nouvel état du jeu
     */
    private static void updatePanel(MazeVisualizerPanel panel, GameStateResponse state) {
        panel.setCloudGameData(
            state.pac(), 
            state.blinky(), 
            state.smallPellets(), 
            state.powerPellets(),
            state.isFrightened()
        );
        // Mise à jour du titre de la fenêtre pour afficher le score
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        frame.setTitle(String.format("Cloud Pac-Man (ID: %s) - Score: %d | Vies: %d", 
                        state.gameId().substring(0, 8), state.score(), state.lives()));
    }

}