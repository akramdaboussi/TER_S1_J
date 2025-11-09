package Generator;

import java.util.*;
import Model.*;

/*
 * Contient la logique de génération du labyrinthe.
 * Utilise un algorithme de Kruskal modifié pour garantir une symétrie horizontale
 * et générer des chemins aléatoires tout en évitant les culs-de-sac.
*/
public class MazeGenerator {

    /*
     * Génère la structure du labyrinthe en modifiant l'objet Maze fourni.
     * @param maze L'objet Maze à modifier.
     * @param random L'instance de Random pour le caractère aléatoire.
    */
    public void generate(Maze maze, Random random) {
        int width = maze.getWidth();
        int height = maze.getHeight();
        Map<Point, Integer> sets = new HashMap<>();
        int setCounter = 0;

        // Initialise les "ensembles" pour chaque "salle" potentielle.
        // Chaque salle est une cellule isolée au début.
        for (int y = 2; y < height - 2; y += 2) {
            for (int x = 2; x < width - 2; x += 2) {
                // S'assure que l'initialisation commence après le 2e contour (y=2, x=2).
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

        // Suppression des culs-de-sac pour créer des boucles supplémentaires
        removeDeadEnds(maze, random);
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
     * Supprimme les culs-de-sac en cassant des murs de manière symétrique
    */
    private void removeDeadEnds(Maze maze, Random random) {
        int width = maze.getWidth();
        int height = maze.getHeight();
        boolean deadEndRemoved;
        
        // Directions (dx, dy) pour les 4 voisins
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        do {
            deadEndRemoved = false;
            for (int y = 2; y < height - 1; y++) {
                for (int x = 2; x < width / 2; x++) { // Parcourt seulement la moitié gauche
                    if (maze.getState(x, y) != CellState.SOL) {
                        continue;
                    }
                    // On détecte le cul-de-sac : cellule SOL n'ayant qu'un seul voisin SOL
                    int solNeighbors = 0;
                    for (int[] dir : directions) {
                        int nx = x + dir[0];
                        int ny = y + dir[1];
                        // Vérifie que la cellule voisine est dans les limites et est du SOL
                        if (nx > 0 && nx < width - 1 && ny > 0 && ny < height - 1 && maze.getState(nx, ny) == CellState.SOL) {
                            solNeighbors++;
                        }
                    }

                    if (solNeighbors == 1) {
                        // On trouve un mur cassable (MUR simple avec symétrique MUR simple)
                        List<Point> breakableWalls = new ArrayList<>();
                        for (int[] dir : directions) {
                            Point wall = new Point(x + dir[0], y + dir[1]);
                            int symX = width - 1 - wall.x();
                            // On vérifie que le mur est un MUR ET que son symétrique est aussi un MUR
                            if (maze.getState(wall.x(), wall.y()) == CellState.MUR && maze.getState(symX, wall.y()) == CellState.MUR) {
                                 breakableWalls.add(wall);
                            }
                        }

                        // On casse s'il y a un choix
                        if (!breakableWalls.isEmpty()) {
                            Collections.shuffle(breakableWalls, random); 
                            Point wallToBreak = breakableWalls.get(0);
                            int symX = width - 1 - wallToBreak.x();

                            // On casse le mur et son symétrique
                            maze.setState(wallToBreak, CellState.SOL);
                            maze.setState(new Point(symX, wallToBreak.y()), CellState.SOL);
                            deadEndRemoved = true;
                            break; 
                        } 
                    }
                }
                if (deadEndRemoved) break; 
            }
        } while (deadEndRemoved); 
    }

    /*
     * Construit la liste de toutes les paires de murs symétriques potentiels.
     * Ne parcourt que la moitié gauche de la grille pour garantir la symétrie.
    */
    private List<WallPair> createSymmetricWallPairs(int width, int height) {
        List<WallPair> wallPairs = new ArrayList<>();
        for (int y = 2; y < height - 2; y += 2) {
            for (int x = 2; x < width / 2; x += 2) { // Moitié gauche seulement
                // Mur vers la droite (horizontal)
                if (x + 2 < width) {
                    wallPairs.add(new WallPair(
                        new Point(x + 1, y), new Point(width - 2 - x, y),
                        new Point(x, y), new Point(x + 2, y),
                        new Point(width - 1 - x, y), new Point(width - 3 - x, y)
                    ));
                }
                // Mur vers le bas (vertical)
                if (y + 2 < height - 2) {
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