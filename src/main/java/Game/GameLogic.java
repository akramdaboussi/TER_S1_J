package Game;

import Model.CellState;
import Model.Maze;

public final class GameLogic {

  public static void step(GameState s, Action pacAction){
    // 1) Pac-Man : appliquer l’action si possible, sinon continuer tout droit
    moveOne(s.maze, s.pac, pacAction);

    // 2) Fantôme v1 : priorité fixe (UP -> LEFT -> DOWN -> RIGHT), sans 180°
    moveGhostNoRng(s.maze, s.blinky);

    // 3) Consommations & états
    if (s.pellets.eatSmall(s.pac.x, s.pac.y)) s.score += s.cfg.pelletScore;

    if (s.pellets.eatPower(s.pac.x, s.pac.y)) {
      s.score += s.cfg.powerScore;
      s.frightened = true;
      s.frightenedEndTick = s.tick + s.cfg.frightenedTicks;
    }
    if (s.frightened && s.tick >= s.frightenedEndTick) s.frightened = false;

    // 4) Collision Pac-Man / fantôme
    if (s.pac.x==s.blinky.x && s.pac.y==s.blinky.y){
      if (s.frightened){
        s.score += s.cfg.ghostScore1;
        // renvoyer blinky au spawn (ex: 10,10 à l’arrêt)
        s.blinky.x=10; s.blinky.y=10; s.blinky.dx=0; s.blinky.dy=0;
      } else {
        s.lives--;
        // reset positions simples (à ajuster)
        s.pac.x=1; s.pac.y=1; s.pac.dx=1; s.pac.dy=0;
        s.blinky.x=10; s.blinky.y=10; s.blinky.dx=0; s.blinky.dy=0;
      }
    }

    // 5) Fin de niveau
    if (s.pellets.remaining()==0) s.levelCleared = true;

    s.tick++;
  }

  private static void moveOne(Maze m, EntityPos e, Action a){
    int ndx=e.dx, ndy=e.dy;
    switch(a){
      case UP -> { ndx=0; ndy=-1; }
      case DOWN -> { ndx=0; ndy=1; }
      case LEFT -> { ndx=-1; ndy=0; }
      case RIGHT -> { ndx=1; ndy=0; }
      default -> {}
    }
    int nx=e.x+ndx, ny=e.y+ndy;
    if (isWalk(m, nx, ny)) { e.x=nx; e.y=ny; e.dx=ndx; e.dy=ndy; return; }
    // sinon tenter de continuer tout droit
    nx=e.x+e.dx; ny=e.y+e.dy;
    if (isWalk(m, nx, ny)) { e.x=nx; e.y=ny; }
  }

  private static void moveGhostNoRng(Maze m, EntityPos g){
    int[][] dirs = {{0,-1},{-1,0},{0,1},{1,0}}; // U-L-D-R
    for (int[] d: dirs){
      if (d[0]==-g.dx && d[1]==-g.dy) continue; // éviter demi-tour direct
      int nx=g.x+d[0], ny=g.y+d[1];
      if (isWalk(m, nx, ny)) { g.x=nx; g.y=ny; g.dx=d[0]; g.dy=d[1]; return; }
    }
    // sinon demi-tour forcé
    g.dx=-g.dx; g.dy=-g.dy; g.x+=g.dx; g.y+=g.dy;
  }

  private static boolean isWalk(Maze m, int x,int y){
    return m.getState(x,y)==CellState.SOL;
  }
}
