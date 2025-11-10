package Model;

import java.util.List;

/*
 * Représente la structure de données simple du labyrinthe pour la conversion JSON
*/

public record MazeData(String ident, int width, int height, List<List<Integer>> grid) {}