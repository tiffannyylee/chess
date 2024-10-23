package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import requests.LoginRequest;

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

    public AuthData login(UserData user) throws DataAccessException {
        if (user.username()==null||user.password()==null){
            throw new UnauthorizedException("Error: unauthorized");
        }
        UserData storedUser = dataAccess.getUser(user.username());
        if (storedUser == null) {
            throw new UnauthorizedException("Error: unauthorized"); // Ensure this is thrown for non-existent users
        }
        if (!user.password().equals(storedUser.password())){
            throw new UnauthorizedException("Error: unauthorized");
        }
        return dataAccess.createAuth(user.username());
    }
    //public void logout(AuthData auth) {}
}
