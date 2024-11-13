package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import model.GameData;

import java.util.List;

public class GameService {
    private final DataAccess dataAccess;
    private final UserService userService;

    public GameService(UserService userService, DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.userService = userService;
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        userService.verifyAuth(authToken);

        List<GameData> games = dataAccess.getGames();

        return games;
    }

    public GameData createGame(String gameName, String authToken) throws DataAccessException {
        userService.verifyAuth(authToken);
        System.out.println("Using Auth Token: " + authToken);

        AuthData authData = dataAccess.getAuth(authToken);
        System.out.println("Current Auth Data: " + authData);

        if (authData == null) {
            throw new BadRequestException("this if from the auth");
        }
        List<GameData> games = dataAccess.getGames();
        for (GameData game : games) {
            if (game.gameName().equalsIgnoreCase(gameName)) {
                throw new BadRequestException("A game with this name already exists");
            }
        }
        return dataAccess.createGame(gameName, authData.authToken());
    }

    public void joinGame(String playerColor, int gameID, String authToken) throws DataAccessException {
        userService.verifyAuth(authToken);

        AuthData authData = dataAccess.getAuth(authToken);
        GameData gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            throw new BadRequestException("Game not found");
        }
        if (gameData.whiteUsername() != null && playerColor.equals("WHITE") || gameData.blackUsername() != null && playerColor.equals("BLACK")) {
            throw new UserAlreadyExistsException("This game already has a user this color");
        }
        if (playerColor.equals("WHITE")) {
            gameData = gameData.withWhiteUsername(authData.username());
        } else if (playerColor.equals("BLACK")) {
            gameData = gameData.withBlackUsername(authData.username());
        }
        dataAccess.updateGame(gameData);
    }

}
