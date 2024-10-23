package server;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.UserData;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.LoginResult;
import responses.RegisterResult;
import service.UserService;
import spark.Request;
import spark.Response;

public class UserHandler {
    private final UserService userService;
    private final Gson serializer = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object createUser(Request request, Response response) throws DataAccessException {
        var registerRequest = serializer.fromJson(request.body(), RegisterRequest.class);
        var newUser = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        var registeredUser = userService.register(newUser);
        return serializer.toJson(new RegisterResult(registeredUser.username(),registeredUser.authToken()));
    }

    public Object loginUser(Request request, Response response) throws DataAccessException {
        var loginRequest = serializer.fromJson(request.body(), LoginRequest.class);
        var user = new UserData(loginRequest.username(),loginRequest.password(),null);
        var loginUser = userService.login(user);
        return serializer.toJson(new LoginResult(loginUser.username(),loginUser.authToken()));
    }
}
