package Game;

import Model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Logique principale du jeu :
 * Gère les mouvements, la consommation de pastilles, les collisions, et l'IA des fantômes
 */
public final class GameLogic {

    // Constantes de position de la sortie de la ghost house
    private static final int GHOST_EXIT_X = 14; 
    private static final int GHOST_EXIT_Y = 12;

    // --- Points d'entrée principaux ---

    public static void step(GameState s) {
        // Gestion des mouvements de Pac-Man
        movePacman(s);
        // Check 1 : si pacman vient de marcher sur le fantôme
        handleGhostCollision(s, true);
        // Gestion de la sortie des fantômes
        manageGhostRelease(s);
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
        handleGhostCollision(s, true);
        // Fin de niveau
        checkLevelCleared(s);
        // Tick suivant
        s.setTick(s.tick() + 1);
    }

    // Ici, pas de déplacement du fantôme (enregistrement uniquement du mouvement du Pac Man)
    public static void stepRecording(GameState s) {
        movePacman(s);
        handlePelletConsumption(s);
        handleFrightenedState(s);
        checkLevelCleared(s);
        s.setTick(s.tick() + 1);
    }

    // Rejoue les mouvements enregistrés, cette fois avec les fantômes actifs
    public static void stepReplay(GameState s) {
        handleGhostCollision(s, false);
        manageGhostRelease(s);
        moveGhostBlinky(s);
        moveGhostPinky(s);
        moveGhostInky(s);
        handlePelletConsumption(s);
        handleFrightenedState(s);
        handleGhostCollision(s, false);
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
            
            reverseIfActive(s.blinky, s.getBlinkyState());
            reverseIfActive(s.pinky, s.getPinkyState());
            reverseIfActive(s.inky, s.getInkyState());
        }
    }

    private static void reverseIfActive(EntityPos g, GhostState state) {
        if (state == GhostState.CHASE || state == GhostState.EXITING) {
            g.setDx(-g.dx()); g.setDy(-g.dy());
        }
    }

    // collision fantôme
    private static void handleGhostCollision(GameState s, boolean resetPacman) {
        boolean collision = 
            (s.pac.x() == s.blinky.x() && s.pac.y() == s.blinky.y()) ||
            (s.pac.x() == s.pinky.x() && s.pac.y() == s.pinky.y()) ||
            (s.pac.x() == s.inky.x() && s.pac.y() == s.inky.y());
        if (collision) {
            if (s.isFrightened()) {
                // fantôme mangé
                s.setScore(s.score() + s.cfg.ghostScore1);

                // Reset Blinky 
                resetGhost(s.blinky, s.cfg.blinkySpawn); 
                s.setBlinkyState(GhostState.IN_HOUSE); 
                s.setBlinkyReleaseTick(s.tick() + 20); 

                // Reset Pinky
                resetGhost(s.pinky, s.cfg.pinkySpawn);   
                s.setPinkyState(GhostState.IN_HOUSE);
                s.setPinkyReleaseTick(s.tick() + 60); 

                // Reset Inky
                resetGhost(s.inky, s.cfg.inkySpawn);     
                s.setInkyState(GhostState.IN_HOUSE);
                s.setInkyReleaseTick(s.tick() + 100); 
            } else {
                // mort pac-man
                s.setLives(s.lives() - 1);
                if (s.lives() > 0) {
                    if (resetPacman) {
                        resetEntityPosition(s.pac, s.cfg.pacSpawn);
                    }
                    resetGhost(s.blinky, s.cfg.blinkySpawn); 
                    s.setBlinkyState(GhostState.IN_HOUSE);
                    s.setBlinkyReleaseTick(s.tick()); 
                    
                    resetGhost(s.pinky, s.cfg.pinkySpawn);   
                    s.setPinkyState(GhostState.IN_HOUSE);
                    s.setPinkyReleaseTick(s.tick() + 20); 
                    
                    resetGhost(s.inky, s.cfg.inkySpawn);     
                    s.setInkyState(GhostState.IN_HOUSE);
                    s.setInkyReleaseTick(s.tick() + 40);
                }
            }
        }
    }

    private static void resetGhost(EntityPos g, EntityPos spawn) {
        resetEntityPosition(g, spawn);
        g.setDx(0); g.setDy(0); 
    }

    private static void manageGhostRelease(GameState s) {
        int t = s.tick();
        
        // Blinky sort immédiatement
        if (s.getBlinkyState() == GhostState.IN_HOUSE && t > s.getBlinkyReleaseTick()) {
            s.setBlinkyState(GhostState.EXITING);
        }
        
        // Pinky sort après 5 secondes 
        if (s.getPinkyState() == GhostState.IN_HOUSE && t > s.getPinkyReleaseTick()) {
            s.setPinkyState(GhostState.EXITING);
        }

        // Inky sort après 10 secondes
        if (s.getInkyState() == GhostState.IN_HOUSE && t > s.getInkyReleaseTick()) {
            s.setInkyState(GhostState.EXITING);
        }
    }

    // Animation d'attente 
    private static void moveIdle(Maze m, EntityPos g) {
        // Si immobile horizontalement ou verticalement, on lance le mouvement
        if (g.dy() == 0) g.setDy(1); 
        
        int ny = g.y() + g.dy();
        CellState target = (ny >= 0 && ny < m.getHeight()) ? m.getState(g.x(), ny) : CellState.MUR;
        
        if (target == CellState.GHOST_HOUSE || target == CellState.SOL) {
            applyMove(m, g, 0, g.dy());
        } else {
            g.setDy(-g.dy()); // Inverse la direction
            applyMove(m, g, 0, g.dy());
        }
    }

    // Logique de déplacement des fantômes
    private static void moveGhost(GameState s, EntityPos g, GhostState currentState) {
        Maze m = s.maze;

        if (currentState == GhostState.IN_HOUSE) {
            moveIdle(m, g);
            return;
        }

        boolean isFrightened = s.isFrightened();

        if (isFrightened) {
            if (currentState == GhostState.CHASE) {
                moveRandomly(m, g);
                return;
            }
        }

        if (currentState == GhostState.EXITING) {
            chooseDirection(m, g, GHOST_EXIT_X, GHOST_EXIT_Y, true);
            if (g.x() == GHOST_EXIT_X && g.y() <= GHOST_EXIT_Y) {
                if (g == s.blinky) s.setBlinkyState(GhostState.CHASE);
                else if (g == s.pinky) s.setPinkyState(GhostState.CHASE);
                else if (g == s.inky) s.setInkyState(GhostState.CHASE);

                g.setDx(-1); g.setDy(0);
            }    
            return;
        }
        // État CHASE
        if (currentState == GhostState.CHASE) {
            // Blinky (chasse Pac-Man)
            if (g == s.blinky) {
                chooseDirection(m, g, s.pac.x(), s.pac.y());
            } 
            // Pinky (chasse 4 cases devant Pac-Man)
            else if (g == s.pinky) {
                int[] target = getPinkyTarget(s);
                chooseDirection(m, g, target[0], target[1]);
            }
            // Inky 
            else if (g == s.inky) {

                int tx = s.pac.x() + 2 * s.pac.dx(); 
                int ty = s.pac.y() + 2 * s.pac.dy();
                if (tx < 0) tx = 0;
                if (ty < 0) ty = 0;
                if (tx >= m.getWidth()) tx = m.getWidth() - 1;
                if (ty >= m.getHeight()) ty = m.getHeight() - 1;
                int vx = tx - s.blinky.x();
                int vy = ty - s.blinky.y();
                int inkyTargetX = tx + vx;
                int inkyTargetY = ty + vy;
                chooseDirection(m, g, inkyTargetX, inkyTargetY);
            }
        }
    }  
    
    private static void moveGhostBlinky(GameState s) {
        moveGhost(s, s.blinky, s.getBlinkyState());
    }

    private static void moveGhostPinky(GameState s) {
        moveGhost(s, s.pinky, s.getPinkyState());
    }

    private static void moveGhostInky(GameState s) {
        moveGhost(s, s.inky, s.getInkyState());
    }

    // IA blinky
    private static void chooseDirection(Maze m, EntityPos g, int tx, int ty, boolean allowSpecial) {
        int[][] dirs = {
            {0,-1},   // Up
            {-1,0},   // Left
            {0,1},    // Down
            {1,0}     // Right
        };
        double bestDist = Double.MAX_VALUE;
        int bestDx = g.dx(), bestDy = g.dy(); 
        boolean found = false;

        for (int[] d : dirs) {
            // éviter demi-tour immédiat
            if (!allowSpecial && d[0] == -g.dx() && d[1] == -g.dy()) continue;
            int nx = g.x() + d[0];
            int ny = g.y() + d[1];

            // Ajout du wrap-around
            if (nx < 0){
                nx = m.getWidth() - 1;
            } else if (nx >= m.getWidth()) {
                nx = 0;
            }

            if (!isWalk(m, nx, ny, allowSpecial)) continue;

            double dist = Math.hypot(nx - tx, ny - ty);

            if (dist < bestDist) {
                bestDist = dist;
                bestDx = d[0];
                bestDy = d[1];
                found = true;
            }
        }
        if (!found) {
             if (allowSpecial) { 
                 applyMove(m, g, 0, -1); 
             } else {
                 applyMove(m, g, -g.dx(), -g.dy());
             }
        } else {
            applyMove(m, g, bestDx, bestDy);
        }
    }

    private static void chooseDirection(Maze m, EntityPos g, int tx, int ty) {
        chooseDirection(m, g, tx, ty, false);
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
        if (!candidates.isEmpty()) {
            int[] choice = candidates.get((int)(Math.random() * candidates.size()));
            applyMove(m, g, choice[0], choice[1]);
        } else {
        // Pour la ghost house 
            g.setDx(-g.dx());
            g.setDy(-g.dy());
        }
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

    // Vérifie si une cellule est marchable
    private static boolean isWalk(Maze m, int x, int y, boolean allowSpecial) {
        if (x < 0 || x >= m.getWidth() || y < 0 || y >= m.getHeight()) {
            return false;
        }

        CellState s = m.getState(x, y);
        if (allowSpecial){
            if (s == CellState.PORTE || s == CellState.GHOST_HOUSE) {
                return true;
            }
        }
        return s == CellState.SOL || s == CellState.TUNNEL;
    }

    private static boolean isWalk(Maze m, int x, int y) {
        return isWalk(m, x, y, false);
    }

}

