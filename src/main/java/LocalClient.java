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
 * Client local
 * Gère l'initialisation, la boucle de jeu principale et l'interface utilisateur.
 */
public class LocalClient {

    private static final String INPUT_FILE = "trajectoire_joueur.txt";
    private static final String OUTPUT_FILE = "resultat_simulation.txt";
    
    // --- Etat Global ---
    private static GameState localGameState = null;
    private static Timer timer = null;
    private static MazeData data = null;
    private static final GameClient API_CLIENT = new GameClient();
    private static final PacmanAI bot = new PacmanAI();

    // --- Suivi du score et des vies pour les logs ---
    private static int lastScore = 0;
    private static int lastLives = 3;

    // --- Phases de jeu ---
    private enum GamePhase { DIRECT_PLAY, RECORDING, SIMULATION, AI_PLAY};
    private static GamePhase currentPhase = GamePhase.DIRECT_PLAY;

    // Pour l'affichage et le restart
    private static PacmanAI.Strategy currentAIStrategy = PacmanAI.Strategy.EXPECTIMAX;
    private static boolean currentGhostAStar = false;

    // --- Données du jeu ---
    private static Action desiredAction = Action.NONE;
    private static List<String> recordedPath = new ArrayList<>();   // Partie 1 (Input)
    private static List<String> simulationLog = new ArrayList<>();  // Partie 2 (Output)

