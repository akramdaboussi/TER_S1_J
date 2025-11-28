package Model;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.Gson;

/*
 * Représente la grille du labyrinthe et son état.
 * Cette classe contient les données du labyrinthe.
*/

public class Maze {
    private final int width;
    private final int height;
    private final CellState[][] grid;
    private final String ident;

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
        this.ident = UUID.randomUUID().toString();
        // Initialise toute la grille avec des murs
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = CellState.MUR;
            }
        }
    }

    /* 
     * Constructeur pour reconstruction depuis les données JSON
    */
    public Maze(MazeData data) {
        this.width = data.width();
        this.height = data.height();
        this.ident = data.ident();
        this.grid = new CellState[height][width];
        
        // Traduire la grille simple (int) en CellState
        for (int y = 0; y < height; y++) {
            List<Integer> row = data.grid().get(y);
            for (int x = 0; x < width; x++) {
                switch (row.get(x)) {
                    case 0: this.grid[y][x] = CellState.SOL; break;
                    case 1: this.grid[y][x] = CellState.MUR; break;
                    case 2: this.grid[y][x] = CellState.MUR_PERMANENT; break;
                    case 3: this.grid[y][x] = CellState.GHOST_HOUSE; break;
                    case 4: this.grid[y][x] = CellState.TUNNEL; break;
                    default: 
                        this.grid[y][x] = CellState.MUR; 
                }
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
                if (y <= 1 || y >= height - 2 || x <= 1 || x >= width - 2) {
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

        for (int yClear = ghYStart - 1; yClear <= ghYStart + 5; yClear++) {
            for (int xClear = ghXStart - 1; xClear <= ghXStart + 8; xClear++) {
                // On s'assure que l'on n'est pas sur la bordure permanente du maze
                if (xClear > 1 && xClear < width - 2 && yClear > 1 && yClear < height - 2) {
                    CellState current = grid[yClear][xClear];
                    // Si la cellule est un MUR simple
                    if (current == CellState.MUR) {
                        // On la transforme en SOL
                        grid[yClear][xClear] = CellState.SOL;
                    }
                }
            }
        }

        // Tunnels sur les côtés
        int tunnelY = height / 2;
        if (tunnelY % 2 != 0) tunnelY++; // S'assure que le tunnel est sur une ligne paire si besoin
        for (int x = 2; x < width - 2; x++) {
            if (grid[tunnelY][x] != CellState.MUR_PERMANENT && grid[tunnelY][x] != CellState.GHOST_HOUSE) {
                grid[tunnelY][x] = CellState.SOL;
            }
        }
        int x_in = 2; // La cellule SOL à l'intérieur du 2e contour à gauche.
        int x_out = width - 3; // La cellule SOL à l'intérieur du 2e contour à droite.

        grid[tunnelY - 1][x_in] = CellState.SOL;
        grid[tunnelY + 1][x_in] = CellState.SOL;
        grid[tunnelY - 1][x_out] = CellState.SOL;
        grid[tunnelY + 1][x_out] = CellState.SOL;
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

    public MazeData getMazeData() {
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

        List<List<Integer>> simpleGridList = Arrays.stream(simpleGrid)
            .map(row -> Arrays.stream(row).boxed().collect(Collectors.toList()))
            .collect(Collectors.toList());
        // Crée l'objet de transfert JSON avec l'ID
        return new MazeData(this.ident, this.width, this.height, simpleGridList);
    }

    public String toJsonString(){
        Gson gson = new Gson();
        return gson.toJson(getMazeData());
    }
}
