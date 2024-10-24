package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public interface DataAccess {
    UserData createUser(UserData userData) throws DataAccessException;
    AuthData createAuth(String username) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    AuthData deleteAuth(String authToken) throws DataAccessException;
    List<GameData> getGames() throws DataAccessException;
    GameData createGame(String gameName, String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
    GameData getGame(int gameId) throws DataAccessException;
    void updateGame(GameData gameData) throws DataAccessException;
}
