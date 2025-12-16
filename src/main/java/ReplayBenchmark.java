
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson; 

import Game.*;
import Model.Maze;
import Model.MazeData;

/**
 * Benchmark Replay : Rejoue la trajectoire enregistrée du joueur face aux IA fantômes
 * pour mesurer la résistance du parcours contre différentes configurations de fantômes.
 */
public class ReplayBenchmark {

    private static final int ITERATIONS_PER_CONFIG = 100;
    private static final String INPUT_FILE = "metrics/trajectoire_joueur.txt";
    private static final String MAZE_FILE = "metrics/maze.json"; 
    private static final String OUTPUT_CSV = "metrics/benchmark_replay_score.csv"; 

    public static void main(String[] args) {
        System.out.println(">>> DÉMARRAGE DU BENCHMARK AUTOMATISÉ (SIMULATION SCORE) <<<");
        
        try {
            MazeData data = null;

            // Charger le labyrinthe local (celui de l'enregistrement LocalClient)
            if (Files.exists(Paths.get(MAZE_FILE))) {
                try (Reader reader = new FileReader(MAZE_FILE)) {
                    data = new Gson().fromJson(reader, MazeData.class);
                    System.out.println("SUCCÈS : Labyrinthe 'maze.json' chargé.");
                } catch (Exception e) {
                    System.err.println("Erreur lecture maze.json : " + e.getMessage());
                }
            }

            if (!Files.exists(Paths.get(INPUT_FILE))) {
                System.err.println("ERREUR : 'trajectoire_joueur.txt' manquant."); return;
            }
            List<String> path = Files.readAllLines(Paths.get(INPUT_FILE));

            List<String> results = new ArrayList<>();
            results.add("GHOST_CONFIG; ITERATION; SCORE_FINAL; NB_COUPS; RESULT");

            runReplayConfigs(data, path, results);

            Files.write(Paths.get(OUTPUT_CSV), results);
            System.out.println("\n>>> TERMINÉ ! Résultats sauvegardés dans : " + OUTPUT_CSV);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // Boucle de test pour toutes les configurations de fantômes
    private static void runReplayConfigs(MazeData data, List<String> path, List<String> csvResults) {
        for (int i = 0; i < 16; i++) {
            boolean[] config = new boolean[4];
            config[0] = (i & 1) != 0; // Blinky
            config[1] = (i & 2) != 0; // Pinky
            config[2] = (i & 4) != 0; // Inky
            config[3] = (i & 8) != 0; // Clyde
            
            String configName = configToReadableString(config);
            System.out.print("Simulation Config [" + configName + "] : ");

            long totalScore = 0;
            for (int iter = 1; iter <= ITERATIONS_PER_CONFIG; iter++) {
                GameResult res = simulateReplay(data, config, path);
                totalScore += res.score;

                String line = String.format("%s; %d; %d; %d; %s",
                    configName, iter, res.score, res.ticks, res.won ? "WIN" : "LOSE"
                );
                csvResults.add(line);
            }
            // Affichage du score moyen
            System.out.println(" -> Score Moyen : " + (totalScore / ITERATIONS_PER_CONFIG));
        }
    }

    // Simulation d'une partie en mode Replay
    private static GameResult simulateReplay(MazeData data, boolean[] ghostConfig, List<String> path) {
        GameState state = initGame(data, ghostConfig);
        
        while (state.lives() > 0 && !state.levelCleared) {
            int tick = state.tick();
            if (tick >= path.size()) break;
            try {
                String line = path.get(tick);
                String[] parts;
                if (line.contains(";")) {
                    String[] csvParts = line.split(";");
                    parts = new String[]{csvParts[3], csvParts[4]}; 
                } else {
                    parts = line.split(",");
                }
                state.pac.setX(Integer.parseInt(parts[0].trim()));
                state.pac.setY(Integer.parseInt(parts[1].trim()));
            } catch (Exception e) { break; }
            GameLogic.stepReplay(state);
        }
        return new GameResult(state.levelCleared, state.tick(), state.score(), state.lives());
    }

    // Initialisation du jeu
    private static GameState initGame(MazeData data, boolean[] ghostConfig) {
        Maze maze = new Maze(data);
        PelletField pf = PelletPlacer.place(maze);
        GameConfig cfg = new GameConfig(maze.getWidth(), maze.getHeight());
        
        return new GameState(
            maze, pf, cfg, cfg.pacSpawn.copy(),
            cfg.blinkySpawn.copy(), cfg.pinkySpawn.copy(), 
            cfg.inkySpawn.copy(), cfg.clydeSpawn.copy(),
            ghostConfig
        );
    }

    // Conversion configuration binaire -> lisible
    private static String configToReadableString(boolean[] c) {
        return (c[0]?"A*":"G") + "-" + 
               (c[1]?"A*":"G") + "-" + 
               (c[2]?"A*":"G") + "-" + 
               (c[3]?"A*":"G");
    }    

    // Résultat de la simulation
    private record GameResult(boolean won, int ticks, int score, int lives) {}
}