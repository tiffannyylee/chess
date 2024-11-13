package server;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import model.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
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

        List<GameData> games = gameService.listGames(authToken);
        response.type("application/json");
        response.status(200);
        return serializer.toJson(new GameListResult(games));
    }

    public Object createGame(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");
        CreateGameRequest createGameRequest = serializer.fromJson(request.body(), CreateGameRequest.class);
        GameData createdGame = gameService.createGame(createGameRequest.gameName(), authToken);
        response.type("application/json");
        response.status(200);
        return serializer.toJson(createdGame);
    }

    public Object joinGame(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");
        JoinGameRequest joinGameRequest = serializer.fromJson(request.body(), JoinGameRequest.class);
        String playerColor = joinGameRequest.playerColor();
        int gameId = joinGameRequest.gameID();
        if (playerColor == null) {
            throw new BadRequestException("player color is null");
        }
        gameService.joinGame(playerColor, gameId, authToken);
        response.status(200);
        return "";
    }
}
