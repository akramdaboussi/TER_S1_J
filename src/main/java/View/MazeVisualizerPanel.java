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
        Graphics2D g2d = (Graphics2D) g;
        
        // Active l'anti-aliasing pour des bords plus lisses
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Taille des coins arrondis pour les murs
        int arcSize = CELL_SIZE / 2;

        for (int y = 0; y < mazeGrid.length; y++) {
            for (int x = 0; x < mazeGrid[y].length; x++) { 
                int cellX = x * CELL_SIZE;
                int cellY = y * CELL_SIZE;
                
                switch (mazeGrid[y][x]) {
                    case 0: 
                    case 3: // Ghost House
                    case 4: // Tunnel
                        g2d.setColor(Color.BLACK); 
                        g2d.fillRect(cellX, cellY, CELL_SIZE, CELL_SIZE);
                        break;

                    case 1: // Mur
                    case 2: // Mur permanent
                        g2d.setColor(new Color(0,0,200)); 
                        // Dessine des murs avec des coins arrondis (pour un rendu plus Pac-Man)
                        g2d.fillRoundRect(cellX, cellY, CELL_SIZE, CELL_SIZE, arcSize, arcSize);
                        break;
                        
                    default: 
                        g2d.setColor(Color.RED); 
                        g2d.fillRect(cellX, cellY, CELL_SIZE, CELL_SIZE);
                        break;
                }
            }
        }
    }
}