    public static void main(String[] args) {
        try {
            int w = Integer.getInteger("w", 28);
            int h = Integer.getInteger("h", 31);
            // Récupération du labyrinthe du Cloud
            data = API_CLIENT.fetchMazeData(w,h);

            // Initialisation de l'interface Swing
            SwingUtilities.invokeLater(() -> {
                try {
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    JDialog.setDefaultLookAndFeelDecorated(true);

                    // Setuup et Lancement 
                    JFrame frame = setupDisplay(data);
                    promptAndSendRating(frame, data.ident());
                    showMainMenu(frame);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (URISyntaxException e) {
            System.err.println("Erreur de syntaxe d'URL critique : " + e.getMessage());
        } catch (IOException e) {
             System.err.println("Erreur de communication critique (vérifiez le serveur Cloud) : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
        }
    }

    // Affiche le menu principal et gère le choix de l'utilisateur
    private static void showMainMenu(JFrame frame) {
        Object[] options = {"Jouer (Manuel)", "Enregistrer un Parcours", "IA vs IA"};
        int n = JOptionPane.showOptionDialog(frame,
            "Bienvenue dans Pac-Man !\nQue voulez-vous faire ?",
            "Menu Principal",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]); // Par défaut : Jouer

        if (n == 0){
            boolean useAStar = askGhostDifficulty(frame);
            startDirectPlayPhase(frame, useAStar);    
        }
        else if (n == 1) startRecordingPhase(frame);
        else if (n == 2){
            boolean useAStar = askGhostDifficulty(frame);
            PacmanAI.Strategy strategy = useAStar ? PacmanAI.Strategy.MINIMAX : PacmanAI.Strategy.EXPECTIMAX;
            startAIPhase(frame, strategy);
        } else System.exit(0);
    }

    // --- Demande le type d'IA des fantômes ---
    private static boolean askGhostDifficulty(JFrame frame) {
        Object[] options = {"Gloutons", "A*"};
        int n = JOptionPane.showOptionDialog(frame,
            "Choisissez l'intelligence des fantômes :",
            "Difficulté",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        return (n == 1); // Retourne true si A* choisi
    }

    // --- Jeu direct contre les fantômes ---
    private static void startDirectPlayPhase(JFrame frame, boolean userAStar) {
        currentPhase = GamePhase.DIRECT_PLAY;
        currentGhostAStar = userAStar;
        GameLogic.GHOST_A_STAR = userAStar;
        desiredAction = Action.NONE;
        System.out.println(">>> MODE : JEU DIRECT (Fantomes " + (userAStar ? "A*" : "Gloutons") + ")");
        startGame(frame);
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
    private static void startSimulationPhase(JFrame frame, boolean useAStar) {
        currentPhase = GamePhase.SIMULATION;
        currentGhostAStar = useAStar;
        GameLogic.GHOST_A_STAR = useAStar;
        simulationLog.clear();
        
        // En-tête du fichier
        simulationLog.add("TICK;PAC_X;PAC_Y;BLINKY_X;BLINKY_Y;PINKY_X;PINKY_Y;INKY_X;INKY_Y;CLYDE_X;CLYDE_Y;EVENT");

        System.out.println(">>> DÉBUT PHASE 2 : SIMULATION (Fantomes " + (useAStar ? "A*" : "Gloutons") + ")");
        startGame(frame);
    }

    // --- Phase 3 : IA joue à la place du joueur ---
    private static void startAIPhase(JFrame frame, PacmanAI.Strategy strategy) {
        currentPhase = GamePhase.AI_PLAY;
        currentAIStrategy = strategy;
        bot.setStrategy(strategy);

        if (strategy == PacmanAI.Strategy.MINIMAX) {
            GameLogic.GHOST_A_STAR = true;
            currentGhostAStar = true;
        } else {
            GameLogic.GHOST_A_STAR = false;
            currentGhostAStar = false;
        }

        desiredAction = Action.NONE;
        System.out.println(">>> MODE : IA AUTO (" + strategy + ")");
        startGame(frame);
    }

    /**
     * Initialise et démarre une nouvelle partie locale.
     */
    private static void startGame(JFrame frame) {
        if (timer != null && timer.isRunning()) timer.stop();

        // Initialisation locale des composants de jeu
        Maze maze = new Maze(data); 
        PelletField pf = PelletPlacer.place(maze); 
        GameConfig cfg = new GameConfig(maze.getWidth(), maze.getHeight());

        // Position de départ du Pac-Man
        EntityPos pac = new EntityPos(cfg.pacSpawn.x(), cfg.pacSpawn.y(), cfg.pacSpawn.dx(), cfg.pacSpawn.dy());

        // Positions de départ des fantômes
        EntityPos blinky = new EntityPos(cfg.blinkySpawn.x(), cfg.blinkySpawn.y(), cfg.blinkySpawn.dx(), cfg.blinkySpawn.dy());
        EntityPos pinky = new EntityPos(cfg.pinkySpawn.x(), cfg.pinkySpawn.y(), cfg.pinkySpawn.dx(), cfg.pinkySpawn.dy());
        EntityPos inky = new EntityPos(cfg.inkySpawn.x(), cfg.inkySpawn.y(), cfg.inkySpawn.dx(), cfg.inkySpawn.dy());
        EntityPos clyde = new EntityPos(cfg.clydeSpawn.x(), cfg.clydeSpawn.y(), cfg.clydeSpawn.dx(), cfg.clydeSpawn.dy());

        // Création de l'état du jeu local
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
            if (currentPhase == GamePhase.DIRECT_PLAY) {
                // Mise à jour de la direction souhaitée
                localGameState.setDesiredDir(desiredAction);
                // Mouvement du joueur et des fantômes
                GameLogic.step(localGameState);
                // Fin si Mort ou Victoire
                if (localGameState.lives() <= 0) {
                    ((Timer)ev.getSource()).stop();
                    finishPhase(panel, () -> handleGameOver(panel, "PERDU ! Pac-Man a été mangé en " + localGameState.tick() + " coups."));
                } else if (localGameState.levelCleared) {
                    ((Timer)ev.getSource()).stop();
                    finishPhase(panel, () -> handleGameOver(panel, "GAGNÉ ! Partie terminée."));
                }
            } else if (currentPhase == GamePhase.AI_PLAY) {
                // L'IA choisit la meilleure action
                Action best = bot.getBestAction(localGameState);

                localGameState.setDesiredDir(best);
                GameLogic.step(localGameState);
                if (localGameState.lives() <= 0) {
                    ((Timer)ev.getSource()).stop();
                    finishPhase(panel, () -> handleIAGameOver(panel, "L'IA a perdu...", currentAIStrategy));
                } else if (localGameState.levelCleared) {
                    ((Timer)ev.getSource()).stop();
                    finishPhase(panel, () -> handleIAGameOver(panel, "L'IA a gagné le niveau !", currentAIStrategy));
                }
            } else if (currentPhase == GamePhase.RECORDING) {
                // Mise à jour de la direction souhaitée
                localGameState.setDesiredDir(desiredAction);
                // Mouvement du joueur seul 
                GameLogic.stepRecording(localGameState);
                // Enregistrement de la position du joueur
                recordedPath.add(localGameState.pac.x() + "," + localGameState.pac.y());

                // Fin si Mort ou Victoire
                if (localGameState.levelCleared) {
                    ((Timer)ev.getSource()).stop();
                    finishPhase(panel, () -> handleRecordingFinished(panel));
                }
            } else {
                // Simulation avec les fantômes
                int tick = localGameState.tick();
                if (tick < recordedPath.size()) {
                    // Rejoue la direction enregistrée
                    String[] parts = recordedPath.get(tick).split(",");
                    localGameState.pac.setX(Integer.parseInt(parts[0]));
                    localGameState.pac.setY(Integer.parseInt(parts[1]));

                    GameLogic.stepReplay(localGameState);
                    logSimulationStep();
                } else {
                    // Fin du replay
                    ((Timer)ev.getSource()).stop();
                    finishPhase(panel, () -> handleSimulationFinished(panel, "Fin de la trajectoire (Survie !)"));
                    return;
                }       
                // Fin si Mort ou Victoire
                if (localGameState.lives() <= 0) {
                    ((Timer)ev.getSource()).stop();
                    String msg = "PERDU ! Pac-Man a été mangé en " + localGameState.tick() + " coups.";
                    finishPhase(panel, () -> handleSimulationFinished(panel, msg));
                } else if (localGameState.levelCleared) {
                    ((Timer)ev.getSource()).stop();
                    finishPhase(panel, () -> handleSimulationFinished(panel, "Niveau terminé !"));
                }
            }
            // Mise à jour de l'affichage 
            if (timer.isRunning()){
                updatePanel(panel, createResponse(localGameState));
            }
        });
        timer.start();
    }

    // Helper pour terminer une phase proprement avec un petit délai visuel
    private static void finishPhase(MazeVisualizerPanel panel, Runnable action) {
        updatePanel(panel, createResponse(localGameState));
        panel.paintImmediately(panel.getBounds());
        Timer t = new Timer(500, e -> action.run());
        t.setRepeats(false);
        t.start();
    }

    // --- Journalisation de la simulation ---

    // Enregistre un pas de la simulation dans le log
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

        String line = String.format("%d; %d; %d; %d; %d; %d; %d; %d; %d; %d; %d; %s", 
            localGameState.tick(), localGameState.pac.x(), localGameState.pac.y(),
            localGameState.blinky.pos.x(), localGameState.blinky.pos.y(), localGameState.pinky.pos.x(), localGameState.pinky.pos.y(),
            localGameState.inky.pos.x(), localGameState.inky.pos.y(), localGameState.clyde.pos.x(), localGameState.clyde.pos.y(), event
        );
        simulationLog.add(line);
    }

    // Fin pour le mode "Jeu Direct"
    private static void handleGameOver(MazeVisualizerPanel panel, String message) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        Object[] options = {"Rejouer (Même IA)","Changer IA", "Retour au Menu", "Quitter"};
        
        int n = JOptionPane.showOptionDialog(frame, 
            message + "\nScore final : " + localGameState.score(), 
            "Fin de Partie", 
            JOptionPane.YES_NO_CANCEL_OPTION, 
            JOptionPane.INFORMATION_MESSAGE, 
            null, options, options[0]);

        if (n == 0) startDirectPlayPhase(frame, currentGhostAStar);
        else if (n == 1) {
            boolean useAStar = askGhostDifficulty(frame);
            startDirectPlayPhase(frame, useAStar);
        }
        else if (n == 2) showMainMenu(frame);
        else System.exit(0);
    }

    // Fin pour le mode "Jeu Direct"
    private static void handleIAGameOver(MazeVisualizerPanel panel, String message, PacmanAI.Strategy strategy) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        Object[] options = {"Rejouer (Même IA)", "Changer IA", "Retour au Menu", "Quitter"};
        
        int n = JOptionPane.showOptionDialog(frame, 
            message + "\nScore final : " + localGameState.score(), 
            "Fin de Partie IA", 
            JOptionPane.YES_NO_CANCEL_OPTION, 
            JOptionPane.INFORMATION_MESSAGE, 
            null, options, options[0]);

        if (n == 0) startAIPhase(frame, strategy);
        else if (n == 1) {
            boolean useAStar = askGhostDifficulty(frame);
            PacmanAI.Strategy newStrategy = useAStar ? PacmanAI.Strategy.MINIMAX : PacmanAI.Strategy.EXPECTIMAX;
            startAIPhase(frame, newStrategy);
        } else if (n == 2) showMainMenu(frame);
        else System.exit(0);
    }
    
    // Gère la fin de l'enregistrement et affiche les options à l'utilisateur
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
            // On demande la difficulté des fantômes
            boolean useAStar = askGhostDifficulty(frame);
            startSimulationPhase(frame, useAStar);
        } else {
            System.exit(0);
        }
    }

    // Gère la fin de la simulation et affiche les options à l'utilisateur
    private static void handleSimulationFinished(MazeVisualizerPanel panel, String message) {
        saveToFile(OUTPUT_FILE, simulationLog);
        System.out.println("Rapport de simulation généré.");
        
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        Object[] options = {"Rejouer Simulation", "Nouvelle Trajectoire","Retour au Menu", "Quitter"};
        
        int n = JOptionPane.showOptionDialog(frame,
            message + "\n\nRésultat sauvegardé dans : " + OUTPUT_FILE + "\nQue faire ?",
            "Fin Simulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]);

        if (n == 0) {
            boolean useAStar = askGhostDifficulty(frame);
            startSimulationPhase(frame, useAStar);
        } else if (n == 1) {
            startRecordingPhase(frame);
        } else if (n == 2) {
            showMainMenu(frame);
        } else {
            System.exit(0);
        }
    }

    // Sauvegarde une liste de chaînes dans un fichier
    private static void saveToFile(String filename, List<String> data) {
        try {
            Files.write(Paths.get(filename), data);
            System.out.println("Fichier sauvegardé : " + filename);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde " + filename + ": " + e.getMessage());
        }
    }

    // --- Interface Graphique et Interaction avec l'API ---

    // Demande à l'utilisateur une note (0-5) et envoie la notation à l'API Render via POST
    private static void promptAndSendRating(JFrame parent, String ident) {
        String input = JOptionPane.showInputDialog(
            parent, 
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

    // Crée le panneau de jeu et configure les contrôles clavier
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
                if (currentPhase == GamePhase.RECORDING || currentPhase == GamePhase.DIRECT_PLAY) {
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

    // Affiche la grille du labyrinthe dans une fenêtre Swing
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

    // --- DTO et Transfert des données ---

    // Convertit l'état interne du jeu en un DTO de réponse pour l'affichage
    private static GameStateResponse createResponse(GameState s) {
        String mode = switch(currentPhase) {
            case DIRECT_PLAY -> "JEU (Direct)";
            case RECORDING -> "REC (Joueur)";
            case SIMULATION -> "REPLAY (IA)";
            case AI_PLAY -> "IA (" + currentAIStrategy + ")";
        };
        return new GameStateResponse(
            mode, s.tick(), s.score(), s.lives(), s.levelCleared, s.isFrightened(), 
            pos(s.pac), pos(s.blinky.pos), pos(s.pinky.pos), pos(s.inky.pos), pos(s.clyde.pos), s.pellets.remaining(), s.maze.getMazeData(), 
            s.pellets.getSmall(), s.pellets.getPower()
        );
    }

    private static Map<String, Integer> pos(EntityPos e) {
        return Map.of("x", e.x(), "y", e.y(), "dx", e.dx(), "dy", e.dy());
    }

    
    // Met à jour le panneau de visualisation avec le nouvel état du jeu.
    private static void updatePanel(MazeVisualizerPanel panel, GameStateResponse state) {
        panel.updateGameState(state.pac(), state.blinky(), state.pinky(), state.inky(), state.clyde(), state.smallPellets(), 
            state.powerPellets(), state.isFrightened(), state.score(), state.lives(), state.gameId()
        );
    }
}