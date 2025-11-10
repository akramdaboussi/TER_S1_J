package Model;

/*
 * Représente la structure de données simple du labyrinthe pour la conversion JSON
*/

public record MazeData(String ident, int width, int height, int[][] grid) {}