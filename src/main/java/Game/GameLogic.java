package Game;

import Model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Logique principale du jeu :
 * Gère les mouvements, la consommation de pastilles, les collisions, et l'IA des fantômes
 */
public final class GameLogic {

    // --- Paramètres de l'IA des fantômes ---
    public static boolean GHOST_A_STAR = true;

    // --- Boucles de jeu ---

    public static void step(GameState s) {
        // Gestion de la pause si une collision a eu lieu
        if (s.freezeTimer > 0) {
            handleFreezeStep(s);
            return;
        }
        // Gestion des mouvements de Pac-Man
        movePacman(s);
        // Gestions des fantômes 
        for (Ghost g : s.ghosts){
            checkCollisionInteraction(s, g);
            if (s.freezeTimer > 0) {
                break; // Arrêt si collision
            }        
            // Gestion de la sortie des fantômes
            manageGhostRelease(s, g);
            // Déplacement du fantôme (v1)
            moveGhost(s, g);
            // Check 2 : Si le fantôme vient de marcher sur pacman
            checkCollisionInteraction(s, g);
            if (s.freezeTimer > 0) {
                break; // Arrêt si collision
            }
        }
        if (s.freezeTimer > 0) {
            return; // Ne pas continuer si le jeu est figé
        }
        // Consommation pellet / power pellet
        handlePelletConsumption(s);
        // Frightened
        handleFrightenedState(s);
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
        if (s.freezeTimer > 0) {
            handleFreezeStep(s);
            return;
        }
        for (Ghost g : s.ghosts){
            checkCollisionInteraction(s, g);
            if (s.freezeTimer > 0) {
                break; 
            }        
            manageGhostRelease(s, g);
            moveGhost(s, g);
            checkCollisionInteraction(s, g);
            if (s.freezeTimer > 0) {
                break; 
            }
        }
        handlePelletConsumption(s);
        handleFrightenedState(s);
        checkLevelCleared(s);
        s.setTick(s.tick() + 1);
    }

    // Gestion du mode freeze (pause après mort de Pac-Man)
    private static void handleFreezeStep(GameState s) {
        s.freezeTimer--;
        if (s.freezeTimer == 0) {
            // La pause est finie : on renvoie le fantôme concerné à la ghost house
            if (s.ghostToReset != null) {
                s.ghostToReset.reset(); 
                s.ghostToReset.setReleaseTick(s.tick() + s.cfg.respawnDelay);
                s.ghostToReset = null;
            }
        }
    }

    // --- Mouvements des entités ---

    // Déplacement de Pac-Man
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
        // Avance dans la direction courante
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

    // Déplacement des fantômes
    private static void moveGhost(GameState s, Ghost g) {
        Maze m = s.maze;
        GhostState currentState = g.getState();

        if (currentState == GhostState.IN_HOUSE) {
            moveIdle(m, g.pos);
            return;
        }

        boolean isFrightened = s.isFrightened();

        if (isFrightened) {
            if (currentState == GhostState.CHASE) {
                moveRandomly(m, g.pos);
                return;
            }
        }

        if (currentState == GhostState.EXITING) {
            int targetX = s.cfg.houseExitTarget.x();
            int targetY = s.cfg.houseExitTarget.y();
            chooseDirectionAStar(m, g.pos, targetX, targetY, true);
            if (g.pos.x() == targetX && g.pos.y() <= targetY) {
                g.setState(GhostState.CHASE);
                g.pos.setDx(-1); 
                g.pos.setDy(0);
            }    
            return;
        }
        // État CHASE
        if (currentState == GhostState.CHASE) {
            int tx = s.pac.x();
            int ty = s.pac.y();

            if (GHOST_A_STAR) {
                chooseDirectionAStar(m, g.pos, tx, ty);
            } else {
                int targetX = tx;
                int targetY = ty;

                switch (g.type) {
                    case BLINKY -> { 
                        targetX = tx; 
                        targetY = ty; 
                    }
                    case PINKY -> { // Vise 4 cases devant Pac-Man
                    int[] target = getPinkyTarget(s);
                    targetX = target[0];
                    targetY = target[1];
                    }
                    case INKY -> { // Vise symétrique de Blinky par rapport à Pac-Man
                        int pivotX = tx + 2 * s.pac.dx();
                        int pivotY = ty + 2 * s.pac.dy();
                        int vecX = pivotX - s.blinky.pos.x();
                        int vecY = pivotY - s.blinky.pos.y();
                        targetX = pivotX + vecX;
                        targetY = pivotY + vecY;
                    }
                    case CLYDE -> { // Chasse si loin (>=8), sinon va en bas à gauche
                        double dist = Math.hypot(g.pos.x() - tx, g.pos.y() - ty);
                        if (dist >= 8) {
                            targetX = tx;
                            targetY = ty;
                        } else {
                            targetX = 0;
                            targetY = m.getHeight() - 1;
                        }
                    }
                }
                chooseDirection(m, g.pos, targetX, targetY);
            }
        }
    }

