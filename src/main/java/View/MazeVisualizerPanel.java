package View;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.Line2D;

import Game.GameState;
import Game.EntityPos;
import Game.PelletField;

public class MazeVisualizerPanel extends JPanel {

    private final int[][] mazeGrid;
    private static final int CELL_SIZE = 20; // Taille d'une case en pixels

    private GameState gameState; // État courant du jeu

    public MazeVisualizerPanel(int[][] grid) {
        this.mazeGrid = grid;
        int panelWidth = grid[0].length * CELL_SIZE;
        int panelHeight = grid.length * CELL_SIZE;
        setPreferredSize(new Dimension(panelWidth, panelHeight));

        setFocusable(true);
        requestFocusInWindow();
    }

    

    // Permet au panneau de connaître l'état du jeu
    public void setGameState(GameState state) {
        this.gameState = state;
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
        if (gameState != null) {
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
        return v == 1 || v == 2; // 1 = mur, 2 = mur spécial
    }

    // pacman
    private void drawPacman(Graphics2D g) {
        EntityPos p = gameState.pac;
        int px = p.x() * CELL_SIZE;
        int py = p.y() * CELL_SIZE;

        g.setColor(Color.YELLOW);
        g.fillOval(px, py, CELL_SIZE, CELL_SIZE);
    }

    // pellets
    private void drawPellets(Graphics2D g) {
        PelletField pellets = gameState.pellets();

        boolean[][] small = pellets.getSmall();
        boolean[][] power = pellets.getPower();

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
                    int px = x * CELL_SIZE + CELL_SIZE/2 - r;
                    int py = y * CELL_SIZE + CELL_SIZE/2 - r;
                    g.fillOval(px, py, r * 2, r * 2);
                }
            }
        }
    }

    //ghosts (un seul atm)
    private void drawGhosts(Graphics2D g) {
        EntityPos ghost = gameState.blinky;

        int gx = ghost.x() * CELL_SIZE;
        int gy = ghost.y() * CELL_SIZE;

        g.setColor(Color.RED);
        g.fillOval(gx, gy, CELL_SIZE, CELL_SIZE);
    }
}
