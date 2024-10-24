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

        AuthData authData = dataAccess.getAuth(authToken); //this is the problem line
        System.out.println("Current Auth Data: " + authData);

        if (authData == null){
            throw new BadRequestException("this if from the auth");
        }
        return dataAccess.createGame(gameName, authData.authToken()); //FIX THIS
    }
    public void joinGame(String playerColor, int gameID, String authToken) throws DataAccessException {
        userService.verifyAuth(authToken);
        //now i have authdata
        AuthData authData = dataAccess.getAuth(authToken);
        GameData gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Game not found");
        }
        if (playerColor.equals("WHITE") && gameData.whiteUsername() != null) {
            throw new DataAccessException("White player already joined");
        } else if (playerColor.equals("BLACK") && gameData.blackUsername() != null) {
            throw new DataAccessException("Black player already joined");
        }
        if (playerColor.equals("WHITE")) {
            gameData.withWhiteUsername(authData.username());
        } else if (playerColor.equals("BLACK")) {
            gameData.withBlackUsername(authData.username());
        }
    }

}
