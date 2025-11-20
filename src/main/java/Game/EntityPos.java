package Game;

public final class EntityPos {
  private int x,y;      // cellule
  private int dx,dy;    // direction courante

  public EntityPos(int x,int y,int dx,int dy){ 
    this.x=x; this.y=y; this.dx=dx; this.dy=dy; 
  }

  // Getters et Setters  
  public int x(){ return x; }
  public int y(){ return y; }
  public int dx(){ return dx; }
  public int dy(){ return dy; }

  public void setX(int x){ this.x=x; }
  public void setY(int y){ this.y=y; }
  public void setDx(int dx){ this.dx=dx; }
  public void setDy(int dy){ this.dy=dy; }
}

