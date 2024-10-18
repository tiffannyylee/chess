package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {this.dataAccess = dataAccess;}

    public AuthData register(UserData newUser) throws DataAccessException {
        if (dataAccess.getUser(newUser.username())!=null){
            throw new DataAccessException("User already exists");
        }
        dataAccess.createUser(newUser);
        return dataAccess.createAuth(newUser.username());
    }

   // public AuthData login(UserData user) {}
    //public void logout(AuthData auth) {}
}
