package View;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Panneau graphique principal.
 * Gère l'affichage du labyrinthe, des entités et du HUD en style rétro.
 */
public class MazeVisualizerPanel extends JPanel {

    // Sprites
    private BufferedImage pacmanImg;
    private BufferedImage blinkyImg;
    private BufferedImage pinkyImg;
    private BufferedImage inkyImg;
    private BufferedImage clydeImg;

    // Animations (1 ligne, 8 frames)
    private BufferedImage[] pacmanAnim;
    private BufferedImage[] blinkyAnim;
    private BufferedImage[] pinkyAnim;
    private BufferedImage[] inkyAnim;
    private BufferedImage[] clydeAnim;

    // Frames indépendantes par fantôme
    private int blinkyFrame = 0;
    private int pinkyFrame  = 0;
    private int inkyFrame   = 0;
    private int clydeFrame  = 0;

    private int pacmanFrame = 0;
    private int animTick = 0;

    // Configuration Graphique 
    private final int[][] mazeGrid;
    private static final int CELL_SIZE = 24;

    // Couleurs rétro
    private static final Color COLOR_BG = new Color(10, 10, 18);
    private static final Color COLOR_WALL_OUTLINE = new Color(33, 33, 222);
    private static final Color COLOR_PELLET = new Color(250, 180, 180);
    private static final Color COLOR_TEXT = new Color(222, 222, 255);

    // Données du jeu
    private Map<String, Integer> pacPosData;
    private Map<String, Integer> blinkyPosData;
    private Map<String, Integer> pinkyPosData;
    private Map<String, Integer> inkyPosData;
    private Map<String, Integer> clydePosData;

    private boolean[][] smallPelletsData;
    private boolean[][] powerPelletsData;
    private boolean isFrightened = false;

    private int currentScore = 0;
    private int currentLives = 3;
    private String currentMode = "WAITING";

    // Taille réelle des sprites fantômes
    private static final int GHOST_SPRITE_WIDTH  = 14;
    private static final int GHOST_SPRITE_HEIGHT = 14;
    private static final int GHOST_SPRITE_GAP    = 3;