    // Gestion de la sortie des fantômes de la ghost house
    private static void manageGhostRelease(GameState s, Ghost g) {
        int t = s.tick();
        
        if (g.getState() == GhostState.IN_HOUSE && t > g.getReleaseTick()) {
            g.setState(GhostState.EXITING);
        }
    }

    // Animation d'attente des fantômes dans la ghost house
    private static void moveIdle(Maze m, EntityPos g) {
        // Si immobile horizontalement ou verticalement, on lance le mouvement
        if (g.dy() == 0) g.setDy(1); 
        
        int ny = g.y() + g.dy();
        CellState target = (ny >= 0 && ny < m.getHeight()) ? m.getState(g.x(), ny) : CellState.MUR;
        
        if (target == CellState.GHOST_HOUSE || target == CellState.SOL) {
            applyMove(m, g, 0, g.dy());
        } else {
            g.setDy(-g.dy()); // Rebond
            applyMove(m, g, 0, g.dy());
        }
    }

    // --- Algorithmes de déplacement ---

    // IA : Algorithme de recherche de chemin (Glouton : choisit la case la plus proche de la cible)
    private static void chooseDirection(Maze m, EntityPos g, int tx, int ty, boolean allowSpecial) {
        int[][] dirs = {{0,-1}, {-1,0}, {0,1}, {1,0}}; // Haut, Gauche, Bas, Droite
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

    // IA : Algorithme A* pour un chemin optimal vers la cible
    private static void chooseDirectionAStar(Maze m, EntityPos g, int rawTx, int rawTy, boolean allowSpecial) {
        Point safeTarget = findNearestWalkable(m, rawTx, rawTy);
        int tx = safeTarget.x();
        int ty = safeTarget.y();

        class Node {
            int x, y;
            int g, h;
            Node parent;
            int f() { return g + h; }
        }

        int width = m.getWidth();
        int height = m.getHeight();

        boolean[][] closed = new boolean[width][height];

        PriorityQueue<Node> open = new PriorityQueue<>(
            (a, b) -> Integer.compare(a.f(), b.f())
        );

        Node start = new Node();
        start.x = g.x();
        start.y = g.y();
        start.g = 0;
        start.h = Math.abs(start.x - tx) + Math.abs(start.y - ty);
        open.add(start);

        Node target = null;
        int[][] dirs = {{0,-1}, {-1,0}, {0,1}, {1,0}};

        while (!open.isEmpty()) {
            Node current = open.poll();

            if (current.x == tx && current.y == ty) {
                target = current;
                break;
            }

            closed[current.x][current.y] = true;

            for (int[] d : dirs) {
                if (current.parent == null) {
                    // Pas de demi-tour possible au premier pas
                    if (d[0] == -g.dx() && d[1] == -g.dy()) {
                        continue; 
                    }
                }

                int nx = current.x + d[0];
                int ny = current.y + d[1];

                // wrap-around horizontal
                if (nx < 0) nx = width - 1;
                else if (nx >= width) nx = 0;

                if (ny < 0 || ny >= height) continue;
                if (!isWalk(m, nx, ny, allowSpecial)) continue;
                if (closed[nx][ny]) continue;

                Node n = new Node();
                n.x = nx;
                n.y = ny;
                n.g = current.g + 1;
                n.h = Math.abs(nx - tx) + Math.abs(ny - ty);
                n.parent = current;

                open.add(n);
            }
        }

        if (target== null) {
            // On cherche n'importe quel voisin valide qui n'est PAS un demi-tour
            for(int[] d : dirs) {
                // Si c'est un demi-tour, on ignore
                if (d[0] == -g.dx() && d[1] == -g.dy()) continue; 
                
                int nx = g.x() + d[0];
                int ny = g.y() + d[1];
                // Wrap around pour le fallback aussi
                if (nx < 0) nx = width - 1; else if (nx >= width) nx = 0;

                if (ny >= 0 && ny < height && isWalk(m, nx, ny, allowSpecial)) {
                    applyMove(m, g, d[0], d[1]);
                    return; // On a trouvé une issue de secours
                }
            }

            // Aucun chemin trouvé → fallback
            if (target == null || target.parent == null) {
                applyMove(m, g, -g.dx(), -g.dy());
                return;
            }
        }

        // Remonter jusqu’au premier pas
        while (target.parent != null && target.parent.parent != null) {
            target = target.parent;
        }

        int dx = target.x - g.x();
        int dy = target.y - g.y();
        if (Math.abs(dx) > 1) dx = (dx > 0) ? -1 : 1;
        applyMove(m, g, dx, dy);
    }

    private static void chooseDirectionAStar(Maze m, EntityPos g, int tx, int ty) {
        chooseDirectionAStar(m, g, tx, ty, false);
    }

    // Trouve la case marchable la plus proche de la cible donnée
    private static Point findNearestWalkable(Maze m, int tx, int ty) {
        // Clamp bounds
        if (tx < 0) tx = 0; if (tx >= m.getWidth()) tx = m.getWidth()-1;
        if (ty < 0) ty = 0; if (ty >= m.getHeight()) ty = m.getHeight()-1;

        if (isWalk(m, tx, ty)) return new Point(tx, ty);

        // Recherche spirale simple pour trouver un voisin libre
        for (int r = 1; r < 6; r++) { // Rayon de recherche étendu
             for (int dx = -r; dx <= r; dx++) {
                 for (int dy = -r; dy <= r; dy++) {
                     int nx = tx + dx;
                     int ny = ty + dy;
                     if (nx >= 0 && nx < m.getWidth() && ny >= 0 && ny < m.getHeight()) {
                         if (isWalk(m, nx, ny)) return new Point(nx, ny);
                     }
                 }
             }
        }
        return new Point(tx, ty);
    }

    // Mode frightened : déplacement aléatoire
    private static void moveRandomly(Maze m, EntityPos g) {
        int[][] dirs = {{0,-1}, {-1,0}, {0,1}, {1,0}}; // Haut, Gauche, Bas, Droite
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

    // Applique le mouvement à une entité avec gestion du wrap-around
    private static void applyMove(Maze m, EntityPos e, int dx, int dy) {
        e.setDx(dx); e.setDy(dy);
        int nextX = e.x() + dx;
        int nextY = e.y() + dy;

        // Wrap-around
        if (nextX < 0){
            nextX = m.getWidth() - 1;
        } else if (nextX >= m.getWidth()) {
            nextX = 0;
        }

        e.setX(nextX); e.setY(nextY);
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


    // --- Gestion des Collisions et Gameplay ---
    
    // Vérifie et gère les collisions entre Pac-Man et un fantôme
    private static void checkCollisionInteraction(GameState s, Ghost g) {
        // Detection collision (coordonées identiques)
        boolean collision = (s.pac.x() == g.pos.x() && s.pac.y() == g.pos.y());
        if (collision) {
            if (s.isFrightened()) {
                // Pac-Man mange le fantôme
                s.setScore(s.score() + s.cfg.ghostScore1);
                triggerDeathPause(s, g);
            } else {
                // Fantôme mange Pac-Man
                s.setLives(s.lives() - 1);
                if (s.lives() > 0) {
                    triggerDeathPause(s, g);
                }
            }
        }
    }

    // Action de figer le jeu après une mort
    private static void triggerDeathPause(GameState s, Ghost ghostToReset) {
        s.freezeTimer = 10; // Durée de la pause en ticks
        s.ghostToReset = ghostToReset; // On mémorise quel fantôme devra rentrer à la ghost house     
    }

    // Gestion de la consommation des pellets et power pellets
    private static void handlePelletConsumption(GameState s) {
        int x = s.pac.x();
        int y = s.pac.y();

        if (s.pellets.eatSmall(x, y)) {
            s.setScore(s.score() + s.cfg.pelletScore);
        }

        if (s.pellets.eatPower(x, y)) {
            s.setScore(s.score() + s.cfg.powerScore);
            s.setFrightened(true); // Active le mode frightened
            s.setFrightenedEndTick(s.tick() + s.cfg.frightenedTicks);
            
            // Demi-tour des fantômes frightened
            for (Ghost g : s.ghosts) {
                if (g.getState() == GhostState.CHASE || g.getState() == GhostState.EXITING) {
                    g.pos.setDx(-g.pos.dx());
                    g.pos.setDy(-g.pos.dy());
                }
            }
        }
    }

    // Gestion du mode frightened
    private static void handleFrightenedState(GameState s) {
        if (s.isFrightened() && s.tick() >= s.getFrightenedEndTick()) {
            s.resetFrightened();
        }
    }

    // Fin de niveau
    private static void checkLevelCleared(GameState s) {
        if (s.pellets.remaining() == 0) {
            s.levelCleared = true;
        }
    }

    // --- Fonctions utilitaires ---

    private static int[] dirToDelta(Action a) {
        return switch (a) {
            case UP -> new int[]{0, -1};
            case DOWN -> new int[]{0, 1};
            case LEFT -> new int[]{-1, 0};
            case RIGHT -> new int[]{1, 0};
            default -> new int[]{0, 0};
        };
    }

    // Calcul de la cible de Pinky
    private static int[] getPinkyTarget(GameState s) {
        // Cible 4 cases devant Pac-Man
        return new int[]{ 
            s.pac.x() + 4 * s.pac.dx(), 
            s.pac.y() + 4 * s.pac.dy() 
        };
    }
}

