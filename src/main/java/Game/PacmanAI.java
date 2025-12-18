package Game;

import Model.CellState;
import java.util.LinkedList;

public class PacmanAI {

    // Paramètres de recherche
    private static final int HORIZON = 4; // Profondeur de récursion (4 coups à l'avance)
    
    // Paramètres Monte-Carlo pour Expectimax
    private static final int SAMPLES_FRIGHTENED = 5; // Plus d'échantillons en mode effrayé
    private static final int SAMPLES_CHASE = 1; // Moins d'échantillons en mode poursuite

    // Mémoire des positions pour éviter les boucles 
    private final LinkedList<String> positionHistory = new LinkedList<>();
    private static final int MEMORY_SIZE = 12;


    // Calcule la meilleure action à effectuer dans l'état de jeu donné
    public Action getBestAction(GameState currentState) {
        // Mise à jour de l'historique des positions
        String currentPosKey = currentState.pac.x() + "," + currentState.pac.y();
        positionHistory.add(currentPosKey);
        if (positionHistory.size() > MEMORY_SIZE) {
            positionHistory.removeFirst();
        }
        return runMinimax(currentState);
    }

    // Minimax contre IA A* avec élagage Alpha-Beta
    private Action runMinimax(GameState currentState) {
        double maxVal = Double.NEGATIVE_INFINITY;
        Action bestAction = Action.NONE;
        Action currentDir = currentState.getCurrentDir();
        
        // Initialisation Alpha-Beta 
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        Action[] possibleActions = {Action.UP, Action.DOWN, Action.LEFT, Action.RIGHT};

        for (Action action : possibleActions) {
            if (!isValidMove(currentState, action)) continue;

            double sumValues = 0;
            // Si fantomes effrayés, on fait 5 simulations, sinon 1 seule
            int iterations = currentState.isFrightened() ? SAMPLES_FRIGHTENED : SAMPLES_CHASE;
            
            for (int i = 0; i < iterations; i++) {
                GameState nextState = currentState.copy();
                nextState.setDesiredDir(action);
                GameLogic.step(nextState);
                sumValues += minimaxRecursive(nextState, HORIZON - 1, alpha, beta);
            }
            // Moyenne des scores 
            double val = sumValues / iterations;

            // Application des sécurités (Anti-Boucle et Anti-Tremblement)
            val = applySafetyPenalties(val, currentState, action, currentDir);

            if (val > maxVal) {
                maxVal = val;
                bestAction = action;
            }
            alpha = Math.max(alpha, maxVal); // Mise à jour de la meilleure option trouvée
        }
        return bestAction;
    }

    private double minimaxRecursive(GameState state, int depth, double alpha, double beta) {
        if (depth == 0 || state.lives() <= 0 || state.levelCleared) {
            return evaluate(state);
        }
        double maxVal = Double.NEGATIVE_INFINITY;
        Action possibleActions[] = {Action.UP, Action.DOWN, Action.LEFT, Action.RIGHT};

        // On explore les coups possibles de Pac-Man au tour suivant
        for (Action action : possibleActions) {
            if (!isValidMove(state, action)) continue;

            GameState nextState = state.copy();
            nextState.setDesiredDir(action);
            GameLogic.step(nextState); 

            double val = minimaxRecursive(nextState, depth - 1, alpha, beta);
            
            maxVal = Math.max(maxVal, val);
            alpha = Math.max(alpha, maxVal);
            
            // Elagage Alpha-Beta : Si cette branche est pire qu'une déjà trouvée, on coupe
            if (beta <= alpha) break; 
        }
        // Si bloqué, retourne l'état actuel, sinon le max trouvé
        return (maxVal == Double.NEGATIVE_INFINITY) ? evaluate(state) : maxVal;
    }


    // Méthode pour empêcher les boucles et tremblements
    private double applySafetyPenalties(double score, GameState currentState, Action action, Action currentDir) {
        // Anti-Boucle
        int nextX = currentState.pac.x();
        int nextY = currentState.pac.y();
        switch(action) {
            case UP -> nextY--; 
            case DOWN -> nextY++;
            case LEFT -> nextX--; 
            case RIGHT -> nextX++;
            case NONE -> {}
        }
        // Vérifie combien de fois cette position a été visitée récemment
        String targetPosKey = nextX + "," + nextY;
        int visits = 0;
        for (String pos : positionHistory) {
            if (pos.equals(targetPosKey)) visits++;
        }
        // Si visitée plus de 1 fois récemment -> Grosse prénalité pour forcer l'exploration
        if (visits > 1) {
            score -= 10000.0; 
        }
        // Anti-Tremblement
        if (action == getOpposite(currentDir)) {
            score -= 200.0; // Pénalise le demi-tour 
        } else if (action == currentDir) {
            score += 10.0;  // Favorise la continuité de la direction
        } 
        return score;
    }


    // Évaluation heuristique de l'état du jeu   
    private double evaluate(GameState state) {
        if (state.lives() <= 0) return -1000000.0;
        if (state.levelCleared) return 1000000.0;

        double score = state.score();
        double distPellet = getNearestPelletDist(state);
        double ghostScore = evaluateGhosts(state);

        // Formule d'évaluation : Score actuel - (Poids * DistancePellet) + DangerFantômes
        return (score * 10) - (distPellet * 5) + ghostScore;
    }

    // Évalue la menace ou opportunité des fantômes
    private double evaluateGhosts(GameState s) {
        double val = 0;
        int px = s.pac.x();
        int py = s.pac.y();

        for(Ghost g : s.ghosts) {
            double d = Math.abs(px - g.pos.x()) + Math.abs(py - g.pos.y());
            if (s.isFrightened()) {
                // Si vulnérable et proche -> Bonus pour le manger
                if (d < 8) val += (200.0 / (d + 1)); 
            } else {
                // Si dangereux -> Pénalité exponentielle selon la proximité
                if (d <= 1) val -= 10000; 
                else if (d < 3) val -= 1000; 
                else if (d < 5) val -= 200; 
            }
        }
        return val;
    }

    // Trouve la distance vers le pellet le plus proche
    private double getNearestPelletDist(GameState s) {
        int px = s.pac.x();
        int py = s.pac.y();
        double minDst = 9999;
        boolean[][] small = s.pellets.getSmall();
        int w = s.maze.getWidth();
        int h = s.maze.getHeight();

        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                if(small[y][x]) {
                    double d = Math.abs(px - x) + Math.abs(py - y);
                    if(d < minDst) minDst = d;
                }
            }
        }
        return minDst;
    }

    // Vérifie si le mouvement est valide (pas de mur ni ghost house)
    private boolean isValidMove(GameState s, Action a) {
        int dx = 0, dy = 0;
        switch(a) {
            case UP -> dy = -1; 
            case DOWN -> dy = 1;
            case LEFT -> dx = -1; 
            case RIGHT -> dx = 1;
            case NONE -> {}
        }
        int nx = s.pac.x() + dx;
        int ny = s.pac.y() + dy;
        // Gestion du tunnel pour la validation
        if (nx < 0 || nx >= s.maze.getWidth()) return true; 
        CellState cs = s.maze.getState(nx, ny);
        return cs != CellState.MUR && cs != CellState.MUR_PERMANENT && cs != CellState.GHOST_HOUSE;
    }

    // Retourne l'action opposée (pour l'anti-tremblement)
    private Action getOpposite(Action a) {
        switch (a) {
            case UP: return Action.DOWN;
            case DOWN: return Action.UP;
            case LEFT: return Action.RIGHT;
            case RIGHT: return Action.LEFT;
            default: return Action.NONE;
        }
    }
}