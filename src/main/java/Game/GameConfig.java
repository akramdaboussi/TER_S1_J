package Game;

/**
 * Configuration globale du jeu.
 * Contient les constantes d'équilibrage (vitesse, scores, délais).
 */
public final class GameConfig {

  // Vitesse de déplacement en cellules par seconde
  public int tickPerSecond = 8;
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

  // Positions de départ 
  public final EntityPos pacSpawn;

  public final EntityPos blinkySpawn;
  public final EntityPos pinkySpawn;
  public final EntityPos inkySpawn;
  public final EntityPos clydeSpawn;

  // Position de sortie de la ghost house
  public final EntityPos houseExitTarget;

  public GameConfig(int w, int h) {
    // On récupère le centre du labyrinthe
    int centerX = w / 2;
    int centerY = h / 2;

    int tunnelY = h / 2;
    if (tunnelY % 2 != 0) tunnelY++;

    // Initialisation de la position du Pac-Man
    this.pacSpawn = new EntityPos(1, tunnelY, 1, 0);

    // Initialisation des positions des fantômes
    this.blinkySpawn = new EntityPos(centerX, centerY - 1, 0, 0);
    this.pinkySpawn = new EntityPos(centerX, centerY + 1, 0, 0);
    this.inkySpawn = new EntityPos(centerX - 2, centerY - 1, 0, 0);
    this.clydeSpawn = new EntityPos(centerX + 2, centerY - 1, 0, 0);

    this.houseExitTarget = new EntityPos(centerX, centerY - 3, 0, 0);
  }
}
