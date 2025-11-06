package Model;

import Generator.MazeGenerator;
import org.junit.jupiter.api.Test;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour les contraintes fondamentales de la classe Maze
 * et les propriétés de symétrie du générateur.
 */
public class MazeTest {

    private static final int DEFAULT_WIDTH = 28;
    private static final int DEFAULT_HEIGHT = 30;

    // --- TESTS DE LA CONTRAINTE D'INITIALISATION ---

    @Test
    void testMazeCreationWithEvenWidth() {
        // La création devrait réussir si la largeur est paire
        assertDoesNotThrow(() -> new Maze(DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    @Test
    void testMazeCreationFailsWithOddWidth() {
        // La création doit échouer si la largeur est impaire (contrainte Pac-Man pour la symétrie)
        assertThrows(IllegalArgumentException.class, () -> new Maze(29, DEFAULT_HEIGHT));
    }

    // --- TESTS DU GABARIT FIXE ---

    @Test
    void testTemplateSetsPermanentBorderWalls() {
        Maze maze = new Maze(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        maze.applyTemplate();
        
        // Vérifie qu'un coin est bien un MUR_PERMANENT (2)
        assertEquals(CellState.MUR_PERMANENT, maze.getState(0, 0), "Le coin (0,0) doit être un mur permanent.");
        
        // Vérifie qu'un élément central est initialement un SOL ou une GHOST_HOUSE
        // La position (4, 4) est dans la zone générée et doit être un MUR initial
        assertEquals(CellState.MUR, maze.getState(4, 4), "L'intérieur doit être un MUR avant génération.");
    }
    
    // --- TEST DE LA PROPRIÉTÉ DE SYMÉTRIE ---

    @Test
    void testGeneratorMaintainsSymmetry() {
        long seed = 42;
        Maze maze = new Maze(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        maze.applyTemplate();
        
        MazeGenerator generator = new MazeGenerator();
        generator.generate(maze, new Random(seed), 0.0); // 0.0 imperfection pour un labyrinthe simple

        int width = maze.getWidth();
        
        // Choisit une cellule dans la moitié gauche (x=3) et dans la zone générée (y=5).
        int x_left = 3;
        int y = 5;

        // Calcule la position symétrique dans la moitié droite
        int x_right = width - 1 - x_left;

        // Le statut (SOL, MUR, etc.) doit être identique entre les deux côtés.
        CellState stateLeft = maze.getState(x_left, y);
        CellState stateRight = maze.getState(x_right, y);

        assertEquals(stateLeft, stateRight, 
            "La cellule (" + x_left + "," + y + ") doit avoir le même état que sa symétrique (" + x_right + "," + y + ").");

        // On vérifie une autre paire : par exemple, près de la Ghost House
        int x_near_center = width / 2 - 5; // ex: 14 - 5 = 9
        int x_sym_near_center = width - 1 - x_near_center; // ex: 27 - 9 = 18

        CellState stateNearCenter = maze.getState(x_near_center, y);
        CellState stateSymNearCenter = maze.getState(x_sym_near_center, y);

        assertEquals(stateNearCenter, stateSymNearCenter, 
            "La cellule près du centre (" + x_near_center + "," + y + ") doit être symétrique à (" + x_sym_near_center + "," + y + ").");
    }
}