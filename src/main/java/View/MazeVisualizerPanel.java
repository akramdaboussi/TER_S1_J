package View;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.Map;

public class MazeVisualizerPanel extends JPanel {

    private final int[][] mazeGrid;
    private static final int CELL_SIZE = 20; // Taille d'une case en pixels

    private Map<String, Integer> pacPosData;
    private Map<String, Integer> blinkyPosData;
    private Map<String, Integer> pinkyPosData;
    private Map<String, Integer> inkyPosData;
    private boolean[][] smallPelletsData;
    private boolean[][] powerPelletsData;
    private boolean isFrightened = false;

    public MazeVisualizerPanel(int[][] grid) {
        this.mazeGrid = grid;
        int panelWidth = grid[0].length * CELL_SIZE;
        int panelHeight = grid.length * CELL_SIZE;
        setPreferredSize(new Dimension(panelWidth, panelHeight));

        setFocusable(true);
        requestFocusInWindow();
    }

    // Méthode pour mettre à jour l'état du jeu à partir des données du Cloud
    public void setCloudGameData(Map<String, Integer> pac, Map<String, Integer> blinky, Map<String, Integer> pinky, Map<String, Integer> inky,
                                 boolean[][] smallPellets, boolean[][] powerPellets, boolean frightened) {
        this.pacPosData = pac;
        this.blinkyPosData = blinky;
        this.pinkyPosData = pinky;
        this.inkyPosData = inky;
        this.smallPelletsData = smallPellets;
        this.powerPelletsData = powerPellets;
        this.isFrightened = frightened;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Color wallColor = new Color(0, 0, 200); // Bleu pacman
        int halfCell = CELL_SIZE / 2;

        Stroke roundedWallStroke = new BasicStroke(
                CELL_SIZE,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        );

        //fond
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        //murs
        g2d.setColor(wallColor);
        g2d.setStroke(roundedWallStroke);

        for (int y = 0; y < mazeGrid.length; y++) {
            for (int x = 0; x < mazeGrid[y].length; x++) {

                if (est_mur(y, x)) {
                    int cx = x * CELL_SIZE + halfCell;
                    int cy = y * CELL_SIZE + halfCell;

                    // mur d'une case
                    if (!est_mur(y + 1, x)) {
                        g2d.draw(new Line2D.Float(cx, cy, cx, cy));
                    }

                    // Segment horizontal
                    if (est_mur(y, x + 1)) {
                        g2d.draw(new Line2D.Float(cx, cy, cx + CELL_SIZE, cy));
                    }

                    // Segment vertical
                    if (est_mur(y + 1, x)) {
                        g2d.draw(new Line2D.Float(cx, cy, cx, cy + CELL_SIZE));
                    }
                }
            }
        }

        // elements du jeu
        if (pacPosData != null) {
            drawPellets(g2d);
            drawPacman(g2d);
            drawGhosts(g2d);
        }
    }

    private boolean est_mur(int y, int x) {
        if (x < 0 || x >= mazeGrid[0].length || y < 0 || y >= mazeGrid.length) {
            return false;
        }
        int v = mazeGrid[y][x];
        return v == 1 || v == 2; // 1 = mur, 2 = mur permanent
    }

    // pacman 
    private void drawPacman(Graphics2D g) {
        int px = pacPosData.get("x") * CELL_SIZE;
        int py = pacPosData.get("y") * CELL_SIZE;

        g.setColor(Color.YELLOW);
        g.fillOval(px, py, CELL_SIZE, CELL_SIZE);
    }

    // pellets
    private void drawPellets(Graphics2D g) {
        boolean[][] small = smallPelletsData;
        boolean[][] power = powerPelletsData;

        g.setColor(Color.WHITE);
        int r = CELL_SIZE / 4;

        for (int y = 0; y < small.length; y++) {
            for (int x = 0; x < small[0].length; x++) {

                // Small pellet
                if (small[y][x]) {
                    int px = x * CELL_SIZE + CELL_SIZE/2 - r/2;
                    int py = y * CELL_SIZE + CELL_SIZE/2 - r/2;
                    g.fillOval(px, py, r, r);
                }

                // Power pellet (plus grosse)
                if (power[y][x]) {
                    int power_d = r * 4;
                    int power_offset = r * 2;
                    int px = x * CELL_SIZE + CELL_SIZE/2 - power_offset;
                    int py = y * CELL_SIZE + CELL_SIZE/2 - power_offset;
                    g.fillOval(px, py, power_d, power_d);
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

    // ---- Blinky (rouge ou bleu si frightened)
    g.setColor(isFrightened ? Color.BLUE : Color.RED);
    g.fillOval(gx, gy, CELL_SIZE, CELL_SIZE);

    // ---- Pinky (rose ou bleu si frightened)
    Color pink = new Color(255, 100, 180); // rose Pac-Man
    g.setColor(isFrightened ? Color.BLUE : pink);
    g.fillOval(px, py, CELL_SIZE, CELL_SIZE);

    // ---- Inky (cyan ou bleu si frightened)
    Color inkyBlue = new Color(0, 255, 255); // cyan
    g.setColor(isFrightened ? Color.BLUE : inkyBlue);
    g.fillOval(ix, iy, CELL_SIZE, CELL_SIZE);

}

}
