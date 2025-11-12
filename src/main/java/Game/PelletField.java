package Game;

public final class PelletField {
  final boolean[][] small; // pellet simple
  final boolean[][] power; // power pellet

  public PelletField(int w, int h){ small=new boolean[h][w]; power=new boolean[h][w]; }
  public boolean hasSmall(int x,int y){ return small[y][x]; }
  public boolean hasPower(int x,int y){ return power[y][x]; }

  /** @return true si une petite pellet a été mangée ici */
  public boolean eatSmall(int x,int y){
    if(small[y][x]){ small[y][x]=false; return true; }
    return false;
  }
  /** @return true si une power pellet a été mangée ici */
  public boolean eatPower(int x,int y){
    if(power[y][x]){ power[y][x]=false; return true; }
    return false;
  }

  public int remaining(){ // nombre de pellets restantes
    int r=0;
    for (int y=0;y<small.length;y++)
      for (int x=0;x<small[0].length;x++)
        if (small[y][x] || power[y][x]) r++;
    return r;
  }
}
