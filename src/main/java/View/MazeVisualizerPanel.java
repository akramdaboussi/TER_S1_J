package View;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.Map;

public class MazeVisualizerPanel extends JPanel {

    private final int[][] mazeGrid;
    private static final int CELL_SIZE = 24; // Taille d'une case en pixels

    // Couleur Retro
    private static final Color COLOR_BG = new Color(10, 10, 18);        // Fond Noir Bleuté
    private static final Color COLOR_WALL_OUTLINE = new Color(33, 33, 222); // Bleu Arcade (Contour)
    private static final Color COLOR_PELLET = new Color(250, 180, 180); // Rose Pâle
    private static final Color COLOR_TEXT = new Color(222, 222, 255);   // Blanc bleuté

    // Données du jeu 
    private Map<String, Integer> pacPosData;
    private Map<String, Integer> blinkyPosData;
    private Map<String, Integer> pinkyPosData;
    private Map<String, Integer> inkyPosData;
    private Map<String, Integer> clydePosData;
    private boolean[][] smallPelletsData;
    private boolean[][] powerPelletsData;
    private boolean isFrightened = false;

    // Données HUD
    private int currentScore = 0;
    private int currentLives = 3;
    private String currentMode = "WAITING";

    public MazeVisualizerPanel(int[][] grid) {
        this.mazeGrid = grid;
        int panelWidth = grid[0].length * CELL_SIZE;
        int panelHeight = grid.length * CELL_SIZE;

        // Espace pour le HUD en bas
        setPreferredSize(new Dimension(panelWidth, panelHeight + 40));
        setBackground(COLOR_BG);
        setFocusable(true);
        requestFocusInWindow();
    }

    // Méthode pour mettre à jour l'état du jeu à partir des données du Cloud
    public void updateGameState(Map<String, Integer> pac, Map<String, Integer> blinky, Map<String, Integer> pinky, 
        Map<String, Integer> inky, Map<String, Integer> clyde, boolean[][] smallPellets, boolean[][] powerPellets, boolean frightened,
        int score, int lives, String mode) {
        this.pacPosData = pac;
        this.blinkyPosData = blinky;
        this.pinkyPosData = pinky;
        this.inkyPosData = inky;
        this.clydePosData = clyde;
        this.smallPelletsData = smallPellets;
        this.powerPelletsData = powerPellets;
        this.isFrightened = frightened;
        this.currentScore = score;
        this.currentLives = lives;
        this.currentMode = mode;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Fond Rétro
        g2d.setColor(COLOR_BG);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Murs creux 
        drawArcadeWalls(g2d);

        // Éléments de jeu
        if (pacPosData != null) {
            drawPellets(g2d);
            drawPacman(g2d);
            drawGhosts(g2d);
            
            // HUD Rétro complet
            drawRetroHUD(g2d);
        }
    }

    // murs creux
    private void drawArcadeWalls(Graphics2D g2d) {
        // Le contour (Gros trait bleu)
        drawMazePath(g2d, COLOR_WALL_OUTLINE, CELL_SIZE - 4);

        // L'intérieur (Trait moyen noir pour creuser)
        drawMazePath(g2d, COLOR_BG, CELL_SIZE - 10);
    }

