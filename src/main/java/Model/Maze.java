package Model;

import com.google.gson.Gson;

/*
 * Représente la grille du labyrinthe et son état.
 * Cette classe contient les données du labyrinthe.
*/

public class Maze {
    private final int width;
    private final int height;
    private final CellState[][] grid;

    /*
     * Construit un labyrinthe vide de dimensions données, rempli de murs.
     * @param width La largeur de la grille.
     * @param height La hauteur de la grille.
    */

    public Maze(int width, int height) {
        if (width % 2 != 0) {
            throw new IllegalArgumentException("La largeur doit être paire pour une symétrie parfaite.");
        }
        this.width = width;
        this.height = height;
        this.grid = new CellState[height][width];
        // Initialise toute la grille avec des murs
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = CellState.MUR;
            }
        }
    }

    /*
     * Applique la structure fixe du labyrinthe (contours, Ghost house, tunnels) (début de construction de la structure fixe pacman)
    */
    public void applyTemplate() {
        // Murs permanents sur les contours
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y == 0 || y == height - 1 || x == 0 || x == width - 1) {
                    grid[y][x] = CellState.MUR_PERMANENT;
                }
            }
        }
        // Ghost house au centre
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
        grid[ghYStart][width / 2 - 1] = CellState.SOL; 
        grid[ghYStart][width / 2] = CellState.SOL;

        // Tunnels sur les côtés
        int tunnelY = height / 2;
        if (tunnelY % 2 != 0) tunnelY++; // S'assure que le tunnel est sur une ligne paire si besoin
        grid[tunnelY][0] = CellState.TUNNEL;
        grid[tunnelY][width - 1] = CellState.TUNNEL;
        grid[tunnelY][1] = CellState.SOL;
        grid[tunnelY][width - 2] = CellState.SOL;
    }

    // Accesseurs (Getters/Setters) pour permettre la modification par le générateur
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public CellState getState(int x, int y) { return grid[y][x]; }
    public void setState(Point p, CellState state) { grid[p.y()][p.x()] = state; }

    public String toJsonString() {
        // Traduire la grille complexe (CellState[][]) en une grille simple (int[][])
        int[][] simpleGrid = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                switch (grid[y][x]) {
                    case SOL:           simpleGrid[y][x] = 0; break;
                    case MUR:           simpleGrid[y][x] = 1; break;
                    case MUR_PERMANENT: simpleGrid[y][x] = 2; break;
                    case GHOST_HOUSE:   simpleGrid[y][x] = 3; break;
                    case TUNNEL:        simpleGrid[y][x] = 4; break;
                    default:            simpleGrid[y][x] = -1; break; // Pour repérer les erreurs
                }
            }
        }
        // Créer un objet qui représente la structure finale du JSON
        MazeData dataForJson = new MazeData(this.width, this.height, simpleGrid);

        // Utiliser la bibliothèque Gson pour créer la chaîne de caractères
        Gson gson = new Gson();
        return gson.toJson(dataForJson);
    }
}
