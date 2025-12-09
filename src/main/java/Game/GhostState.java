package Game;

public enum GhostState {
    IN_HOUSE,    // Le fantôme est dans la ghost house et en attente/en mouvement interne.
    EXITING,     // Le fantôme se dirige vers la sortie.
    CHASE        // Le fantôme poursuit Pac-Man.
}