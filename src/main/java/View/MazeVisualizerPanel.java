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
    private BufferedImage blinkyImg;
    private BufferedImage pinkyImg;
    private BufferedImage inkyImg;
    private BufferedImage clydeImg;

    // Animations
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

    // Configuration Graphique
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

    //Frightened
    private boolean frightened = false;
    private long frightenedStartTime = 0;

    // Durées
    private static final int FRIGHTENED_DURATION = 6000;
    private static final int FRIGHTENED_BLINK_TIME = 2000;

    // Initialise le panneau avec la grille statique du labyrinthe.
    public MazeVisualizerPanel(int[][] grid) {
        this.mazeGrid = grid;

        int panelWidth = grid[0].length * CELL_SIZE;
        int panelHeight = grid.length * CELL_SIZE;

        // Espace pour le HUD en bas
        setPreferredSize(new Dimension(panelWidth, panelHeight + 40));
        setBackground(COLOR_BG);
        setFocusable(true);
        requestFocusInWindow();

        try {
            blinkyImg = ImageIO.read(getClass().getResource("/img/Blinky.png"));
            pinkyImg  = ImageIO.read(getClass().getResource("/img/Pinky.png"));
            inkyImg   = ImageIO.read(getClass().getResource("/img/Inky.png"));
            clydeImg  = ImageIO.read(getClass().getResource("/img/Clyde.png"));

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

    // Met à jour toutes les données d'affichage et redessine la scène.
    // Appelée à chaque tick par le contrôleur.
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

    // Dessin du panneau
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

        // Fond rétro
        g2d.setColor(COLOR_BG);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Murs creux
        drawArcadeWalls(g2d);

        // Murs creux
        if (pacPosData != null) {
            drawPellets(g2d);
            drawPacman(g2d);
            drawGhosts(g2d);
            drawRetroHUD(g2d);
        }

        // Gère la fin du mode frightened
        if (frightened) {
            long elapsed = System.currentTimeMillis() - frightenedStartTime;
            if (elapsed > FRIGHTENED_DURATION) {
                frightened = false;
            }
        }

    }

    // Dessine Pacman
    private void drawPacman(Graphics2D g) {
        int px = pacPosData.get("x") * CELL_SIZE;
        int py = pacPosData.get("y") * CELL_SIZE;

        int margin = 3;
        int size = CELL_SIZE - 2 * margin;

        int dx = pacPosData.getOrDefault("dx", 0);
        int dy = pacPosData.getOrDefault("dy", 0);

        // Animation bouche (0 → ouverte, 1 → fermée)
        int mouthAngle = (pacmanFrame == 0) ? 40 : 10;

        int startAngle;

        if (dx > 0) {          // RIGHT
            startAngle = mouthAngle;
        } else if (dx < 0) {   // LEFT
            startAngle = 180 + mouthAngle;
        } else if (dy < 0) {   // UP
            startAngle = 90 + mouthAngle;
        } else {               // DOWN ou immobile
            startAngle = 270 + mouthAngle;
        }

        g.setColor(Color.YELLOW);
        g.fillArc(
            px + margin,
            py + margin,
            size,
            size,
            startAngle,
            360 - 2 * mouthAngle
        );
    }



    // Dessine fantôme frightened
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

    // Dessine les fantômes
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

    

    // dessine fantome avec bonne direction
    private void drawGhost(Graphics2D g, BufferedImage[] anim,
                           Map<String, Integer> pos, int frame) { 
        int base = getGhostBaseFrame(pos);
        g.drawImage(anim[base + frame],
                pos.get("x") * CELL_SIZE,
                pos.get("y") * CELL_SIZE,
                CELL_SIZE, CELL_SIZE, null);
    }

    

    private int getGhostBaseFrame(Map<String, Integer> data) { // Direction fantôme
        int dx = data.getOrDefault("dx", 0);
        int dy = data.getOrDefault("dy", 0);

        if (dx > 0) return 0;
        if (dx < 0) return 2;
        if (dy < 0) return 4;
        return 6;
    }


    // Dessine les pastilles
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

    // Affiche le HUD (Score, Vies, Mode) en bas de l'écran
    private void drawRetroHUD(Graphics2D g) { 
        int yPos = getHeight() - 12;
        g.setFont(new Font("Monospaced", Font.BOLD, 16)); 
        g.setColor(COLOR_TEXT);

        // Mode à gauche
        g.drawString(currentMode, 10, yPos);

        // Score centré
        String scoreText = "SCORE: " + currentScore;
        g.drawString(scoreText,
                getWidth() / 2 - g.getFontMetrics().stringWidth(scoreText) / 2,
                yPos);
        
        // Vies à droite
        String livesText = "VIES: " + currentLives;
        g.drawString(livesText,
                getWidth() - g.getFontMetrics().stringWidth(livesText) - 10,
                yPos);
        
        // Ligne de séparation bleue
        g.setColor(COLOR_WALL_OUTLINE);
        g.setStroke(new BasicStroke(2));
        g.drawLine(0, getHeight() - 40, getWidth(), getHeight() - 40);
    }

    // Dessine les murs en deux passes pour créer l'effet "double trait" arcade
    private void drawArcadeWalls(Graphics2D g2d) {
        // Le contour (Gros trait bleu)
        drawMazePath(g2d, COLOR_WALL_OUTLINE, CELL_SIZE - 4);
        // L'intérieur (Trait moyen noir pour creuser)
        drawMazePath(g2d, COLOR_BG, CELL_SIZE - 10);
    }

    // Dessine la structure des murs selon l'épaisseur donnée
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

    // Vérifie si une case de la grille est un mur
    private boolean est_mur(int y, int x) { 
        if (x < 0 || x >= mazeGrid[0].length || y < 0 || y >= mazeGrid.length) return false;
        int v = mazeGrid[y][x];
        return v == 1 || v == 2; // 1 = mur, 2 = mur permanent
    }
}
