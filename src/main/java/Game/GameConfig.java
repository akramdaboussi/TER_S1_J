package Game;

public final class GameConfig {
  public int tickPerSecond = 8;
  public int frightenedTicks = 6 * tickPerSecond;

  public int pelletScore = 10;
  public int powerScore  = 50;
  public int ghostScore1 = 200; // score pour le 1er fantome mangé

  public final int pinkyStartDelay = 50;
  public final int inkyStartDelay = 100;
  
  // Temps d'attente dans la maison après s'être fait manger
  public final int respawnDelay = 50;

  public final EntityPos pacSpawn = new EntityPos(0, 16, 1, 0); 
  public final EntityPos blinkySpawn = new EntityPos(14, 14, 0, 0);
  public final EntityPos pinkySpawn = new EntityPos(16, 14, 0, 0);
  public final EntityPos inkySpawn = new EntityPos(12, 14, 0, 0);

  public final EntityPos houseExitTarget = new EntityPos(14, 11, 0, 0);
}
