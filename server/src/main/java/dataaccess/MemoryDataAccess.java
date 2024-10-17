package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authentication = new HashMap<>();

    @Override
    public UserData createUser(UserData userData) throws DataAccessException {
        if (users.containsKey(userData.username())) {
            throw new DataAccessException("User already exists");
        }
        users.put(userData.username(), userData);
        return userData;
    }

    @Override
    public AuthData createAuth(AuthData authData) throws DataAccessException {
        if (authentication.containsKey(authData.authToken())){
            throw new DataAccessException("Auth code already exists");
        }
        authentication.put(authData.authToken(), authData);
        return authData;
    }

    @Override
    public UserData getUser(UserData username) throws DataAccessException {
        UserData user = users.get(username.username());
        if (user == null) {
            throw new DataAccessException("User not found");
        }
        return user;
    }
}
