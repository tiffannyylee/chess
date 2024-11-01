package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import requests.LoginRequest;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData newUser) throws DataAccessException {
        if (newUser.username() == null || newUser.password() == null || newUser.email() == null) {
            throw new BadRequestException("Error: bad request");
        }
        if (dataAccess.getUser(newUser.username()) != null) {
            throw new UserAlreadyExistsException("Error: already taken");
        }
        String hashedPassword = BCrypt.hashpw(newUser.password(), BCrypt.gensalt());
        newUser = new UserData(newUser.username(), hashedPassword, newUser.email()); // replace password with hashed version
        dataAccess.createUser(newUser);
        return dataAccess.createAuth(newUser.username());
    }

    public AuthData login(UserData user) throws DataAccessException {
        if (user.username() == null || user.password() == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        UserData storedUser = dataAccess.getUser(user.username());
        if (storedUser == null) {
            throw new UnauthorizedException("Error: unauthorized"); // Ensure this is thrown for non-existent users
        }
        //CHANGED FROM (!user.password.equals(stored.User.password())
        if (!BCrypt.checkpw(user.password(), storedUser.password())) { //REMEMBER YOU CHANGED THIS FOR SQL TESTS
            throw new UnauthorizedException("Error: unauthorized");
        }
        return dataAccess.createAuth(user.username());
    }

    public void logout(AuthData auth) throws DataAccessException {
        String token = auth.authToken();
        AuthData existingAuth = dataAccess.getAuth(token);
        if (existingAuth == null) {
            throw new UnauthorizedException("Error: invalid auth token"); // Token not found
        }
        dataAccess.deleteAuth(token);
    }

    public AuthData verifyAuth(String authToken) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException("Invalid auth token");
        }
        return authData;
    }
}
