package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import model.GameData;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {this.dataAccess = dataAccess;}

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
