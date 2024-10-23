package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.UserData;
import server.UserHandler;
import service.UserService;
import spark.*;

public class Server {
    private final Gson serializer = new Gson();
    private final UserService service= new UserService(new MemoryDataAccess());
    private final UserHandler userHandler = new UserHandler(service);


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", (userHandler::createUser));
        Spark.post("/session", (userHandler::loginUser));
        Spark.delete("/session", )
        Spark.delete("/db", (request, response) -> "{}");
        Spark.exception(Exception.class, this::exceptionHandler);
        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(Exception e, Request request, Response response) {
        if (e instanceof UserAlreadyExistsException) {
            response.status(403);
        } else if (e instanceof BadRequestException) {
            response.status(400);
        } else if (e instanceof UnauthorizedException){
            response.status(401);
        } else {
            response.status(500);
        }
        response.body("{\"message\": \"Internal Server Error\"}");
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
