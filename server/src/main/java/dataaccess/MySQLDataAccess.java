package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public class MySQLDataAccess implements DataAccess {
    @Override
    public UserData createUser(UserData userData) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        return null;
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData deleteAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public List<GameData> getGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public GameData createGame(String gameName, String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        return null;
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {

    }
}
