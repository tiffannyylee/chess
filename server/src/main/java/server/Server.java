package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import service.UserService;
import spark.*;

public class Server {
    private final Gson serializer = new Gson();
    private final UserService service= new UserService(new MemoryDataAccess());


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::createUser);
        Spark.delete("/db", (request, response) -> "{}");
        Spark.exception(Exception.class, this::exceptionHandler);
        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(Exception e, Request request, Response response) {
        if (e instanceof DataAccessException) {
            response.status(400);
            response.body("{\"message\": \"" + e.getMessage() + "\"}");
        } else {
            response.status(500);
            response.body("{\"message\": \"Internal Server Error\"}");
        }
    }

    private Object createUser(Request request, Response response) throws DataAccessException {
        var newUser = serializer.fromJson(request.body(), UserData.class);
        var registeredUser = service.register(newUser);
        return serializer.toJson(registeredUser);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
