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
    public EntityPos pac;      
    public EntityPos blinky;   
    public EntityPos pinky;    
    public EntityPos inky;
    public EntityPos clyde;  

    // États des fantômes
    private GhostState blinkyState;
    private GhostState pinkyState;
    private GhostState inkyState;
    private GhostState clydeState;

    // Timers pour gérer la sortie de la ghost house
    private int blinkyReleaseTick = 0;
    private int pinkyReleaseTick = 0;
    private int inkyReleaseTick = 0;
    private int clydeReleaseTick = 0;

    private Action currentDir = Action.NONE;      
    private Action desiredDir = Action.NONE;      

    private boolean frightened = false;
    private int frightenedEndTick = -1;

    public GameState(Maze maze, PelletField pellets, GameConfig cfg,
                     EntityPos pacStart, EntityPos blinkyStart, EntityPos pinkyStart, EntityPos inkyStart, EntityPos clydeStart) {
        this.maze = maze;
        this.pellets = pellets;
        this.cfg = cfg;
        this.pac = pacStart;
        this.blinky = blinkyStart;
        this.pinky = pinkyStart;
        this.inky = inkyStart;
        this.clyde = clydeStart;
        
        // Initialisation des états à IN_HOUSE
        this.blinkyState = GhostState.IN_HOUSE;
        this.pinkyState = GhostState.IN_HOUSE;
        this.inkyState = GhostState.IN_HOUSE;
        this.clydeState = GhostState.IN_HOUSE;

        // Délais de sortie initiaux 
        this.blinkyReleaseTick = 0;   // Sort immédiatement
        this.pinkyReleaseTick = 40;   // Sort après 5s 
        this.inkyReleaseTick = 80;   // Sort après 10s
        this.clydeReleaseTick = 120; // Sort après 15s
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

    public EntityPos pacmanPos() { return pac; }
    public EntityPos blinkyPos() { return blinky; }
    public EntityPos pinkyPos() { return pinky; }
    public EntityPos inkyPos() { return inky; }
    public EntityPos clydePos() { return clyde; }
    public PelletField pellets() { return pellets; }

    public Action getCurrentDir() { return currentDir; }
    public Action getDesiredDir() { return desiredDir; }
    public void setCurrentDir(Action dir) { this.currentDir = dir; }
    public void setDesiredDir(Action dir) { this.desiredDir = dir; }

    // Gestion des États
    public GhostState getBlinkyState() { return blinkyState; }
    public GhostState getPinkyState() { return pinkyState; }
    public GhostState getInkyState() { return inkyState; }
    public GhostState getClydeState() { return clydeState; }

    public void setBlinkyState(GhostState state) { this.blinkyState = state; }
    public void setPinkyState(GhostState state) { this.pinkyState = state; }
    public void setInkyState(GhostState state) { this.inkyState = state; }
    public void setClydeState(GhostState state) { this.clydeState = state; }

    // Gestion des Timers de sortie
    public int getBlinkyReleaseTick() { return blinkyReleaseTick; }
    public int getPinkyReleaseTick() { return pinkyReleaseTick; }
    public int getInkyReleaseTick() { return inkyReleaseTick; }
    public int getClydeReleaseTick() { return clydeReleaseTick; }

    public void setBlinkyReleaseTick(int t) { this.blinkyReleaseTick = t; }
    public void setPinkyReleaseTick(int t) { this.pinkyReleaseTick = t; }
    public void setInkyReleaseTick(int t) { this.inkyReleaseTick = t; }
    public void setClydeReleaseTick(int t) { this.clydeReleaseTick = t; }

    public void resetFrightened() {
        frightened = false;
        frightenedEndTick = -1;
    }
}