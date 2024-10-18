package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {this.dataAccess = dataAccess;}

    public UserData register(UserData newUser) throws DataAccessException {
        if (dataAccess.getUser(newUser)!=null){
            throw new DataAccessException("User already exists");
        }
        dataAccess.createUser(newUser);
        return newUser;
    }
   // public AuthData login(UserData user) {}
    //public void logout(AuthData auth) {}
}
