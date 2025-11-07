package View;

import javax.swing.JPanel;
import java.awt.*;


public class MazeVisualizerPanel extends JPanel {

    private final int[][] mazeGrid;
    private static final int CELL_SIZE = 20; // La taille d'un carré en pixels

    public MazeVisualizerPanel(int[][] grid) {
        this.mazeGrid = grid;
        int panelWidth = grid[0].length * CELL_SIZE;
        int panelHeight = grid.length * CELL_SIZE;
        setPreferredSize(new Dimension(panelWidth, panelHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int y = 0; y < mazeGrid.length; y++) {
            for (int x = 0; x < mazeGrid[y].length; x++) {
                // On choisit la couleur en fonction du chiffre dans la grille
                switch (mazeGrid[y][x]) {
                    case 0: g.setColor(Color.BLACK); break; // SOL
                    case 1: g.setColor(new Color(0,0,200));; break; // MUR
                    case 2: g.setColor(new Color(0, 0, 200)); break; // MUR_PERMANENT
                    case 3: g.setColor(Color.BLACK); break; // GHOST_HOUSE
                    case 4: g.setColor(Color.BLACK); break; // TUNNEL
                    default: g.setColor(Color.RED); break;  // Erreur
                }
                // On dessine le carré
                g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }
}