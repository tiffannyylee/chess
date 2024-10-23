package service;

import dataaccess.BadRequestException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.UserAlreadyExistsException;
import model.AuthData;
import model.UserData;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {this.dataAccess = dataAccess;}

    public AuthData register(UserData newUser) throws DataAccessException {
        if (newUser.username() == null || newUser.password() == null || newUser.email() == null) {
            throw new BadRequestException("Error: bad request");
        }
        if (dataAccess.getUser(newUser.username())!=null){
            throw new UserAlreadyExistsException("Error: already taken");
        }
        dataAccess.createUser(newUser);
        return dataAccess.createAuth(newUser.username());
    }

    public AuthData login(UserData user) {

    }
    //public void logout(AuthData auth) {}
}
