import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import Model.MazeData; 
import View.MazeVisualizerPanel; 
import Game.Action;
import Game.Api.GameClient;
import Model.Maze;
import Game.*;

/**
 * Client Cloud pour Pac-Man
 * Gère le flux d'exécution, l'interface utilisateur et les événements clavier
 * Délègue toutes les opérations réseau à GameClient
 */
public class LocalClient {

    private static GameState localGameState = null;
    private static Action desiredAction = Game.Action.NONE;
    private static final GameClient API_CLIENT = new GameClient();

    public static void main(String[] args) {
        try {
            // Récupération du labyrinthe du Cloud
            MazeData data = API_CLIENT.fetchMazeData();
            JFrame frame = setupDisplay(data);
            // Evaluation
            promptAndSendRating(data.ident());
            // Démarrage du jeu
            startGame(frame, data);
        } catch (URISyntaxException e) {
            System.err.println("Erreur de syntaxe d'URL critique : " + e.getMessage());
        } catch (IOException e) {
             System.err.println("Erreur de communication critique (vérifiez le serveur Cloud) : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
        }
    }

    /**
     * Convertit les MazeData en un MazeVisualizerPanel configuré et ajoute l'écouteur de clavier.
     */
    private static MazeVisualizerPanel createAndConfigurePanel(MazeData data) {
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

        // Utilisation du MazeVisualizerPanel existant
        MazeVisualizerPanel panel = new MazeVisualizerPanel(gridArray);
        
        // Gestion des événements clavier pour capturer les actions de l'utilisateur
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                desiredAction = switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> Game.Action.UP;
                    case KeyEvent.VK_DOWN -> Game.Action.DOWN;
                    case KeyEvent.VK_LEFT -> Game.Action.LEFT;
                    case KeyEvent.VK_RIGHT -> Game.Action.RIGHT;
                    default -> Game.Action.NONE;
                };
            }
        });
        return panel;
    }

    /**
     * Affiche la grille du labyrinthe dans une fenêtre Swing
     */
    private static JFrame setupDisplay(MazeData data) {
        System.out.println("Lancement de la visualisation graphique (W=" + data.width() + ", H=" + data.height() + ")");
        JFrame frame = new JFrame("Labyrinthe récupéré de Render(ID: " + data.ident() + ")");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
        MazeVisualizerPanel panel = createAndConfigurePanel(data);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

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
     * Convertit l'état interne du jeu en un DTO de réponse pour l'affichage
     */
    private static GameStateResponse createResponseFromLocalState(GameState s) {
        // Utilise l'état local pour construire l'objet d'affichage GameStateResponse
        return new GameStateResponse(
            "LOCAL_GAME", 
            s.tick(),
            s.score(),
            s.lives(),
            s.levelCleared,
            s.isFrightened(),
            Map.of("x",s.pac.x(),"y",s.pac.y(),"dx",s.pac.dx(),"dy",s.pac.dy()),
            Map.of("x",s.blinky.x(),"y",s.blinky.y(),"dx",s.blinky.dx(),"dy",s.blinky.dy()),
            s.pellets.remaining(),
            s.maze.getMazeData(), 
            s.pellets.getSmall(),
            s.pellets.getPower()
        );
    }

    /**
     * Initialise l'état du jeu localement et lance la boucle de jeu.
     */
    private static void startGame(JFrame frame, MazeData initialMazeData) throws Exception {
        // Initialisation locale des composants de jeu
        Maze maze = new Maze(initialMazeData); 
        PelletField pf = PelletPlacer.place(maze); // Placement des pellets sur le labyrinthe évalué
        GameConfig cfg = new GameConfig();
        EntityPos pac = new EntityPos(cfg.pacSpawn.x(), cfg.pacSpawn.y(), cfg.pacSpawn.dx(), cfg.pacSpawn.dy()); 
        EntityPos blinky = new EntityPos(cfg.blinkySpawn.x(), cfg.blinkySpawn.y(), cfg.blinkySpawn.dx(), cfg.blinkySpawn.dy());
        localGameState = new GameState(maze, pf, cfg, pac, blinky);
        
        System.out.println("Partie démarrée");

        // Récupère l'instance existante du MazeVisualizerPanel
        MazeVisualizerPanel panel = (MazeVisualizerPanel) frame.getContentPane().getComponent(0);
        
        // Premier rendu de l'état (affiche les pastilles et entités)
        updatePanel(panel, createResponseFromLocalState(localGameState));

        // Lancement de la boucle de jeu locale
        startLocalGameLoop(panel);
    }


    /**
     * Boucle principale de jeu : exécute la logique de jeu en local à chaque tick.
     */
    private static void startLocalGameLoop(MazeVisualizerPanel panel) {
        // Délai basé sur la configuration de GameConfig 
        int delay = 1000 / localGameState.cfg.tickPerSecond; 
        
        Timer timer = new Timer(delay, ev -> {
            // Mise à jour de la direction souhaitée
            localGameState.setDesiredDir(desiredAction); 
            
            // Exécution de la logique de jeu locale
            GameLogic.step(localGameState);
            
            GameStateResponse newState = createResponseFromLocalState(localGameState);

            if (newState != null) {
                updatePanel(panel, newState);
                
                // Vérification des conditions de fin de partie
                if (newState.levelCleared() || newState.lives() <= 0) {
                    ((Timer)ev.getSource()).stop();
                    String message = newState.levelCleared() ? "Niveau terminé!" : "Game Over!";
                    JOptionPane.showMessageDialog(null, message + " Score final: " + newState.score(), "Fin de Partie", JOptionPane.INFORMATION_MESSAGE);
                }
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