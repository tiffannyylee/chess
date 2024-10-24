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
    public GameData createGame(String gameName, String authToken){
        return null;
    }
    public void joinGame(String playerColor, int gameID, String authToken){
        return;
    }

}
