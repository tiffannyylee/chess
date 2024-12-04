package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.ConnectCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommandDeserializer;
import websocket.messages.LoadGame;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    ConnectionManager connections = new ConnectionManager();
    private MySQLDataAccess dataAccess = new MySQLDataAccess();
    private Gson gson = new Gson();
    private GameService gameService = new GameService(new UserService(dataAccess), dataAccess);

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException, InvalidMoveException {
        System.out.println("Received message: " + message);
        handleMessage(message);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(UserGameCommand.class, new UserGameCommandDeserializer())
                .create();

        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> {
                if (command instanceof ConnectCommand connectCommand) {
                    String color = connectCommand.getColor();
                    connect(command.getAuthToken(), session, command.getGameID(), color);
                } else {
                    System.out.println("Invalid command: CONNECT command is not of type ConnectCommand.");
                }
            }
            case MAKE_MOVE -> {
                if (command instanceof MakeMoveCommand makeMoveCommand) {
                    ChessMove move = makeMoveCommand.getMove();
                    ChessGame.TeamColor color = makeMoveCommand.getPlayerColor();
                    makeMove(command.getAuthToken(), session, command.getGameID(), color, move);
                }
            }
            case LEAVE -> leave();
            case RESIGN -> resign();
        }

    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error occurred for session: " + session);
        throwable.printStackTrace();
        // You might also want to close the session if it's in an unrecoverable state
        if (session != null && session.isOpen()) {
            session.close();
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

    private void connect(String authToken, Session session, int gameID, String color) throws DataAccessException, IOException {
        //connect to the connections manager
        //send load game to root
        //broadcast notification that root joined
        AuthData user = null;
        try {
            user = dataAccess.getAuth(authToken);
//            if (user.authToken() == null) {
//                ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error : bad auth");
//                connections.send(session, message);
//            }
        } catch (DataAccessException e) {
            System.out.println("Bad auth error");
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error : bad auth");
            connections.send(session, errorMessage);
            return;
        }

        if (user == null) {
//            ErrorMessage message = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error : bad auth");
//            connections.send(session, message);
        }
        connections.add(user.username(), session);
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error : game not found");
            connections.send(session, errorMessage);
            return;
        }
        LoadGame loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, game, color);
        connections.send(session, loadGame);
        var message = String.format("%s has joined the game", user.username());
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(user.username(), notification);
    }

    private void makeMove(String authToken, Session session, int gameID, ChessGame.TeamColor playerColor, ChessMove move) throws DataAccessException, InvalidMoveException, IOException {
        //verify move is valid
        //update game to show move and update game in database
        //send a load game to all clients
        //send notification to others about what move was made
        //send notification ab check checkmate or stalemate to all
        //current user = user
        //team turn = game.getTeamTurn()
        //white username gameData.whiteUsername()
        String user = dataAccess.getAuth(authToken).username();
        GameData gameData = dataAccess.getGame(gameID);
        ChessGame game = gameData.game();
        try {
            boolean isWhite = user.equals(gameData.whiteUsername());
            if (isWhite) {
                playerColor = ChessGame.TeamColor.WHITE;
            } else {
                playerColor = ChessGame.TeamColor.BLACK;
            }
            if (game.getTeamTurn().equals(playerColor)) {
                game.makeMove(move);
                ChessGame.TeamColor opposingColor = playerColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

                if (game.isInCheckmate(opposingColor)) {
                    Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s won the game!", user));
                    connections.broadcast("", notification); //send to everyone
                } else if (game.isInCheck(opposingColor)) {
                    Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s is now in check!", opposingColor.toString()));
                    connections.broadcast("", notification); //send to everyone
                } else if (game.isInStalemate(opposingColor)) {
                    Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "The game is now in stalemate");
                    connections.broadcast("", notification); //send to everyone
                } else {
                    Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s just made a move", user));
                    connections.broadcast(user, notification);
                }
                dataAccess.updateGame(gameData);
                LoadGame loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, gameData, playerColor.toString());
                connections.broadcast("", loadGame);
            }
        } catch (InvalidMoveException e) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "This is not a valid move");
            connections.send(session, errorMessage);
        }

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
