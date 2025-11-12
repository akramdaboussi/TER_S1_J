package Game.Api;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import Generator.MazeGenerator;
import Model.Maze;
import Game.*;

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
      // TODO: place mieux les spawns selon ton maze
      EntityPos pac = new EntityPos(1,1,1,0);
      EntityPos blinky = new EntityPos(10,10,0,0);

      GameState gs = new GameState(maze, pf, cfg, pac, blinky);
      String id = UUID.randomUUID().toString();
      GAMES.put(id, gs);

      res.type("application/json");
      return GSON.toJson(Map.of("gameId", id));
    });

    get("/api/game/:id/state", (req,res)->{
      GameState gs = GAMES.get(req.params(":id"));
      if (gs==null){ res.status(404); return "game not found"; }
      res.type("application/json");
      return GSON.toJson(stateDto(gs));
    });

    post("/api/game/:id/step", (req,res)->{
      GameState gs = GAMES.get(req.params(":id"));
      if (gs==null){ res.status(404); return "game not found"; }
      Map<String,Object> body = null;
      try {
          @SuppressWarnings("unchecked")
          Map<String,Object> tmp = (Map<String,Object>) GSON.fromJson(req.body(), Map.class);
          body = tmp;
      } catch (Exception ignore) {}

      String actStr = "NONE";
      if (body != null) {
          Object v = body.getOrDefault("action", "NONE");
          actStr = String.valueOf(v);
      }
      Action a = Action.valueOf(actStr);
      GameLogic.step(gs, a);

      res.type("application/json");
      return GSON.toJson(stateDto(gs));
    });
  }

  private static Map<String,Object> stateDto(GameState s){
    return Map.of(
      "tick", s.tick,
      "score", s.score,
      "lives", s.lives,
      "levelCleared", s.levelCleared,
      "pac", Map.of("x",s.pac.x,"y",s.pac.y,"dx",s.pac.dx,"dy",s.pac.dy),
      "blinky", Map.of("x",s.blinky.x,"y",s.blinky.y,"dx",s.blinky.dx,"dy",s.blinky.dy),
      "pelletsRemaining", s.pellets.remaining()
    );
  }
}