    private void drawMazePath(Graphics2D g2d, Color color, float thickness) {
        int halfCell = CELL_SIZE / 2;
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int y = 0; y < mazeGrid.length; y++) {
            for (int x = 0; x < mazeGrid[y].length; x++) {
                if (est_mur(y, x)) {
                    int cx = x * CELL_SIZE + halfCell;
                    int cy = y * CELL_SIZE + halfCell;

                    if (!est_mur(y, x+1) && !est_mur(y, x-1) && !est_mur(y+1, x) && !est_mur(y-1, x)) {
                         g2d.drawLine(cx, cy, cx, cy);
                    }
                    
                    if (est_mur(y, x + 1)) g2d.draw(new Line2D.Float(cx, cy, cx + CELL_SIZE, cy));
                    if (est_mur(y + 1, x)) g2d.draw(new Line2D.Float(cx, cy, cx, cy + CELL_SIZE));
                }
            }
        }
    }


    // pacman 
    private void drawPacman(Graphics2D g) {
        int px = pacPosData.get("x") * CELL_SIZE;
        int py = pacPosData.get("y") * CELL_SIZE;
        g.setColor(Color.YELLOW);
        g.fillOval(px + 3, py + 3, CELL_SIZE - 6, CELL_SIZE - 6);
    }

    // pellets
    private void drawPellets(Graphics2D g) {
        g.setColor(COLOR_PELLET);
        for (int y = 0; y < smallPelletsData.length; y++) {
            for (int x = 0; x < smallPelletsData[0].length; x++) {
                int cx = x * CELL_SIZE + CELL_SIZE / 2;
                int cy = y * CELL_SIZE + CELL_SIZE / 2;

                if (smallPelletsData[y][x]) {
                    g.fillRect(cx - 2, cy - 2, 4, 4); 
                }
                if (powerPelletsData[y][x]) {
                    g.setColor(Color.WHITE);
                    g.fillOval(cx - 7, cy - 7, 14, 14);
                    g.setColor(COLOR_PELLET); 
                }
            }
        }
    }

    //ghosts 
    private void drawGhosts(Graphics2D g) {
        int gx = blinkyPosData.get("x") * CELL_SIZE;
        int gy = blinkyPosData.get("y") * CELL_SIZE;
        int px = pinkyPosData.get("x") * CELL_SIZE;
        int py = pinkyPosData.get("y") * CELL_SIZE;
        int ix = inkyPosData.get("x") * CELL_SIZE;
        int iy = inkyPosData.get("y") * CELL_SIZE;
        int cx = clydePosData.get("x") * CELL_SIZE;
        int cy = clydePosData.get("y") * CELL_SIZE;

        // ---- Blinky (rouge ou bleu si frightened)
        g.setColor(isFrightened ? Color.BLUE : Color.RED);
        g.fillOval(gx + 3, gy + 3, CELL_SIZE - 6, CELL_SIZE - 6);

        // ---- Pinky (rose ou bleu si frightened)
        Color pink = new Color(255, 100, 180); // rose Pac-Man
        g.setColor(isFrightened ? Color.BLUE : pink);
        g.fillOval(px + 3, py + 3, CELL_SIZE - 6, CELL_SIZE - 6);

        // ---- Inky (cyan ou bleu si frightened)
        Color inkyBlue = new Color(0, 255, 255); // cyan
        g.setColor(isFrightened ? Color.BLUE : inkyBlue);
        g.fillOval(ix + 3, iy + 3, CELL_SIZE - 6, CELL_SIZE - 6);

        // ---- Clyde (orange ou bleu si frightened)
        Color orange = new Color(255, 165, 0); // orange
        g.setColor(isFrightened ? Color.BLUE : orange);
        g.fillOval(cx + 3, cy + 3, CELL_SIZE - 6, CELL_SIZE - 6);
    }

    // HUD rétro avec score, vies, mode
    private void drawRetroHUD(Graphics2D g) {
        int yPos = getHeight() - 12;
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.setColor(COLOR_TEXT);
        
        // Mode à gauche
        g.drawString(currentMode, 10, yPos);
        
        // Score au centre
        String scoreText = "SCORE: " + currentScore;
        int wScore = g.getFontMetrics().stringWidth(scoreText);
        g.drawString(scoreText, getWidth()/2 - wScore/2, yPos);
        
        // Vies à droite
        String livesText = "VIES: " + currentLives;
        int wLives = g.getFontMetrics().stringWidth(livesText);
        g.drawString(livesText, getWidth() - wLives - 10, yPos);
        
        // Ligne de séparation bleue
        g.setColor(COLOR_WALL_OUTLINE);
        g.setStroke(new BasicStroke(2));
        g.drawLine(0, getHeight() - 40, getWidth(), getHeight() - 40);
    }

    private boolean est_mur(int y, int x) {
        if (x < 0 || x >= mazeGrid[0].length || y < 0 || y >= mazeGrid.length) {
            return false;
        }
        int v = mazeGrid[y][x];
        return v == 1 || v == 2; // 1 = mur, 2 = mur permanent
    }
}
