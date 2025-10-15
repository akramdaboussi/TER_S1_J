package Generator;

import Model.Point;
/*
 * Représentation immuable d'une paire de murs symétriques.
 * Contient le mur, son symétrique, et les 4 cellules qu'ils séparent.
*/
public record WallPair(Point wall, Point symWall, Point cell1, Point cell2, Point symCell1, Point symCell2) {}