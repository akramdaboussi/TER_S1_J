import java.util.Random;
import javax.swing.*;
import com.google.gson.Gson;
import Generator.*;
import Model.*;
import View.*;

/*
 * Classe principale de l'application.
 * Gère la configuration, l'analyse des arguments de la ligne de commande,
 * et effectue la génération et l'affichage du labyrinthe.
*/
public class Main {

    // Configuration par défaut
    private static final int WIDTH = 28;
    private static final int HEIGHT = 31;
    private static double IMPERFECTION_PERCENTAGE = 0.2; // 20% (peut être modifié via les arguments)

    /*
     * Point d'entrée du programme.
     * @param args Arguments de la ligne de commande (--seed, --imperfection).
    */
    public static void main(String[] args) {
        long seed = System.currentTimeMillis();
        double imperfection = IMPERFECTION_PERCENTAGE;

        // Analyse des arguments pour surcharger la configuration par défaut
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--seed") && i + 1 < args.length) {
                try {
                    seed = Long.parseLong(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.err.println("Erreur: Le seed doit être un nombre entier.");
                }
            }
            if (args[i].equals("--imperfection") && i + 1 < args.length) {
                try {
                    imperfection = Double.parseDouble(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.err.println("Erreur: L'imperfection doit être un nombre");
                }
            }
        }

        System.out.println("Génération du labyrinthe... (seed=" + seed + ")");
        Random random = new Random(seed);

        // Crée un labyrinthe vide
        Maze maze = new Maze(WIDTH, HEIGHT);
        
        // Applique la structure de base (murs extérieurs, maison des fantômes)
        maze.applyTemplate();

        // Instancie un générateur et lance l'algorithme sur le labyrinthe
        MazeGenerator generator = new MazeGenerator();
        generator.generate(maze, random, imperfection);

        // Création du JSON
        String mazeAsJsonString = maze.toJsonString();

        // Lecture du JSON et affichage graphique
        Gson gson = new Gson();
        MazeData dataFromJSON = gson.fromJson(mazeAsJsonString, MazeData.class);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Représentation Graphique du JSON");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new MazeVisualizerPanel(dataFromJSON.grid()));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

         // Affiche le JSON dans la console
        System.out.println(mazeAsJsonString);
    }
}
