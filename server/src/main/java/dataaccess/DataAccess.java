package dataaccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {
    UserData createUser(UserData userData) throws DataAccessException;
    AuthData createAuth(String username) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
}
