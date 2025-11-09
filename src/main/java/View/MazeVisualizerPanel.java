package View;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.Line2D;

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

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Color wallColor = new Color(0, 0, 200); // Bleu Pacman
        int halfCell = CELL_SIZE / 2;
        
        // Crée un style de trait avec des extrémités et des jointures arrondies
        Stroke roundedWallStroke = new BasicStroke(
            CELL_SIZE, 
            BasicStroke.CAP_ROUND,  // Extrémités de ligne arrondies 
            BasicStroke.JOIN_ROUND // Jointures arrondies
        );

        // Étape 1: Remplir le fond en noir
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(wallColor);
        g2d.setStroke(roundedWallStroke);

        // Étape 2: Dessiner les murs avec des segments arrondis
        for (int y = 0; y < mazeGrid.length; y++) {
            for (int x = 0; x < mazeGrid[y].length; x++) {
                if (est_mur(y, x)) {
                    int centerX = x * CELL_SIZE + halfCell;
                    int centerY = y * CELL_SIZE + halfCell;
                    
                    // Dessiner une ligne vers le bas si le voisin du bas n'est pas un mur
                    if (!est_mur(y + 1, x)) {
                        // Dessiner une courte ligne centrée verticalement pour la case seule
                        g2d.draw(new Line2D.Float(centerX, centerY, centerX, centerY));
                    }

                    // --- Dessin des segments de mur ---
                    
                    // Ligne horizontale vers la droite (pour couvrir les segments de mur horizontal)
                    if (est_mur(y, x + 1)) {
                        // Dessine la ligne du centre de la cellule actuelle au centre de la suivante
                        g2d.draw(new Line2D.Float(centerX, centerY, centerX + CELL_SIZE, centerY));
                    }

                    // Ligne verticale vers le bas (pour couvrir les segments de mur vertical)
                    if (est_mur(y + 1, x)) {
                        // Dessine la ligne du centre de la cellule actuelle au centre de la suivante
                        g2d.draw(new Line2D.Float(centerX, centerY, centerX, centerY + CELL_SIZE));
                    }
                }
            }
        }
    }

    private boolean est_mur(int y, int x) {
        if (x < 0 || x >= mazeGrid[0].length || y < 0 || y >= mazeGrid.length) {
            return false;
        }
        int value = mazeGrid[y][x];
        return value == 1 || value == 2;
    }
}