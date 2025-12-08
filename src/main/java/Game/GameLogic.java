package Game;

import Model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Logique principale du jeu :
 * Gère les mouvements, la consommation de pastilles, les collisions, et l'IA des fantômes
 */
public final class GameLogic {

    // --- Points d'entrée principaux ---

    public static void step(GameState s) {
        // Gestion des mouvements de Pac-Man
        movePacman(s);
        // Check 1 : si pacman vient de marcher sur le fantôme
        handleGhostCollision(s);
        // Déplacement du fantôme (v1)
        moveGhostBlinky(s);
        // Déplacement Pinky
        moveGhostPinky(s);
        // Déplacement Inky 
        moveGhostInky(s);
        // Consommation pellet / power pellet
        handlePelletConsumption(s);
        // Frightened
        handleFrightenedState(s);
        // Check 2 : Si le fantôme vient de marcher sur pacman
        handleGhostCollision(s);
        // Fin de niveau
        checkLevelCleared(s);
        // Tick suivant
        s.setTick(s.tick() + 1);
    }

    // Ici, pas de déplacement du fantôme (enregistrement uniquement du mouvement du Pac Man)
    public static void stepRecording(GameState s) {
        movePacman(s);
        handlePelletConsumption(s);
        checkLevelCleared(s);
        s.setTick(s.tick() + 1);
    }

    // Rejoue les mouvements enregistrés, cette fois avec les fantômes actifs
    public static void stepReplay(GameState s) {
        handleGhostCollision(s);
        moveGhostBlinky(s);
        moveGhostPinky(s);
        moveGhostInky(s);
        handlePelletConsumption(s);
        handleFrightenedState(s);
        handleGhostCollision(s);
        checkLevelCleared(s);
        s.setTick(s.tick() + 1);
    }

    // --- Logique de déplacement & Gameplay ---

    // pacman 
    private static void movePacman(GameState s) {
        Maze m = s.maze;
        EntityPos e = s.pac;

        Action desired = s.getDesiredDir();
        if (desired != Action.NONE) {
            int[] turn = dirToDelta(desired);

            int turnX = e.x() + turn[0];
            int turnY = e.y() + turn[1];

            if (isWalk(m, turnX, turnY)) {
                // Virage accepté
                e.setDx(turn[0]);
                e.setDy(turn[1]);
                s.setCurrentDir(desired);
            }
        }
        //deplacement direction actuelle
        int nx = e.x() + e.dx();
        int ny = e.y() + e.dy();

        // Ajout du wrap-around
        if (nx < 0 || nx >= m.getWidth()) { 
            if (m.getState(e.x(), e.y()) == CellState.TUNNEL) {
                if (nx < 0) { // Sortie gauche (x=-1), va vers la droite
                    e.setX(m.getWidth() - 1);
                } else { // Sortie droite (x=width), va vers la gauche
                    e.setX(0);
                }
                e.setY(ny); 
                return; 
            }
        }

        if (isWalk(m, nx, ny)) {
            e.setX(nx);
            e.setY(ny);
        }
    }

    // pellets
    private static void handlePelletConsumption(GameState s) {
        int x = s.pac.x();
        int y = s.pac.y();

        if (s.pellets.eatSmall(x, y)) {
            s.setScore(s.score() + s.cfg.pelletScore);
        }

        if (s.pellets.eatPower(x, y)) {
            s.setScore(s.score() + s.cfg.powerScore);
            s.setFrightened(true);
            s.setFrightenedEndTick(s.tick() + s.cfg.frightenedTicks);
            s.blinky.setDx( -s.blinky.dx() );
            s.blinky.setDy( -s.blinky.dy() );
            s.pinky.setDx( -s.pinky.dx() );
            s.pinky.setDy( -s.pinky.dy() );
            s.inky.setDx( -s.inky.dx() );
            s.inky.setDy( -s.inky.dy() );
        }
    }



    // collision fantôme
    private static void handleGhostCollision(GameState s) {
        if (s.pac.x() == s.blinky.x() && s.pac.y() == s.blinky.y() || s.pac.x() == s.pinky.x() && s.pac.y() == s.pinky.y() || s.pac.x() == s.inky.x() && s.pac.y() == s.inky.y()) {
            if (s.isFrightened()) {
                // fantôme mangé
                s.setScore(s.score() + s.cfg.ghostScore1);
                resetEntityPosition(s.blinky, s.cfg.blinkySpawn);
                resetEntityPosition(s.pinky, s.cfg.pinkySpawn);
                resetEntityPosition(s.inky, s.cfg.inkySpawn);
            } else {
                // mort pac-man
                s.setLives(s.lives() - 1);
                if (s.lives() > 0) {
                    resetEntityPosition(s.pac, s.cfg.pacSpawn);
                    resetEntityPosition(s.blinky, s.cfg.blinkySpawn);
                    resetEntityPosition(s.pinky, s.cfg.pinkySpawn);
                    resetEntityPosition(s.inky, s.cfg.inkySpawn);
                }
            }
        }
    }

    // ghost
    private static void moveGhostBlinky(GameState s) {
        EntityPos g = s.blinky;
        EntityPos pac = s.pac;
        Maze m = s.maze;

        if (s.isFrightened()){
            moveRandomly(m, g);
        } else {
            chooseDirection(m, g, pac.x(), pac.y());
        }
    }


