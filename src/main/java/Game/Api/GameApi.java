package Game.Api;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import Generator.MazeGenerator;
import Model.Maze;
import Game.*;
import Game.GameStateResponse;

public final class GameApi {
  private static final Map<String,GameState> GAMES = new ConcurrentHashMap<>();
  private static final Gson GSON = new Gson();

  public static void mount(){
    post("/api/game/start", (req,res)->{
      // génère un maze 
      Maze maze = new Maze(28,31);
      new MazeGenerator().generate(maze, new Random());

      PelletField pf = PelletPlacer.place(maze);
      GameConfig cfg = new GameConfig();

      EntityPos pac = cfg.pacSpawn;
      EntityPos blinky = cfg.blinkySpawn;

      GameState gs = new GameState(maze, pf, cfg, pac, blinky);
      String id = UUID.randomUUID().toString();
      GAMES.put(id, gs);

      res.type("application/json");
      return GSON.toJson(stateDto(gs, id));
    });

    // --- Obtenir l'état de la partie ---
    get("/api/game/:id/state", (req,res)->{
      String gameId = req.params(":id");
      GameState gs = GAMES.get(gameId);
      if (gs==null){ 
        res.status(404); 
        return "game not found"; 
      }
      res.type("application/json");
      return GSON.toJson(stateDto(gs, gameId));
    });

    // --- Effectuer un pas de jeu ---
    post("/api/game/:id/step", (req,res)->{
      String gameId = req.params(":id");
      GameState gs = GAMES.get(gameId);
      if (gs==null){ 
        res.status(404); 
        return "game not found"; 
      }
      Action action;
      try {
        String actionStr = GSON.fromJson(req.body(), String.class);
        action = Action.valueOf(actionStr.toUpperCase());
      } catch (Exception e) {
        action = Action.NONE; // Utilise NONE en cas d'échec de parsing
      }
      gs.setDesiredDir(action);
      GameLogic.step(gs);

      res.type("application/json");
      return GSON.toJson(stateDto(gs, gameId));
    });
  }

  /**
   * Construit le DTO de réponse à partir du GameState.
   */
  private static GameStateResponse stateDto(GameState s, String gameId){
    return new GameStateResponse(
      gameId,
      s.tick(),
      s.score(),
      s.lives(),
      s.levelCleared,
      s.isFrightened(),
      Map.of("x",s.pac.x(),"y",s.pac.y(),"dx",s.pac.dx(),"dy",s.pac.dy()),
      Map.of("x",s.blinky.x(),"y",s.blinky.y(),"dx",s.blinky.dx(),"dy",s.blinky.dy()),
      s.pellets.remaining(),
      s.maze.getMazeData(), 
      s.pellets.getSmall(),
      s.pellets.getPower()
    );
  }
}
