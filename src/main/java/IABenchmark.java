
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
 * Benchmark IA vs IA : Teste la performance des algorithmes sur le labyrinthe enregistré.
 */
public class IABenchmark {

    private static final int ITERATIONS_PER_CONFIG = 100;
    private static final String MAZE_FILE = "metrics/maze.json"; 
    private static final String OUTPUT_CSV = "metrics/benchmark_ia_results.csv";

    public static void main(String[] args) {
        System.out.println(">>> DÉMARRAGE DU BENCHMARK AUTOMATISÉ (IA vs IA) <<<");
        
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

            // Préparation des résultats
            List<String> results = new ArrayList<>();
            results.add("STRATEGY; GHOST_CONFIG; ITERATION; RESULT; NB_COUPS; SCORE");

            runIAConfigs(data, results);

            // Sauvegarde finale
            Files.write(Paths.get(OUTPUT_CSV), results);
            System.out.println("\n>>> TERMINÉ ! Résultats sauvegardés dans : " + OUTPUT_CSV);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    // Boucle de test pour toutes les configurations de fantômes 
    private static void runIAConfigs(MazeData data, List<String> csvResults) {
        // Il y a 16 configurations possibles (2^4)
        for (int i = 0; i < 16; i++) {
            boolean[] config = new boolean[4];
            config[0] = (i & 1) != 0; // Blinky
            config[1] = (i & 2) != 0; // Pinky
            config[2] = (i & 4) != 0; // Inky
            config[3] = (i & 8) != 0; // Clyde
            
            String configName = configToReadableString(config);
            
            int countAStar = 0;
            for(boolean b : config) if(b) countAStar++;
            String strategyLabel = (countAStar >= 2) ? "MINIMAX" : "EXPECTIMAX";

            System.out.print("Test Config " + configName + " [" + strategyLabel + "] : ");

            int wins = 0;

            // Lancement des 100 itérations
            for (int iter = 1; iter <= ITERATIONS_PER_CONFIG; iter++) {
                GameResult res = simulateAI(data, config);
                
                if (res.won) wins++;

                // Ajout au CSV
                String line = String.format("%s; %s; %d; %s; %d; %d",
                    strategyLabel, configName, iter,
                    res.won ? "WIN" : "LOSE",
                    res.ticks, res.score
                );
                csvResults.add(line);
            }
            System.out.println(" -> " + wins + "% Victoires");
        }
    }

    // Simulateur IA
    private static GameResult simulateAI(MazeData data, boolean[] ghostConfig) {
        GameState state = initGame(data, ghostConfig);
        PacmanAI ai = new PacmanAI();

        // Sélection automatique de la stratégie
        int countAStar = 0;
        for(boolean isAStar : ghostConfig) {
            if(isAStar) countAStar++;
        }

        if (countAStar >= 2) {
            ai.setStrategy(PacmanAI.Strategy.MINIMAX);
        } else {
            ai.setStrategy(PacmanAI.Strategy.EXPECTIMAX);
        }

        while (state.lives() > 0 && !state.levelCleared) {
            // Décision IA
            Action best = ai.getBestAction(state);
            state.setDesiredDir(best);
            // Moteur Physique
            GameLogic.step(state);
            // Sécurité boucle infinie
            if (state.tick() > 15000) break; 
        }
        return new GameResult(state.levelCleared, state.tick(), state.score(), state.lives());
    }

    // Initialisation du jeu avec le labyrinthe donné
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

    // Résultat d'une simulation
    private record GameResult(boolean won, int ticks, int score, int lives) {}
}