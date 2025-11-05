package Generator;

import java.util.*;
import Model.*;

/*
 * Contient la logique de génération du labyrinthe.
 * Utilise un algorithme de Kruskal modifié pour garantir une symétrie horizontale
 * et générer des chemins aléatoires avec des imperfections contrôlables.
*/
public class MazeGenerator {

    /*
     * Génère la structure du labyrinthe en modifiant l'objet Maze fourni.
     * @param maze L'objet Maze à modifier.
     * @param random L'instance de Random pour le caractère aléatoire.
     * @param imperfection Le pourcentage de murs supplémentaires à retirer (0.0 à 1.0).
    */
    public void generate(Maze maze, Random random, double imperfection) {
        int width = maze.getWidth();
        int height = maze.getHeight();
        Map<Point, Integer> sets = new HashMap<>();
        int setCounter = 0;

        // Initialise les "ensembles" pour chaque "salle" potentielle.
        // Chaque salle est une cellule isolée au début.
        for (int y = 2; y < height - 2; y += 2) {
            for (int x = 2; x < width - 2; x += 2) {
                if (maze.getState(x, y) == CellState.MUR) {
                    sets.put(new Point(x, y), setCounter++);
                }
            }
        }

        // Crée une liste de tous les murs destructibles et de leurs symétriques.
        List<WallPair> wallPairs = createSymmetricWallPairs(width, height);
        Collections.shuffle(wallPairs, random); // Mélange la liste pour un ordre aléatoire.
        List<WallPair> remainingWallPairs = new ArrayList<>();

        // Applique l'algorithme de Kruskal symétrique.
        for (WallPair pair : wallPairs) {
            Integer set1 = sets.get(pair.cell1());
            Integer set2 = sets.get(pair.cell2());

            // Si les deux cellules adjacentes ne sont pas déjà connectées...
            if (set1 != null && set2 != null && !set1.equals(set2)) {
                // ... on casse le mur (et son symétrique).
                carvePassage(maze, pair);

                // ... et on fusionne leurs ensembles pour marquer qu'elles sont maintenant connectées.
                int oldSet = set2;
                sets.replaceAll((p, v) -> v.equals(oldSet) ? set1 : v);
                
                // On fait de même pour la partie symétrique.
                Integer symSet1 = sets.get(pair.symCell1());
                Integer symSet2 = sets.get(pair.symCell2());
                if (symSet1 != null && symSet2 != null) {
                    int oldSymSet = symSet2;
                    sets.replaceAll((p, v) -> v.equals(oldSymSet) ? symSet1 : v);
                }
            } else {
                // Si les cellules étaient déjà connectées, on garde le mur pour plus tard.
                remainingWallPairs.add(pair);
            }
        }

        // Ajoute des imperfections en cassant des murs supplémentaires.
        addImperfections(maze, remainingWallPairs, random, imperfection);
    }

    /*
     * Modifie l'état des cellules du labyrinthe pour créer un passage.
    */
    private void carvePassage(Maze maze, WallPair pair) {
        maze.setState(pair.wall(), CellState.SOL);
        maze.setState(pair.symWall(), CellState.SOL);
        maze.setState(pair.cell1(), CellState.SOL);
        maze.setState(pair.cell2(), CellState.SOL);
        maze.setState(pair.symCell1(), CellState.SOL);
        maze.setState(pair.symCell2(), CellState.SOL);
    }

    /*
     * Ajoute des boucles en retirant un certain pourcentage des murs restants.
    */
    private void addImperfections(Maze maze, List<WallPair> remainingWalls, Random random, double imperfection) {
        int numToRemove = (int) (remainingWalls.size() * imperfection);
        Collections.shuffle(remainingWalls, random);
        for (int i = 0; i < numToRemove; i++) {
            WallPair pair = remainingWalls.get(i);
            if (maze.getState(pair.wall().x(), pair.wall().y()) == CellState.MUR) {
                maze.setState(pair.wall(), CellState.SOL);
            }
            if (maze.getState(pair.symWall().x(), pair.symWall().y()) == CellState.MUR) {
                maze.setState(pair.symWall(), CellState.SOL);
            }
        }
    }

    /*
     * Construit la liste de toutes les paires de murs symétriques potentiels.
     * Ne parcourt que la moitié gauche de la grille pour garantir la symétrie.
    */
    private List<WallPair> createSymmetricWallPairs(int width, int height) {
        List<WallPair> wallPairs = new ArrayList<>();
        for (int y = 2; y < height - 2; y += 2) {
            for (int x = 2; x < width / 2; x += 2) { // Moitié gauche seulement
                // Mur vers la droite
                if (x + 2 < width) {
                    wallPairs.add(new WallPair(
                        new Point(x + 1, y), new Point(width - 2 - x, y),
                        new Point(x, y), new Point(x + 2, y),
                        new Point(width - 1 - x, y), new Point(width - 3 - x, y)
                    ));
                }
                // Mur vers le bas
                if (y + 2 < height) {
                    wallPairs.add(new WallPair(
                        new Point(x, y + 1), new Point(width - 1 - x, y + 1),
                        new Point(x, y), new Point(x, y + 2),
                        new Point(width - 1 - x, y), new Point(width - 1 - x, y + 2)
                    ));
                }
            }
        }
        return wallPairs;
    }
}