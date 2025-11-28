package Game;

public final class GameConfig {
  public int tickPerSecond = 10;
  public int frightenedTicks = 6 * tickPerSecond;

  public int pelletScore = 10;
  public int powerScore  = 50;
  public int ghostScore1 = 200; // score pour le 1er fantome mangé

  public final EntityPos pacSpawn = new EntityPos(0, 16, 1, 0); 
  public final EntityPos blinkySpawn = new EntityPos(14, 14, 0, 0);
}