    // IA blinky
    private static void chooseDirection(Maze m, EntityPos g, int tx, int ty) {
        int[][] dirs = {
            {0,-1},   // Up
            {-1,0},   // Left
            {0,1},    // Down
            {1,0}     // Right
        };
        double bestDist = Double.MAX_VALUE;
        int bestDx = g.dx(), bestDy = g.dy(); // fallback = continuer tout droit
        boolean foundMove = false;

        for (int[] d : dirs) {
            // éviter demi-tour immédiat
            if (d[0] == -g.dx() && d[1] == -g.dy()) continue;
            int nx = g.x() + d[0];
            int ny = g.y() + d[1];

            if (nx < 0){
                nx = m.getWidth() - 1;
            } else if (nx >= m.getWidth()) {
                nx = 0;
            }

            if (!isWalk(m, nx, ny)) continue;

            double dist = Math.hypot(nx - tx, ny - ty);

            if (dist < bestDist) {
                bestDist = dist;
                bestDx = d[0];
                bestDy = d[1];
                foundMove = true;
            }
        }
        applyMove(m, g, bestDx, bestDy);
    }

    private static int[] getPinkyTarget(GameState s) {
        EntityPos pac = s.pac;

        int dx = pac.dx();
        int dy = pac.dy();

        // Pinky vise 4 cases devant Pac-Man
        int tx = pac.x() + 4 * dx;
        int ty = pac.y() + 4 * dy;

        return new int[]{tx, ty};
    }

    private static void moveGhostPinky(GameState s) {
        EntityPos g = s.pinky;
        Maze m = s.maze;

        if (s.isFrightened()) {
            moveRandomly(m, g);
            return;
        }

        int[] target = getPinkyTarget(s);
        chooseDirection(m, g, target[0], target[1]);
    }

    private static void moveGhostInky(GameState s) {
    EntityPos inky = s.inky;
    EntityPos pac = s.pac;
    EntityPos blinky = s.blinky;
    Maze m = s.maze;

    if (s.isFrightened()) {
        moveRandomly(m, inky);
        return;
    }

    int tx = pac.x() + 2 * pac.dx(); // direction de Pac-Man
    int ty = pac.y() + 2 * pac.dy();

    if (tx < 0) tx = 0;
    if (ty < 0) ty = 0;
    if (tx >= m.getWidth()) tx = m.getWidth() - 1;
    if (ty >= m.getHeight()) ty = m.getHeight() - 1;

   
    int vx = tx - blinky.x();
    int vy = ty - blinky.y();

    
    int inkyTargetX = tx + vx;
    int inkyTargetY = ty + vy;

    chooseDirection(m, inky, inkyTargetX, inkyTargetY);
}



    // frightened
    private static void moveRandomly(Maze m, EntityPos g) {
        int[][] dirs = {
            {0,-1},   // Up
            {-1,0},   // Left
            {0,1},    // Down
            {1,0}     // Right
        };
        List<int[]> candidates = new ArrayList<>();

        for (int[] d : dirs) {
            // éviter demi-tour immédiat
            if (d[0] == -g.dx() && d[1] == -g.dy()) continue;
            int nx = g.x() + d[0];
            int ny = g.y() + d[1];

            if (nx < 0){
                nx = m.getWidth() - 1;
            } else if (nx >= m.getWidth()) {
                nx = 0;
            }

            if (isWalk(m, nx, ny)) {
                candidates.add(d);
            }
        }
            int[] choice = candidates.get((int)(Math.random() * candidates.size()));
            applyMove(m, g, choice[0], choice[1]);
    }

    private static void applyMove(Maze m, EntityPos e, int dx, int dy) {
        e.setDx(dx);
        e.setDy(dy);

        int nextX = e.x() + dx;
        int nextY = e.y() + dy;

        // Wrap-around
        if (nextX < 0){
            nextX = m.getWidth() - 1;
        } else if (nextX >= m.getWidth()) {
            nextX = 0;
        }

        e.setX(nextX);
        e.setY(nextY);
    }

    // --- Fonctions utilitaires ---

    // frightened
    private static void handleFrightenedState(GameState s) {
        if (s.isFrightened() && s.tick() >= s.getFrightenedEndTick()) {
            s.resetFrightened();
        }
    }

    // level cleared
    private static void checkLevelCleared(GameState s) {
        if (s.pellets.remaining() == 0) {
            s.levelCleared = true;
        }
    }

    private static void resetEntityPosition(EntityPos e, EntityPos spawn) {
        e.setX(spawn.x());
        e.setY(spawn.y());
        e.setDx(spawn.dx());
        e.setDy(spawn.dy());
    }

    private static int[] dirToDelta(Action a) {
        return switch (a) {
            case UP -> new int[]{0, -1};
            case DOWN -> new int[]{0, 1};
            case LEFT -> new int[]{-1, 0};
            case RIGHT -> new int[]{1, 0};
            default -> new int[]{0, 0};
        };
    }

    private static boolean isWalk(Maze m, int x, int y) {
        if (x < 0 || x >= m.getWidth() || y < 0 || y >= m.getHeight()) {
            return false;
        }
        CellState s = m.getState(x, y);
        return s == CellState.SOL || s == CellState.TUNNEL;
    }
}
