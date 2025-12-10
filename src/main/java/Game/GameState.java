package Game;

import Model.Maze;
import java.util.List;
import java.util.ArrayList;

public final class GameState {

    // Composants statiques du jeu 
    public final Maze maze; // Labyrinthe
    public final PelletField pellets; // Les pastilles
    public final GameConfig cfg; // Configuration du jeu

    // Composants dynamiques du jeu 
    private int tick = 0; // Compteur de temps (à chaque tick, les entités peuvent bouger)
    private int score = 0; // Score actuel
    private int lives = 3; // Vies restantes
    public boolean levelCleared = false; // Indique si le niveau est terminé

    // Positions du pacman
    public EntityPos pac;      

    // Fantômes
    public final Ghost blinky;   
    public final Ghost pinky;    
    public final Ghost inky;
    public final Ghost clyde;  

    // Liste pour itérer facilement sur tous les fantômes sans répéter le code
    public final List<Ghost> ghosts;

    // Directions de Pac-Man
    private Action currentDir = Action.NONE;      
    private Action desiredDir = Action.NONE;      

    // État du mode frightened
    private boolean frightened = false;
    private int frightenedEndTick = -1;

    // Gestion du freeze du jeu
    public int freezeTimer = 0; // Compteur pour figer le jeu
    public Ghost ghostToReset = null;

    /**
     * Initialise une nouvelle partie avec les positions de départ.
     */
    public GameState(Maze maze, PelletField pellets, GameConfig cfg,
                     EntityPos pacStart, EntityPos blinkyStart, EntityPos pinkyStart, EntityPos inkyStart, EntityPos clydeStart) {
        this.maze = maze;
        this.pellets = pellets;
        this.cfg = cfg;
        this.pac = pacStart;

        // Instanciation des fantômes avec leurs délais de sortie respectifs
        this.blinky = new Ghost(Ghost.Type.BLINKY, blinkyStart, 0);
        this.pinky = new Ghost(Ghost.Type.PINKY, pinkyStart, cfg.pinkyStartDelay);
        this.inky = new Ghost(Ghost.Type.INKY, inkyStart, cfg.inkyStartDelay);
        this.clyde = new Ghost(Ghost.Type.CLYDE, clydeStart, cfg.clydeStartDelay);
        
        // Création de la liste
        this.ghosts = new ArrayList<>();
        this.ghosts.add(blinky);
        this.ghosts.add(pinky);
        this.ghosts.add(inky);
        this.ghosts.add(clyde);
    }

    // Getters & Setters
    
    public int tick() { return tick; }
    public void setTick(int tick) { this.tick = tick; }

    public int score() { return score; }
    public void setScore(int score) { this.score = score; }

    public int lives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    // Gestion du mode frightened
    public boolean isFrightened() { return frightened; }
    public void setFrightened(boolean frightened) { this.frightened = frightened; }

    public int getFrightenedEndTick() { return frightenedEndTick; }
    public void setFrightenedEndTick(int frightenedEndTick) { this.frightenedEndTick = frightenedEndTick; }

    public void resetFrightened() {
        frightened = false;
        frightenedEndTick = -1;
    }

    // Gestion des directions
    public Action getCurrentDir() { return currentDir; }
    public void setCurrentDir(Action dir) { this.currentDir = dir; }

    public Action getDesiredDir() { return desiredDir; }
    public void setDesiredDir(Action dir) { this.desiredDir = dir; }

}