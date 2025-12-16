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

    // Animations
    private BufferedImage[] pacmanAnim;
    private BufferedImage[] blinkyAnim;
    private BufferedImage[] pinkyAnim;
    private BufferedImage[] inkyAnim;
    private BufferedImage[] clydeAnim;
    private BufferedImage frightenedImg;
    private BufferedImage[] frightenedAnim;


    // Frames
    private int pacmanFrame = 0;
    private int blinkyFrame = 0;
    private int pinkyFrame  = 0;
    private int inkyFrame   = 0;
    private int clydeFrame  = 0;
    private int animTick = 0;

    // Configuration
    private final int[][] mazeGrid;
    private static final int CELL_SIZE = 24;

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

    private int currentScore = 0;
    private int currentLives = 3;
    private String currentMode = "WAITING";

    // Taille fantômes
    private static final int SPRITE_WIDTH  = 14;
    private static final int SPRITE_HEIGHT = 14;
    private static final int SPRITE_GAP    = 3;

    private static final int[] PACMAN_WIDTHS = {
        13, 9, 13, 9, 13, 9, 13, 9
    };

    //Frightened
    private boolean frightened = false;
    private long frightenedStartTime = 0;

    // Durées
    private static final int FRIGHTENED_DURATION = 6000;
    private static final int FRIGHTENED_BLINK_TIME = 2000;

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

            pacmanAnim = splitPacmanSprite(pacmanImg);
            blinkyAnim = splitSprite(blinkyImg);
            pinkyAnim  = splitSprite(pinkyImg);
            inkyAnim   = splitSprite(inkyImg);
            clydeAnim  = splitSprite(clydeImg);

            frightenedImg = ImageIO.read(getClass().getResource("/img/Frightened.png"));
            frightenedAnim = splitSprite(frightenedImg);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Découpe fantômes
    private BufferedImage[] splitSprite(BufferedImage sheet) {
        BufferedImage[] frames = new BufferedImage[8];
        for (int i = 0; i < 8; i++) {
            int x = i * (SPRITE_WIDTH + SPRITE_GAP);
            frames[i] = sheet.getSubimage(x, 0, SPRITE_WIDTH, SPRITE_HEIGHT);
        }
        return frames;
    }

    // Découpe Pacman
    private BufferedImage[] splitPacmanSprite(BufferedImage sheet) {
        BufferedImage[] frames = new BufferedImage[8];
        int x = 0;

        for (int i = 0; i < 8; i++) {
            frames[i] = sheet.getSubimage(
                x,
                0,
                PACMAN_WIDTHS[i],
                SPRITE_HEIGHT
            );
            x += PACMAN_WIDTHS[i] + SPRITE_GAP;
        }
        return frames;
    }

    private int getPacmanBaseFrame(Map<String, Integer> data) {
        int dx = data.getOrDefault("dx", 0);
        int dy = data.getOrDefault("dy", 0);

        if (dx > 0) return 0;
        if (dx < 0) return 2;
        if (dy < 0) return 4;
        return 6;
    }

    public void updateGameState(Map<String, Integer> pac, Map<String, Integer> blinky,
                                Map<String, Integer> pinky, Map<String, Integer> inky,
                                Map<String, Integer> clyde,
                                boolean[][] smallPellets, boolean[][] powerPellets,
                                boolean frightened, int score, int lives, String mode) {

        pacPosData = pac;
        blinkyPosData = blinky;
        pinkyPosData = pinky;
        inkyPosData = inky;
        clydePosData = clyde;
        smallPelletsData = smallPellets;
        powerPelletsData = powerPellets;
        currentScore = score;
        currentLives = lives;
        currentMode = mode;

        if (frightened && !this.frightened) {
            frightenedStartTime = System.currentTimeMillis();
        }
        this.frightened = frightened;


        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        animTick++;
        if (animTick % 6 == 0) {
            pacmanFrame = (pacmanFrame + 1) % 2;
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

        if (frightened) {
            long elapsed = System.currentTimeMillis() - frightenedStartTime;
            if (elapsed > FRIGHTENED_DURATION) {
                frightened = false;
            }
        }

    }

    private void drawPacman(Graphics2D g) { // dessine pacman
        int px = pacPosData.get("x") * CELL_SIZE;
        int py = pacPosData.get("y") * CELL_SIZE;
        int base = getPacmanBaseFrame(pacPosData);

        g.drawImage(
            pacmanAnim[base + pacmanFrame],
            px,
            py,
            CELL_SIZE,
            CELL_SIZE,
            null
        );
    }

    private void drawFrightenedGhost(Graphics2D g, Map<String, Integer> pos) {
        long elapsed = System.currentTimeMillis() - frightenedStartTime;
        long remaining = FRIGHTENED_DURATION - elapsed;

        int frame;

        if (remaining <= FRIGHTENED_BLINK_TIME) {
            // clignote
            frame = (animTick / 6) % 2 == 0 ? 0 : 2;
        } else {
            // bleu normal
            frame = 0;
        }

        g.drawImage(
            frightenedAnim[frame + (animTick / 6) % 2],
            pos.get("x") * CELL_SIZE,
            pos.get("y") * CELL_SIZE,
            CELL_SIZE,
            CELL_SIZE,
            null
        );
    }


    private void drawGhosts(Graphics2D g) { 
        if (frightened) {
            drawFrightenedGhost(g, blinkyPosData);
            drawFrightenedGhost(g, pinkyPosData);
            drawFrightenedGhost(g, inkyPosData);
            drawFrightenedGhost(g, clydePosData);
        } else {
            drawGhost(g, blinkyAnim, blinkyPosData, blinkyFrame);
            drawGhost(g, pinkyAnim,  pinkyPosData,  pinkyFrame);
            drawGhost(g, inkyAnim,   inkyPosData,   inkyFrame);
            drawGhost(g, clydeAnim,  clydePosData,  clydeFrame);
        }
    }

    


    private void drawGhost(Graphics2D g, BufferedImage[] anim,
                           Map<String, Integer> pos, int frame) { 
        int base = getGhostBaseFrame(pos);
        g.drawImage(anim[base + frame],
                pos.get("x") * CELL_SIZE,
                pos.get("y") * CELL_SIZE,
                CELL_SIZE, CELL_SIZE, null);
    }

    

    private int getGhostBaseFrame(Map<String, Integer> data) { // direction fantôme
        int dx = data.getOrDefault("dx", 0);
        int dy = data.getOrDefault("dy", 0);

        if (dx > 0) return 0;
        if (dx < 0) return 2;
        if (dy < 0) return 4;
        return 6;
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

    private void drawRetroHUD(Graphics2D g) { // affichage style arcade
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
                    if (!est_mur(y, x+1) && !est_mur(y, x-1) && !est_mur(y+1, x) && !est_mur(y-1, x)) {
                         g2d.drawLine(cx, cy, cx, cy);
                    }
                    if (est_mur(y, x + 1)) g2d.draw(new Line2D.Float(cx, cy, cx + CELL_SIZE, cy));
                    if (est_mur(y + 1, x)) g2d.draw(new Line2D.Float(cx, cy, cx, cy + CELL_SIZE));
                }
            }
        }
    }


    private boolean est_mur(int y, int x) { // check si mur
        if (x < 0 || x >= mazeGrid[0].length || y < 0 || y >= mazeGrid.length) return false;
        int v = mazeGrid[y][x];
        return v == 1 || v == 2;
    }
}
