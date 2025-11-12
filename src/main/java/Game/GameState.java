package Game;

import Model.Maze;

public final class GameState {
  public final Maze maze;
  public final PelletField pellets;
  public final GameConfig cfg;

  public int tick=0;
  public int score=0;
  public int lives=3; 
  public boolean levelCleared=false;

  public EntityPos pac;     // Pac-Man
  public EntityPos blinky;  // premier fantome pour test (le rouge)

  public boolean frightened=false;
  public int frightenedEndTick=-1;

  public GameState(Maze maze, PelletField pellets, GameConfig cfg,
                   EntityPos pacStart, EntityPos blinkyStart){
    this.maze=maze; this.pellets=pellets; this.cfg=cfg;
    this.pac=pacStart; this.blinky=blinkyStart;
  }
}
