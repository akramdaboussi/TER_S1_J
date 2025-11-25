package Game;

import Model.CellState;
import Model.Maze;

public final class PelletPlacer {
  public static PelletField place(Maze m){
    int w=m.getWidth(), h=m.getHeight();
    PelletField pf = new PelletField(w,h);

    // petites pellets sur les SOL
    for(int y=0;y<h;y++)
      for(int x=0;x<w;x++)
        if (m.getState(x,y)==CellState.SOL || m.getState(x,y)==CellState.TUNNEL){
          pf.small[y][x]=true;
        } else {
          pf.small[y][x]=false;
        }
    

    // 4 power pellets avec placement conditionnel, on modifiera plus tard pour un placement plus intelligent
    markPowerIfWalkable(pf,m,2,3);
    markPowerIfWalkable(pf,m,w-3,3);
    markPowerIfWalkable(pf,m,2,h-4);
    markPowerIfWalkable(pf,m,w-3,h-4);

    return pf;
  }

  private static void markPowerIfWalkable(PelletField pf, Maze m, int x,int y){
    if (m.getState(x,y)==CellState.SOL || m.getState(x,y)==CellState.TUNNEL){
      pf.small[y][x]=false;
      pf.power[y][x]=true;
    }
  }
}
