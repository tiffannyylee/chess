package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.UserData;
import server.UserHandler;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {
    private final Gson serializer = new Gson();
    private final DataAccess dataAccess = new MemoryDataAccess();
    private final UserService service= new UserService(dataAccess);
    private final GameService gameService = new GameService(service, dataAccess);
    private final UserHandler userHandler = new UserHandler(service);
    private final GameHandler gameHandler = new GameHandler(service, gameService);


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", (userHandler::createUser));
        Spark.post("/session", (userHandler::loginUser));
        Spark.post("/game", (gameHandler::createGame));
        Spark.delete("/session", (userHandler::logoutUser));
        Spark.delete("/db", this::clearDatabase);
        Spark.get("/game",(gameHandler::listGames));
        Spark.put("/game", (gameHandler::joinGame));
        Spark.exception(Exception.class, this::exceptionHandler);
        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }
    private Object clearDatabase(Request request, Response response) {
        try {
            dataAccess.clear();
            response.status(200);
            return "{}";
        } catch (Exception e) {
            response.status(500);
            return "{\"message\": \"Error: " + e.getMessage() + "\"}";
        }
    }

    private void exceptionHandler(Exception e, Request request, Response response) {
        e.printStackTrace(); // This will print the full stack trace to the server logs
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
