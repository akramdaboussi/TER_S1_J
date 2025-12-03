package Game;

import Model.*;

/**
 * Logique principale du jeu :
 * - déplacement de Pac-Man
 * - tentative de changement de direction (desiredDir → currentDir)
 * - déplacement du fantôme
 * - gestion pellet / power pellet
 * - gestion frightened
 * - gestion collision fantôme
 */
public final class GameLogic {

    // un tick = une iteration
    public static void step(GameState s) {

        // Gestion des mouvements de Pac-Man
        movePacman(s);

        // Déplacement du fantôme (v1)
        moveGhostBlinky(s);


        // Consommation pellet / power pellet
        handlePelletConsumption(s);

        // Frightened
        if (s.isFrightened() && s.tick() >= s.getFrightenedEndTick()) {
            s.resetFrightened();
        }

        // Collision Pac-Man / fantôme
        handleGhostCollision(s);

        // Fin de niveau
        if (s.pellets.remaining() == 0) {
            s.levelCleared = true;
        }

        // Tick suivant
        s.setTick(s.tick() + 1);
    }

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

    private static int[] dirToDelta(Action a) {
        return switch (a) {
            case UP -> new int[]{0, -1};
            case DOWN -> new int[]{0, 1};
            case LEFT -> new int[]{-1, 0};
            case RIGHT -> new int[]{1, 0};
            default -> new int[]{0, 0};
        };
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
        }
    }

    // collision fantôme

    private static void handleGhostCollision(GameState s) {
        if (s.pac.x() == s.blinky.x() && s.pac.y() == s.blinky.y()) {
            if (s.isFrightened()) {
                // fantôme mangé
                s.setScore(s.score() + s.cfg.ghostScore1);
                resetEntityPosition(s.blinky, s.cfg.blinkySpawn);
            } else {
                // mort pac-man
                s.setLives(s.lives() - 1);
                resetEntityPosition(s.pac, s.cfg.pacSpawn);
                resetEntityPosition(s.blinky, s.cfg.blinkySpawn);
            }
        }
    }

    // ghost

    private static void chooseDirection(Maze m, EntityPos g, int tx, int ty) {

    int[][] dirs = {
        {0,-1},   // Up
        {-1,0},   // Left
        {0,1},    // Down
        {1,0}     // Right
    };

    double bestDist = Double.MAX_VALUE;
    int bestDx = g.dx(), bestDy = g.dy(); // fallback = continuer tout droit

    for (int[] d : dirs) {

        // éviter demi-tour immédiat
        if (d[0] == -g.dx() && d[1] == -g.dy())
            continue;

        int nx = g.x() + d[0];
        int ny = g.y() + d[1];

        if (!isWalk(m, nx, ny))
            continue;

        double dist = Math.hypot(nx - tx, ny - ty);

        if (dist < bestDist) {
            bestDist = dist;
            bestDx = d[0];
            bestDy = d[1];
        }
    }

    // déplacer le fantôme
    g.setDx(bestDx);
    g.setDy(bestDy);
    g.setX(g.x() + bestDx);
    g.setY(g.y() + bestDy);
    }


    private static void moveGhostBlinky(GameState s) {
    EntityPos g = s.blinky;
    EntityPos pac = s.pac;

    chooseDirection(s.maze, g, pac.x(), pac.y());

    }


    // utilitaires

    private static void resetEntityPosition(EntityPos e, EntityPos spawn) {
        e.setX(spawn.x());
        e.setY(spawn.y());
        e.setDx(spawn.dx());
        e.setDy(spawn.dy());
    }

    private static boolean isWalk(Maze m, int x, int y) {
        if (x < 0 || x >= m.getWidth() || y < 0 || y >= m.getHeight()) {
            return false;
        }
        CellState s = m.getState(x, y);
        return s == CellState.SOL || s == CellState.TUNNEL;
    }
}
