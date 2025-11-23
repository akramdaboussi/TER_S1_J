package Game;

import Model.*;
import View.MazeVisualizerPanel;

import javax.swing.*;
import java.awt.*;

public final class GameLauncher {

    private static int[][] convertGrid(MazeData md) {
    int h = md.height();
    int w = md.width();
    int[][] g = new int[h][w];

    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
            g[y][x] = md.grid().get(y).get(x);
        }
    }
    return g;
}

private static EntityPos findSpawn(Maze maze) {
    for (int y = 0; y < maze.getHeight(); y++) {
        for (int x = 0; x < maze.getWidth(); x++) {
            if (maze.getState(x, y) == CellState.SOL) {
                return new EntityPos(x, y, 0, 0); // spawn 
            }
        }
    }
    throw new IllegalStateException("Aucun spawn SOL trouvé dans le maze.");
}


    public static void launchLocalGame() {

        // generation maze en local
        int width = 28, height = 31;
        Maze maze = new Maze(width, height);
        maze.applyTemplate();
        new Generator.MazeGenerator().generate(maze, new java.util.Random());

        // placement pellets
        PelletField pellets = PelletPlacer.place(maze);

        // creation game state
        EntityPos pacSpawn = findSpawn(maze);
        EntityPos blinkySpawn = findSpawn(maze);  // provisoire, ils seront séparés plus tard

        GameConfig cfg = new GameConfig();
        GameState state = new GameState(maze, pellets, cfg, pacSpawn, blinkySpawn);

        // creation fenetre et panneau d'affichage
        JFrame frame = new JFrame("Pac-Man - Mode Local");
        int[][] grid = convertGrid(maze.getMazeData());
        MazeVisualizerPanel panel = new MazeVisualizerPanel(grid);
        panel.setGameState(state);

        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        
        panel.requestFocusInWindow(); // focus sur le panel pour le clavier

        // clavier
        panel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                switch (e.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_UP -> state.setDesiredDir(Action.UP);
                    case java.awt.event.KeyEvent.VK_DOWN -> state.setDesiredDir(Action.DOWN);
                    case java.awt.event.KeyEvent.VK_LEFT -> state.setDesiredDir(Action.LEFT);
                    case java.awt.event.KeyEvent.VK_RIGHT -> state.setDesiredDir(Action.RIGHT);
                }
            }
        });


        // boucle jeu (8 fps comme arcade)
        Timer timer = new Timer(120, ev -> {
            GameLogic.step(state);
            panel.repaint();
        });
        timer.start();

        
    }
    public static void main(String[] args) {
    launchLocalGame();
}
}


