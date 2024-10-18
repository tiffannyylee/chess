package dataaccess;

import model.AuthData;
import model.UserData;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authentication = new HashMap<>();

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
}
