package server;

import com.google.gson.Gson;
import model.UserData;
import spark.*;

public class Server {
    private final Gson serializer = new Gson();

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

    private Object createUser(Request request, Response response) {
        var newUser = serializer.fromJson(request.body(), UserData.class);
        var result = UserService.registerUser(newUser);
        return serializer.toJson(result);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
