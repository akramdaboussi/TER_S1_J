import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    private static final String INPUT_FILE = "trajectoire_joueur.txt";
    private static final String OUTPUT_FILE = "resultat_simulation.txt";
    
    // Etat du client local
    private static GameState localGameState = null;
    private static Timer timer = null;
    private static MazeData data = null;
    private static final GameClient API_CLIENT = new GameClient();

    // Pour détecter les changements (scores et vies)
    private static int lastScore = 0;
    private static int lastLives = 3;

    // Phases du jeu 
    private enum GamePhase { RECORDING, SIMULATION};
    private static GamePhase currentPhase = GamePhase.RECORDING;

    // Données de jeu
    private static Action desiredAction = Action.NONE;
    private static List<String> recordedPath = new ArrayList<>();   // Partie 1 (Input)
    private static List<String> simulationLog = new ArrayList<>();  // Partie 2 (Output)
    public static void main(String[] args) {
        try {
            // Récupération du labyrinthe du Cloud
            data = API_CLIENT.fetchMazeData();
            JFrame frame = setupDisplay(data);
            // Evaluation
            promptAndSendRating(data.ident());
            // Démarrage du jeu
            startRecordingPhase(frame);
        } catch (URISyntaxException e) {
            System.err.println("Erreur de syntaxe d'URL critique : " + e.getMessage());
        } catch (IOException e) {
             System.err.println("Erreur de communication critique (vérifiez le serveur Cloud) : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
        }
    }

    // --- Phase 1 : Enregistrement des mouvements du joueur ---
    private static void startRecordingPhase(JFrame frame) {
        currentPhase = GamePhase.RECORDING;
        recordedPath.clear();
        desiredAction = Action.NONE; // Reset des inputs clavier
        
        System.out.println(">>> DÉBUT PHASE 1 : ENREGISTREMENT (Joueur aux commandes)");
        startGame(frame); 
    }

    // --- Phase 2 : Simulation (l'ordi rejoue avec les fantômes) ---
    private static void startSimulationPhase(JFrame frame) {
        currentPhase = GamePhase.SIMULATION;
        simulationLog.clear();
        
        // En-tête du fichier
        simulationLog.add("TICK;PAC_X;PAC_Y;GHOST_X;GHOST_Y;EVENT");

        System.out.println(">>> DÉBUT PHASE 2 : SIMULATION (Fantôme)");
        startGame(frame);
    }

    /**
     * Initialise l'état du jeu localement et lance la boucle de jeu.
     */
    private static void startGame(JFrame frame) {
        if (timer != null && timer.isRunning()) timer.stop();
        // Initialisation locale des composants de jeu
        Maze maze = new Maze(data); 
        PelletField pf = PelletPlacer.place(maze); // Placement des pellets sur le labyrinthe évalué
        GameConfig cfg = new GameConfig();
        EntityPos pac = new EntityPos(cfg.pacSpawn.x(), cfg.pacSpawn.y(), cfg.pacSpawn.dx(), cfg.pacSpawn.dy()); 
        EntityPos blinky = new EntityPos(cfg.blinkySpawn.x(), cfg.blinkySpawn.y(), cfg.blinkySpawn.dx(), cfg.blinkySpawn.dy());
        EntityPos pinky = new EntityPos(cfg.pinkySpawn.x(), cfg.pinkySpawn.y(), cfg.pinkySpawn.dx(), cfg.pinkySpawn.dy());
        EntityPos inky = new EntityPos(cfg.inkySpawn.x(), cfg.inkySpawn.y(), cfg.inkySpawn.dx(), cfg.inkySpawn.dy());
        EntityPos clyde = new EntityPos(cfg.clydeSpawn.x(), cfg.clydeSpawn.y(), cfg.clydeSpawn.dx(), cfg.clydeSpawn.dy());
        localGameState = new GameState(maze, pf, cfg, pac, blinky, pinky, inky, clyde);

        // Reset trackers
        lastScore = 0;
        lastLives = localGameState.lives();

        // Récupère l'instance existante du MazeVisualizerPanel
        MazeVisualizerPanel panel = (MazeVisualizerPanel) frame.getContentPane().getComponent(0);
        
        // Premier rendu de l'état (affiche les pastilles et entités)
        updatePanel(panel, createResponse(localGameState));
        panel.requestFocusInWindow();
        // Lancement de la boucle de jeu locale
        startGameLoop(panel);
    }

    /**
     * Boucle principale de jeu : exécute la logique de jeu en local à chaque tick.
     */
    private static void startGameLoop(MazeVisualizerPanel panel) {
        // Délai basé sur la configuration de GameConfig 
        int delay = 1000 / localGameState.cfg.tickPerSecond; 
        
        timer = new Timer(delay, ev -> {
            if (currentPhase == GamePhase.RECORDING) {
                // Mise à jour de la direction souhaitée
                localGameState.setDesiredDir(desiredAction);
                // Mouvement du joueur seul 
                GameLogic.stepRecording(localGameState);
                // Enregistrement de la position du joueur
                recordedPath.add(localGameState.pac.x() + "," + localGameState.pac.y());

                if (localGameState.levelCleared) {
                    ((Timer)ev.getSource()).stop();
                    updatePanel(panel, createResponse(localGameState));
                    panel.paintImmediately(panel.getBounds());
                    delayAndAction(500, () -> handleRecordingFinished(panel));
                    }
            } else {
                int tick = localGameState.tick();
                if (tick < recordedPath.size()) {
                    // Rejoue la direction enregistrée
                    String[] parts = recordedPath.get(tick).split(",");
                    localGameState.pac.setX(Integer.parseInt(parts[0]));
                    localGameState.pac.setY(Integer.parseInt(parts[1]));

                    GameLogic.stepReplay(localGameState);

                    logSimulationStep();
                } else {
                    // Fin de la trajectoire
                    ((Timer)ev.getSource()).stop();
                    updatePanel(panel, createResponse(localGameState));
                    panel.paintImmediately(panel.getBounds());
                    delayAndAction(500, () -> handleSimulationFinished(panel, "Fin de la trajectoire (Survie !)"));
                    return;
                }       
                // Fin si Mort ou Victoire
                if (localGameState.lives() <= 0) {
                    ((Timer)ev.getSource()).stop();
                    String msg = "Pac-Man a été attrapé en " + localGameState.tick() + " coups !";
                    updatePanel(panel, createResponse(localGameState));
                    panel.paintImmediately(panel.getBounds());
                    delayAndAction(500, () -> handleSimulationFinished(panel, msg));
                } else if (localGameState.levelCleared) {
                    ((Timer)ev.getSource()).stop();
                        updatePanel(panel, createResponse(localGameState));
                        panel.paintImmediately(panel.getBounds());
                        delayAndAction(500, () -> handleSimulationFinished(panel, "Niveau terminé !"));
                }
            }
            if (timer.isRunning()){
                updatePanel(panel, createResponse(localGameState));
            }
        });
        timer.start();
    }

    private static void delayAndAction(int ms, Runnable action) {
        Timer t = new Timer(ms, e -> action.run());
        t.setRepeats(false);
        t.start();
    }

    private static void logSimulationStep() {
        String event = "RUNNING";

        int currentScore = localGameState.score();
        int currentLives = localGameState.lives();

        if (currentLives < lastLives) {
            if (currentLives == 0){
                event = "DEATH";
            } else {
                event = "LOSE_LIFE";
            }
        } else if (localGameState.levelCleared) {
            event = "WIN";
        } else {
            int diff = currentScore - lastScore;
            if (diff > 0) {
                if (diff == 10) event = "EAT_PELLET";
                else if (diff == 50) event = "EAT_POWER";
                else if (diff >= 200) event = "EAT_GHOST";
            }
        }
        lastScore = currentScore;
        lastLives = currentLives;
        String line = String.format("%d;%d;%d;%d;%d;%s", 
            localGameState.tick(), localGameState.pac.x(), localGameState.pac.y(),
            localGameState.blinky.x(), localGameState.blinky.y(), localGameState.pinky.x(), localGameState.pinky.y(),
        localGameState.inky.x(), localGameState.inky.y(), localGameState.clyde.x(), localGameState.clyde.y(), event
        );
        simulationLog.add(line);
    }
    
    private static void handleRecordingFinished(MazeVisualizerPanel panel) {
        saveToFile(INPUT_FILE, recordedPath);
        System.out.println("Trajectoire enregistrée.");
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        
        // On lance la simulation ?
        int n = JOptionPane.showConfirmDialog(
            frame,
            "Trajectoire enregistrée.\n\nVoulez-vous lancer la simulation avec le fantôme ?",
            "Phase Suivante",
            JOptionPane.YES_NO_OPTION
        );
        if (n == JOptionPane.YES_OPTION) {
            startSimulationPhase(frame);
        } else {
            System.exit(0);
        }
    }

    private static void handleSimulationFinished(MazeVisualizerPanel panel, String message) {
        saveToFile(OUTPUT_FILE, simulationLog);
        System.out.println("Rapport de simulation généré.");
        
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        Object[] options = {"Nouvelle Trajectoire", "Quitter"};
        
        int n = JOptionPane.showOptionDialog(frame,
            message + "\n\nRésultat sauvegardé dans : " + OUTPUT_FILE + "\nQue faire ?",
            "Fin Simulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]);

        if (n == JOptionPane.YES_OPTION) {
            startRecordingPhase(frame);
        } else {
            System.exit(0);
        }
    }

    private static void saveToFile(String filename, List<String> data) {
        try {
            Files.write(Paths.get(filename), data);
            System.out.println("Fichier sauvegardé : " + filename);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde " + filename + ": " + e.getMessage());
        }
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
                // On ne contrôle la direction que pendant l'enregistrement sans les fantômes
                if (currentPhase == GamePhase.RECORDING){
                    desiredAction = switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP -> Game.Action.UP;
                        case KeyEvent.VK_DOWN -> Game.Action.DOWN;
                        case KeyEvent.VK_LEFT -> Game.Action.LEFT;
                        case KeyEvent.VK_RIGHT -> Game.Action.RIGHT;
                        default -> Game.Action.NONE;
                    };
                }
            }
        });
        return panel;
    }

    /**
     * Affiche la grille du labyrinthe dans une fenêtre Swing
     */
    private static JFrame setupDisplay(MazeData data) {
        JFrame frame = new JFrame("Pac-Man (ID: " + data.ident() + ")");
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
     * Convertit l'état interne du jeu en un DTO de réponse pour l'affichage
     */
    private static GameStateResponse createResponse(GameState s) {
        String mode = (currentPhase == GamePhase.RECORDING) ? "REC (Joueur)" : "SIMULATION (Fantôme)";
        return new GameStateResponse(
            mode, s.tick(), s.score(), s.lives(), s.levelCleared, s.isFrightened(), 
            pos(s.pac), pos(s.blinky), pos(s.pinky), pos(s.inky), pos(s.clyde), s.pellets.remaining(), s.maze.getMazeData(), 
            s.pellets.getSmall(), s.pellets.getPower()
        );
    }

    private static Map<String, Integer> pos(EntityPos e) {
        return Map.of("x", e.x(), "y", e.y(), "dx", e.dx(), "dy", e.dy());
    }

    /**
     * Met à jour le panneau de visualisation avec le nouvel état du jeu
     */
    private static void updatePanel(MazeVisualizerPanel panel, GameStateResponse state) {
        panel.updateGameState(state.pac(), state.blinky(), state.pinky(), state.inky(), state.clyde(), state.smallPellets(), 
            state.powerPellets(), state.isFrightened(), state.score(), state.lives(), state.gameId()
        );
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        if (frame != null) {
            frame.setTitle ("Pac-Man (ID: " + data.ident() + ")");
        }
    }

}