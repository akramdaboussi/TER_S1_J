package Game;

/**
 * Configuration globale du jeu.
 * Contient les constantes d'équilibrage (vitesse, scores, délais).
 */
public final class GameConfig {

  // Vitesse de déplacement en cellules par seconde
  public int tickPerSecond = 7;
  public int frightenedTicks = 6 * tickPerSecond;

  // Scores
  public int pelletScore = 10; // score pour une petite pastille
  public int powerScore  = 50; // score pour une power pellet
  public int ghostScore1 = 200; // score pour le 1er fantome mangé

  // Délais de sortie des fantômes (en ticks) (Blinky sort immédiatement)
  public final int pinkyStartDelay = 40;
  public final int inkyStartDelay = 80;
  public final int clydeStartDelay = 120;
  
  public final int respawnDelay = 50; // Temps d'attente dans la maison après s'être fait manger

  // Positions de départ (Pac-Man est positionné dynamique dans Localclient)
  public final EntityPos blinkySpawn = new EntityPos(14, 14, 0, 0);
  public final EntityPos pinkySpawn = new EntityPos(16, 14, 0, 0);
  public final EntityPos inkySpawn = new EntityPos(12, 14, 0, 0);
  public final EntityPos clydeSpawn = new EntityPos(14, 16, 0, 0);

  // Position de sortie de la ghost house
  public final EntityPos houseExitTarget = new EntityPos(14, 11, 0, 0);
}
