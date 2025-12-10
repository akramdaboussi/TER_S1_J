package Game;

import Model.MazeData;
import java.util.Map;

/**
 * DTO représentant l'état du jeu tel qu'il est renvoyé par l'API Cloud
 * Il est utilisé à la fois par le serveur (pour la construction) et par le client (pour le parsing).
*/
public record GameStateResponse(
        String gameId,
        int tick,
        int score,
        int lives,
        boolean levelCleared,
        boolean isFrightened,
        Map<String, Integer> pac,       // Position de Pac-Man (x, y)
        Map<String, Integer> blinky,    // Position de Blinky (x, y)
        Map<String, Integer> pinky,     // Position de Pinky (x, y)
        Map<String, Integer> inky,      // Position d'Inky (x, y)
        Map<String, Integer> clyde,     // Position de Clyde (x, y)
        int pelletsRemaining,
        MazeData mazeData,              // Labyrinthe (peut être null après le start)
        boolean[][] smallPellets,       // Carte des petites pastilles
        boolean[][] powerPellets        // Carte des power pellets
) {}