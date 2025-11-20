package Game;

import Model.*;

public final class GameLogic {

  public static void step(GameState s, Action pacAction){
    // Pac-Man : appliquer l’action si possible, sinon continuer tout droit
    moveOne(s.maze, s.pac, pacAction);
    // Fantôme v1 : priorité fixe 
    moveGhostNoRng(s.maze, s.blinky);
    // Consommations & états
    handlePelletConsumption(s);
    // Gestion de la fin de l'état Frightened
    if (s.isFrightened() && s.tick() >= s.getFrightenedEndTick()) {
        s.resetFrightened();
    }
    // Collision Pac-Man / fantôme
    handleGhostCollision(s);
    // Fin de niveau
    if (s.pellets.remaining()==0) s.levelCleared = true;
    // Incrémentation du tick
    s.setTick(s.tick() + 1);
  }

  /**
   * Gère la consommation des pellets et l'activation de l'état Frightened
   */
  private static void handlePelletConsumption(GameState s) {
    if (s.pellets.eatSmall(s.pac.x(), s.pac.y())) {
        s.setScore(s.score() + s.cfg.pelletScore);
    }

    if (s.pellets.eatPower(s.pac.x(), s.pac.y())) {
      s.setScore(s.score() + s.cfg.powerScore);
      s.setFrightened(true);
      s.setFrightenedEndTick(s.tick() + s.cfg.frightenedTicks);
    }
  }

  /**
   * Gère la collision Pac-Man / Fantôme
   */
  private static void handleGhostCollision(GameState s) {
    if (s.pac.x() == s.blinky.x() && s.pac.y() == s.blinky.y()){
      if (s.isFrightened()){
        // Fantôme mangé
        s.setScore(s.score() + s.cfg.ghostScore1); 
        resetEntityPosition(s.blinky, s.cfg.blinkySpawn);
      } else {
        // Pac-Man perd une vie
        s.setLives(s.lives() - 1);
        // Réinitialisation des positions (mort)
        resetEntityPosition(s.pac, s.cfg.pacSpawn);
        resetEntityPosition(s.blinky, s.cfg.blinkySpawn);
      }
    }
  }

  /**
   * Réinitialise une entité à sa position de départ
   */
  private static void resetEntityPosition(EntityPos e, EntityPos spawn) {
      e.setX(spawn.x());
      e.setY(spawn.y());
      e.setDx(spawn.dx());
      e.setDy(spawn.dy());
  }

  // --- Logique de Mouvement ---

  private static void moveOne(Maze m, EntityPos e, Action a){
    int ndx=e.dx(), ndy=e.dy();
    // Tenter de changer de direction
    switch(a){
      case UP -> { ndx=0; ndy=-1; }
      case DOWN -> { ndx=0; ndy=1; }
      case LEFT -> { ndx=-1; ndy=0; }
      case RIGHT -> { ndx=1; ndy=0; }
      default -> {}
    }
    int nx=e.x()+ndx, ny=e.y()+ndy;
    
    // Tenter le mouvement dans la nouvelle direction
    if (isWalk(m, nx, ny)) { 
      e.setX(nx); e.setY(ny); e.setDx(ndx); e.setDy(ndy); return; 
    }
    
    // Sinon tenter de continuer tout droit
    nx=e.x()+e.dx(); ny=e.y()+e.dy();
    if (isWalk(m, nx, ny)) { 
      e.setX(nx); e.setY(ny); 
    }
  }

  /**
   * Déplace le fantôme selon une priorité fixe (Haut-Gauche-Bas-Droite)
   */
  private static void moveGhostNoRng(Maze m, EntityPos g){
    int[][] dirs = {{0,-1},{-1,0},{0,1},{1,0}}; // U-L-D-R
    
    for (int[] d: dirs){
      // Permet d'éviter de faire demi-tour
      if (d[0]==-g.dx() && d[1]==-g.dy()) continue; 
      int nx=g.x()+d[0], ny=g.y()+d[1];

      if (isWalk(m, nx, ny)) { 
        g.setX(nx); g.setY(ny); g.setDx(d[0]); g.setDy(d[1]); return; 
      }
    }
    // Sinon, demi-tour forcé
    g.setDx(-g.dx()); g.setDy(-g.dy()); 
    g.setX(g.x() + g.dx()); g.setY(g.y() + g.dy());
  }

  /**
   * Vérifie si la cellule aux coordonnées (x,y) est un chemin praticable
   */
  private static boolean isWalk(Maze m, int x,int y){
    CellState state = m.getState(x,y);
    return state == CellState.SOL || state == CellState.TUNNEL;
  }
}