    // Initialise le panneau avec la grille statique du labyrinthe.
    public MazeVisualizerPanel(int[][] grid) {
        this.mazeGrid = grid;

        int panelWidth = grid[0].length * CELL_SIZE;
        int panelHeight = grid.length * CELL_SIZE;

        setPreferredSize(new Dimension(panelWidth, panelHeight + 40));
        setBackground(COLOR_BG);
        setFocusable(true);
        requestFocusInWindow();

        try {
            pacmanImg = ImageIO.read(getClass().getResource("/img/Pacman.png"));
            blinkyImg = ImageIO.read(getClass().getResource("/img/Blinky.png"));
            pinkyImg  = ImageIO.read(getClass().getResource("/img/Pinky.png"));
            inkyImg   = ImageIO.read(getClass().getResource("/img/Inky.png"));
            clydeImg  = ImageIO.read(getClass().getResource("/img/Clyde.png"));

            pacmanAnim = splitSprite(pacmanImg, 7);
            blinkyAnim = splitSprite(blinkyImg, 8);
            pinkyAnim  = splitSprite(pinkyImg, 8);
            inkyAnim   = splitSprite(inkyImg, 8);
            clydeAnim  = splitSprite(clydeImg, 8);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Découpe sprites 
    private BufferedImage[] splitSprite(BufferedImage sheet, int frameCount) {
    BufferedImage[] frames = new BufferedImage[8];

    for (int i = 0; i < 8; i++) {
        int x = i * (GHOST_SPRITE_WIDTH + GHOST_SPRITE_GAP);
        frames[i] = sheet.getSubimage(
            x,
            0,
            GHOST_SPRITE_WIDTH,
            GHOST_SPRITE_HEIGHT
        );
    }
    return frames;
}


    // Mise à jour des données
    public void updateGameState(Map<String, Integer> pac, Map<String, Integer> blinky, Map<String, Integer> pinky, 
        Map<String, Integer> inky, Map<String, Integer> clyde,
        boolean[][] smallPellets, boolean[][] powerPellets, boolean frightened,
        int score, int lives, String mode) {

        pacPosData = pac;
        blinkyPosData = blinky;
        pinkyPosData = pinky;
        inkyPosData = inky;
        clydePosData = clyde;

        smallPelletsData = smallPellets;
        powerPelletsData = powerPellets;
        isFrightened = frightened;

        currentScore = score;
        currentLives = lives;
        currentMode = mode;

        repaint();
    }

    // affichage principal
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        animTick++;

        if (animTick % 6 == 0 && pacPosData != null) {
            blinkyFrame = (blinkyFrame + 1) % 2;
            pinkyFrame  = (pinkyFrame  + 1) % 2;
            inkyFrame   = (inkyFrame   + 1) % 2;
            clydeFrame  = (clydeFrame  + 1) % 2;
        }

        g2d.setColor(COLOR_BG);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        drawArcadeWalls(g2d);

        if (pacPosData != null) {
            drawPellets(g2d);
            drawPacman(g2d);
            drawGhosts(g2d);
            drawRetroHUD(g2d);
        }
    }

    // pacman
    private void drawPacman(Graphics2D g) {
        int px = pacPosData.get("x") * CELL_SIZE;
        int py = pacPosData.get("y") * CELL_SIZE;
        g.drawImage(pacmanAnim[pacmanFrame], px, py, CELL_SIZE, CELL_SIZE, null);
    }

    // fantomes
    private void drawGhosts(Graphics2D g) {

        int bBase = getGhostBaseFrame(blinkyPosData);
        int pBase = getGhostBaseFrame(pinkyPosData);
        int iBase = getGhostBaseFrame(inkyPosData);
        int cBase = getGhostBaseFrame(clydePosData);

        g.drawImage(blinkyAnim[bBase + blinkyFrame],
                blinkyPosData.get("x") * CELL_SIZE,
                blinkyPosData.get("y") * CELL_SIZE,
                CELL_SIZE, CELL_SIZE, null);

        g.drawImage(pinkyAnim[pBase + pinkyFrame],
                pinkyPosData.get("x") * CELL_SIZE,
                pinkyPosData.get("y") * CELL_SIZE,
                CELL_SIZE, CELL_SIZE, null);

        g.drawImage(inkyAnim[iBase + inkyFrame],
                inkyPosData.get("x") * CELL_SIZE,
                inkyPosData.get("y") * CELL_SIZE,
                CELL_SIZE, CELL_SIZE, null);

        g.drawImage(clydeAnim[cBase + clydeFrame],
                clydePosData.get("x") * CELL_SIZE,
                clydePosData.get("y") * CELL_SIZE,
                CELL_SIZE, CELL_SIZE, null);
    }

    // Base frame selon direction
    private int getGhostBaseFrame(Map<String, Integer> data) {
        int dx = data.getOrDefault("dx", 0);
        int dy = data.getOrDefault("dy", 0);

        if (dx > 0) return 0; // RIGHT
        if (dx < 0) return 2; // LEFT
        if (dy < 0) return 4; // UP
        return 6;             // DOWN
    }

    // 

    private void drawPellets(Graphics2D g) {
        g.setColor(COLOR_PELLET);
        for (int y = 0; y < smallPelletsData.length; y++) {
            for (int x = 0; x < smallPelletsData[0].length; x++) {
                int cx = x * CELL_SIZE + CELL_SIZE / 2;
                int cy = y * CELL_SIZE + CELL_SIZE / 2;

                if (smallPelletsData[y][x]) g.fillRect(cx - 2, cy - 2, 4, 4);
                if (powerPelletsData[y][x]) {
                    g.setColor(Color.WHITE);
                    g.fillOval(cx - 7, cy - 7, 14, 14);
                    g.setColor(COLOR_PELLET);
                }
            }
        }
    }

    private void drawRetroHUD(Graphics2D g) {
        int yPos = getHeight() - 12;
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.setColor(COLOR_TEXT);

        g.drawString(currentMode, 10, yPos);

        String scoreText = "SCORE: " + currentScore;
        g.drawString(scoreText,
                getWidth() / 2 - g.getFontMetrics().stringWidth(scoreText) / 2,
                yPos);

        String livesText = "VIES: " + currentLives;
        g.drawString(livesText,
                getWidth() - g.getFontMetrics().stringWidth(livesText) - 10,
                yPos);

        g.setColor(COLOR_WALL_OUTLINE);
        g.setStroke(new BasicStroke(2));
        g.drawLine(0, getHeight() - 40, getWidth(), getHeight() - 40);
    }

    private void drawArcadeWalls(Graphics2D g2d) {
        drawMazePath(g2d, COLOR_WALL_OUTLINE, CELL_SIZE - 4);
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
                    if (est_mur(y, x + 1)) g2d.draw(new Line2D.Float(cx, cy, cx + CELL_SIZE, cy));
                    if (est_mur(y + 1, x)) g2d.draw(new Line2D.Float(cx, cy, cx, cy + CELL_SIZE));
                }
            }
        }
    }

    private boolean est_mur(int y, int x) {
        if (x < 0 || x >= mazeGrid[0].length || y < 0 || y >= mazeGrid.length) return false;
        int v = mazeGrid[y][x];
        return v == 1 || v == 2;
    }
}
