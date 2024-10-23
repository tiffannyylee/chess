package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;


import javax.xml.crypto.Data;
import java.util.*;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authentication = new HashMap<>();
    private final Map<String, GameData> games = new HashMap<>();

    @Override
    public UserData createUser(UserData userData) throws DataAccessException {
        if (users.containsKey(userData.username())) {
            throw new DataAccessException("User already exists");
        }

        // Add the new user to the map
        users.put(userData.username(), userData);
        return userData;
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authentication.put(authToken, authData);
        return authData;
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authentication.get(authToken);
    }

    @Override
    public AuthData deleteAuth(String authToken) throws DataAccessException{
        authentication.remove(authToken);
        return null;
    }

    @Override
    public List<GameData> getGames() {
        return new ArrayList<>(games.values());
    }
}
