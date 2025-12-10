package Game;

/**
 * Représente un fantôme individuel.
 * Contient son état, sa position et son type (qui définit son IA).
 */
public class Ghost {
    
    // Identifiant du fantôme pour différencier les comportements (IA)
    public enum Type { BLINKY, PINKY, INKY, CLYDE }

    public final Type type;
    public final EntityPos pos; // Position actuelle
    public final EntityPos spawnPos; // Point de départ 
    
    private GhostState state; // État courant du fantôme
    private int releaseTick; // Moment où il sort de la maison

    // Crée un nouveau fantôme
    public Ghost(Type type, EntityPos startPos, int releaseTick) {
        this.type = type;
        // Copie de la position de départ
        this.spawnPos = new EntityPos(startPos.x(), startPos.y(), startPos.dx(), startPos.dy());
        this.pos = startPos; 
        this.releaseTick = releaseTick;
        this.state = GhostState.IN_HOUSE;
    }

    // Réinitialise le fantôme à sa position de départ 
    public void reset() {
        this.pos.setX(spawnPos.x());
        this.pos.setY(spawnPos.y());
        this.pos.setDx(0);
        this.pos.setDy(0);
        this.state = GhostState.IN_HOUSE;
    }

    // Getters et Setters
    public GhostState getState() { return state; }
    public void setState(GhostState state) { this.state = state; }

    public int getReleaseTick() { return releaseTick; }
    public void setReleaseTick(int releaseTick) { this.releaseTick = releaseTick; }
}