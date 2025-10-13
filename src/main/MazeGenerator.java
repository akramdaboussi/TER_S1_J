package main;

import java.util.*;

/**
 * Utilise un algorithme de Kruskal modifié pour garantir une symétrie horizontale
 * et générer des chemins aléatoires avec des imperfections contrôlables.
 */
public class MazeGenerator {

    // --- Configuration ---
    private static final int WIDTH = 28;
    private static final int HEIGHT = 31;
    private static double IMPERFECTION_PERCENTAGE = 0.2; // 20%

    // --- Définition des états des cellules ---
    enum CellState {
        MUR,
        SOL,
        MUR_PERMANENT,
        GHOST_HOUSE,
        TUNNEL
    }

    // --- Représentation immuable d'un point ---
    static record Point(int x, int y) {}

    // --- Représentation immuable d'une paire de murs symétriques ---
    static record WallPair(Point wall, Point symWall, Point cell1, Point cell2, Point symCell1, Point symCell2) {}

    // --- Classe principale du Labyrinthe ---
    static class Maze {
        private final int width;
        private final int height;
        private final CellState[][] grid;
        private final Random random;

        public Maze(int width, int height, Random random) {
            if (width % 2 != 0) {
                throw new IllegalArgumentException("La largeur doit être paire pour une symétrie parfaite.");
            }
            this.width = width;
            this.height = height;
            this.random = random;
            this.grid = new CellState[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    grid[y][x] = CellState.MUR;
                }
            }
        }

        // Applique un template de base pour avoir un petit air de Pac-Man 
        private void applyTemplate() { 
            // Murs permanents sur les contours
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (y == 0 || y == height - 1 || x == 0 || x == width - 1) {
                        grid[y][x] = CellState.MUR_PERMANENT;
                    }
                }
            }
            // Ghost House
            int ghXStart = width / 2 - 4;
            int ghYStart = height / 2 - 2;
            for (int y = ghYStart; y < ghYStart + 5; y++) {
                for (int x = ghXStart; x < ghXStart + 8; x++) {
                    grid[y][x] = CellState.GHOST_HOUSE;
                    if (y == ghYStart || y == ghYStart + 4 || x == ghXStart || x == ghXStart + 7) {
                        grid[y][x] = CellState.MUR_PERMANENT;
                    }
                }
            }
            grid[ghYStart][width / 2 - 1] = CellState.SOL; // Porte
            grid[ghYStart][width / 2] = CellState.SOL;

            // Tunnels
            int tunnelY = height / 2;
            if (tunnelY % 2 != 0) tunnelY++;
            grid[tunnelY][0] = CellState.TUNNEL;
            grid[tunnelY][width - 1] = CellState.TUNNEL;
            grid[tunnelY][1] = CellState.SOL;
            grid[tunnelY][width - 2] = CellState.SOL;
        }

        public void generate(double imperfection) {
            applyTemplate();
            Map<Point, Integer> sets = new HashMap<>();
            int setCounter = 0;

            // On initialise les "salles" (coordonnées paires)
            for (int y = 2; y < height - 2; y += 2) {
                for (int x = 2; x < width - 2; x += 2) {
                    if (grid[y][x] == CellState.MUR) {
                        sets.put(new Point(x, y), setCounter++);
                    }
                }
            }
            
            // On crée la liste des paires de murs symétriques
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

            Collections.shuffle(wallPairs, this.random);
            List<WallPair> remainingWallPairs = new ArrayList<>();

            // On applique Kruskal symétrique
            for (WallPair pair : wallPairs) {
                Integer set1 = sets.get(pair.cell1);
                Integer set2 = sets.get(pair.cell2);

                if (set1 != null && set2 != null && !set1.equals(set2)) {
                    // Creuser le passage et son symétrique
                    grid[pair.wall.y][pair.wall.x] = CellState.SOL;
                    grid[pair.symWall.y][pair.symWall.x] = CellState.SOL;
                    grid[pair.cell1.y][pair.cell1.x] = CellState.SOL;
                    grid[pair.cell2.y][pair.cell2.x] = CellState.SOL;
                    grid[pair.symCell1.y][pair.symCell1.x] = CellState.SOL;
                    grid[pair.symCell2.y][pair.symCell2.x] = CellState.SOL;

                    // Fusionner les ensembles
                    int oldSet = set2;
                    sets.replaceAll((p, v) -> v.equals(oldSet) ? set1 : v);
                    
                    Integer symSet1 = sets.get(pair.symCell1);
                    Integer symSet2 = sets.get(pair.symCell2);
                    if(symSet1 != null && symSet2 != null) {
                        int oldSymSet = symSet2;
                        sets.replaceAll((p, v) -> v.equals(oldSymSet) ? symSet1 : v);
                    }
                } else {
                    remainingWallPairs.add(pair);
                }
            }

            // On ajoute des imperfections simples pour créer un labyrinthe moins parfait
            int numToRemove = (int) (remainingWallPairs.size() * imperfection);
            Collections.shuffle(remainingWallPairs, this.random);
            for (int i = 0; i < numToRemove; i++) {
                WallPair pair = remainingWallPairs.get(i);
                if (grid[pair.wall.y][pair.wall.x] == CellState.MUR) grid[pair.wall.y][pair.wall.x] = CellState.SOL;
                if (grid[pair.symWall.y][pair.symWall.x] == CellState.MUR) grid[pair.symWall.y][pair.symWall.x] = CellState.SOL;
            }
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    switch (grid[y][x]) {
                        case MUR: case MUR_PERMANENT: sb.append("██"); break;
                        case SOL: case GHOST_HOUSE: case TUNNEL: sb.append("  "); break;
                        default: sb.append("??"); break;
                    }
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    // --- Exécution du script ---
    public static void main(String[] args) {
        long seed = System.currentTimeMillis();
        double imperfection = IMPERFECTION_PERCENTAGE;

        // Parse les arguments de la ligne de commande manuellement
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
                    System.err.println("Erreur: L'imperfection doit être un nombre (ex: 0.2).");
                }
            }
        }

        System.out.println("Génération du labyrinthe ... (seed=" + seed + ")");
        Random random = new Random(seed);
        Maze maze = new Maze(WIDTH, HEIGHT, random);
        maze.generate(imperfection);
        System.out.println("Labyrinthe généré :\n");
        System.out.println(maze);
    }
}