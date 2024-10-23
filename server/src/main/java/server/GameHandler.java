package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import service.GameService;
import service.UserService;
import responses.GameListResult;
import spark.Request;
import spark.Response;

import java.util.List;

public class GameHandler {
    private final UserService userService;
    private final GameService gameService;
    private final Gson serializer = new Gson();

    public GameHandler(UserService userService, GameService gameService) {
        this.userService = userService;

        this.gameService = gameService;
    }

    public Object listGames(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");

        // Fetch the list of games
        List<GameData> games = gameService.listGames(authToken);

        // Serialize and return the games list as a JSON response
        return serializer.toJson(new GameListResult(games));
    }
}
