package server;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.UserData;
import requests.RegisterRequest;
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
}
