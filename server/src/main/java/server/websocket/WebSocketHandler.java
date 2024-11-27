package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.LoadGame;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    ConnectionManager connections = new ConnectionManager();
    private MySQLDataAccess dataAccess = new MySQLDataAccess();
    private Gson gson = new Gson();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        System.out.println("Received message: " + message);
        handleMessage(message);
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getAuthToken(), session, command.getGameID());
            case MAKE_MOVE -> makeMove();
            case LEAVE -> leave();
            case RESIGN -> resign();
        }

    }

    public void handleMessage(String message) {
        try {
            // Log the message
            System.out.println("Raw message received: " + message);

            // Deserialize the JSON string into a UserGameCommand object

            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            // Log deserialized object
            System.out.println("Command Type: " + command.getCommandType());
            System.out.println("Auth Token: " + command.getAuthToken());
            System.out.println("Game ID: " + command.getGameID());
        } catch (Exception e) {
            System.err.println("Error deserializing message: " + e.getMessage());
        }
    }

    private void connect(String authToken, Session session, int gameID) throws DataAccessException, IOException {
        //connect to the connections manager
        //send load game to root
        //broadcast notification that root joined
        AuthData user = dataAccess.getAuth(authToken);
        if (user == null) {
            throw new DataAccessException("Invalid authentication token");
        }
        connections.add(user.username(), session);
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found for ID: " + gameID);
        }
        String jsonGame = gson.toJson(game);
        LoadGame loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, game);
        String loadGameJson = gson.toJson(loadGame);
        connections.send(session, loadGame);
        var message = String.format("%s has joined the game", user.username());
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(user.username(), notification);
    }

    private void makeMove() {
        //verify move is valid
        //update game to show move and update game in database
        //send a load game to all clients
        //send notification to others about what move was made
        //send notification ab check checkmate or stalemate to all
    }

    private void leave() {
        //remove client from connections
        //update game in database
        //send notification to all players and observers that root left
    }

    private void resign() {
        //server marks the game as over
        //game is updated in the database
        //sends notification to all players that root resigned. both players and observers
    }

}
