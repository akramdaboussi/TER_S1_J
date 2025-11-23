package Game;

import Model.Maze;

public final class GameState {
    public final Maze maze;
    public final PelletField pellets;
    public final GameConfig cfg;

    private int tick = 0;
    private int score = 0;
    private int lives = 3;
    public boolean levelCleared = false;

    // Positions des entités
    public EntityPos pac;      // Pac-Man
    public EntityPos blinky;   // premier fantôme

    // deplacements
    private Action currentDir = Action.NONE;      // direction actuelle
    private Action desiredDir = Action.NONE;      // direction voulue (clavier)

    // Effet frightened (sera mis quand on aura les power pellets)
    private boolean frightened = false;
    private int frightenedEndTick = -1;

    public GameState(Maze maze, PelletField pellets, GameConfig cfg,
                     EntityPos pacStart, EntityPos blinkyStart) {
        this.maze = maze;
        this.pellets = pellets;
        this.cfg = cfg;
        this.pac = pacStart;
        this.blinky = blinkyStart;
    }

    // Getters & Setters
    public int tick() { return tick; }
    public int score() { return score; }
    public int lives() { return lives; }
    public boolean isFrightened() { return frightened; }
    public int getFrightenedEndTick() { return frightenedEndTick; }

    public void setTick(int tick) { this.tick = tick; }
    public void setScore(int score) { this.score = score; }
    public void setLives(int lives) { this.lives = lives; }
    public void setFrightened(boolean frightened) { this.frightened = frightened; }
    public void setFrightenedEndTick(int frightenedEndTick) { this.frightenedEndTick = frightenedEndTick; }

    // getters pour affichage
    public EntityPos pacmanPos() { return pac; }
    public EntityPos blinkyPos() { return blinky; }
    public PelletField pellets() { return pellets; }

    // deplacements
    public Action getCurrentDir() { return currentDir; }
    public Action getDesiredDir() { return desiredDir; }

    public void setCurrentDir(Action dir) { this.currentDir = dir; }
    public void setDesiredDir(Action dir) { this.desiredDir = dir; }

    // Réinitialisation de l'état frightened
    public void resetFrightened() {
        frightened = false;
        frightenedEndTick = -1;
    }
}
