package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import model.GameData;

public class GameService {
    private final DataAccess dataAccess;
    private final UserService userService;

    public GameService(UserService userService, DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.userService = userService;
    }

    public GameData listGames(String authToken) {
        return null;
    }
    public GameData createGame(String gameName, String authToken){
        return null;
    }
    public void joinGame(String playerColor, int gameID, String authToken){
        return;
    }

}
