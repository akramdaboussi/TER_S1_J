package Game;

import Model.Maze;

public final class GameState {
  public final Maze maze;
  public final PelletField pellets;
  public final GameConfig cfg;

  private int tick=0;
  private int score=0;
  private int lives=3; 
  public boolean levelCleared=false;

  public EntityPos pac;     // Pac-Man
  public EntityPos blinky;  // premier fantome pour test (le rouge)

  private boolean frightened=false;
  private int frightenedEndTick=-1;

  public GameState(Maze maze, PelletField pellets, GameConfig cfg, EntityPos pacStart, EntityPos blinkyStart){
    this.maze=maze; this.pellets=pellets; this.cfg=cfg;
    this.pac=pacStart; this.blinky=blinkyStart;
  }

  // Getters et Setters
  public int tick(){ return tick; }
  public int score(){ return score; }
  public int lives(){ return lives; }
  public boolean isFrightened(){ return frightened; }
  public int getFrightenedEndTick(){ return frightenedEndTick; }

  public void setTick(int tick) { this.tick = tick; }
  public void setScore(int score) { this.score = score; }
  public void setLives(int lives) { this.lives = lives; }
  public void setFrightened(boolean frightened) { this.frightened = frightened; }
  public void setFrightenedEndTick(int frightenedEndTick) { this.frightenedEndTick = frightenedEndTick; }
  
  // Méthode utilitaire pour réinitialiser l'état Frightened
  public void resetFrightened() {
      this.frightened = false;
      this.frightenedEndTick = -1;
  }

}
