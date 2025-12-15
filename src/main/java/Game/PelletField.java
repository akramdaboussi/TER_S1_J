package Game;

public final class PelletField {
  final boolean[][] small; // pellet simple
  final boolean[][] power; // power pellet

  public PelletField(int w, int h){ small=new boolean[h][w]; power=new boolean[h][w]; }

  // Crée une copie du PelletField
  public PelletField copy() {
    PelletField newField = new PelletField(small[0].length, small.length);
    for(int y=0; y<small.length; y++) {
        System.arraycopy(this.small[y], 0, newField.small[y], 0, this.small[y].length);
        System.arraycopy(this.power[y], 0, newField.power[y], 0, this.power[y].length);
    }
    return newField;
  }

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

  public boolean[][] getSmall(){ return small; }
  public boolean[][] getPower(){ return power; }
}
