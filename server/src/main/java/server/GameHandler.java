package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import service.UserService;
import spark.Request;
import spark.Response;

public class GameHandler {
    private final UserService userService;
    private final Gson serializer = new Gson();

    public GameHandler(UserService userService) {
        this.userService = userService;

    }

    public Object listGames(Request request, Response response) {
        String authToken = request.headers("Authorization");
        return null;
    }
}
