package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.*;
import websocket.messages.LoadGame;
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
            case LEAVE -> {
                if (command instanceof LeaveCommand leaveCommand) {
                    leave(session, command.getAuthToken(), command.getGameID());
                }
            }
            case RESIGN -> {
                if (command instanceof ResignCommand resignCommand) {
                    String color = resignCommand.getColor();
                    resign(session, command.getAuthToken(), command.getGameID(), color);
                }
            }
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
        } catch (DataAccessException e) {
            System.out.println("Bad auth error");
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error : bad auth");
            connections.send(session, errorMessage);
            return;
        }
        connections.add(user.username(), session, gameID);
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error : game not found");
            connections.send(session, errorMessage);
            return;
        }
        LoadGame loadGame;
        String message;
        if (!user.username().equals(game.whiteUsername()) && !user.username().equals(game.blackUsername())) {
            loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, game, "white");
            message = String.format("%s is observing the game", user.username());
        } else {
            loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, game, color);
            message = String.format("%s has joined the game as %s", user.username(), color);
        }
        connections.send(session, loadGame);
        //var message = String.format("%s has joined the game", user.username());
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(gameID, user.username(), notification);
    }

    private void makeMove(String auth, Session sess, int gameID, ChessGame.TeamColor color, ChessMove move) throws DataAccessException, IOException {
        try {
            String user = dataAccess.getAuth(auth).username();
            GameData gameData = dataAccess.getGame(gameID);
            ChessGame game = gameData.game();
            boolean isWhite = user.equals(gameData.whiteUsername());
            if (isWhite && color == null) {
                color = ChessGame.TeamColor.WHITE;
            } else {
                color = ChessGame.TeamColor.BLACK;
            }
            if (game.getIsOver()) {
                ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Oops! This game is already over.");
                connections.send(sess, errorMessage);
                return;
            }
            if (game.getTeamTurn().equals(color)) {
                game.makeMove(move);
                ChessGame.TeamColor opposingColor = color == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                ChessPosition from = move.getStartPosition();
                ChessPosition to = move.getEndPosition();
                Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION,
                        String.format("%s just moved from %s to %s ", user, from, to));
                connections.broadcast(gameID, user, notification);

                if (game.isInCheckmate(opposingColor)) {
                    //change to players name
                    Notification notif = new Notification(ServerMessage.ServerMessageType.NOTIFICATION,
                            String.format("%s is in checkmate!", opposingColor));
                    connections.broadcast(gameID, "", notif); //send to everyone
                    game.setIsOver(true);
                } else if (game.isInCheck(opposingColor)) {
                    //change to players name
                    Notification notif = new Notification(ServerMessage.ServerMessageType.NOTIFICATION,
                            String.format("%s is now in check!", opposingColor.toString()));
                    connections.broadcast(gameID, "", notif); //send to everyone
                } else if (game.isInStalemate(opposingColor)) {
                    Notification notif = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "The game is now in stalemate. It's a tie!");
                    connections.broadcast(gameID, "", notif); //send to everyone
                    game.setIsOver(true);
                }
                LoadGame loadGameWhite = new LoadGame(
                        ServerMessage.ServerMessageType.LOAD_GAME,
                        gameData,
                        ChessGame.TeamColor.WHITE.toString()
                );

                LoadGame loadGameBlack = new LoadGame(
                        ServerMessage.ServerMessageType.LOAD_GAME,
                        gameData,
                        ChessGame.TeamColor.BLACK.toString()
                );
                if (isWhite) {
                    connections.send(sess, loadGameWhite); // Send White's perspective to White
                    connections.broadcast(gameID, user, loadGameBlack); // Send Black's perspective to Black
                } else {
                    connections.send(sess, loadGameBlack); // Send White's perspective to White
                    connections.broadcast(gameID, user, loadGameWhite); // Send Black's perspective to Black
                }
                dataAccess.updateGame(gameData);
            } else {
                ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Oh no! It is not your turn.");
                connections.send(sess, errorMessage);
            }
        } catch (InvalidMoveException e) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "This is not a valid move");
            connections.send(sess, errorMessage);
        } catch (UnauthorizedException e) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Looks like you are not authorized. uh oh!");
            connections.send(sess, errorMessage);
        }

    }

    private void leave(Session session, String authToken, int gameID) throws DataAccessException, IOException {
        //remove client from connections
        //update game in database
        //send notification to all players and observers that root left
        String user = dataAccess.getAuth(authToken).username();
        GameData gameData = dataAccess.getGame(gameID);
        ChessGame.TeamColor color = user.equals(gameData.whiteUsername()) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s has left the game :(", user));
        GameData updatedGameData;
        if (color == ChessGame.TeamColor.WHITE) {
            updatedGameData = new GameData(gameID, null, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else {
            updatedGameData = new GameData(gameID, gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
        }

        // Update the game in the database
        dataAccess.updateGame(updatedGameData);
        connections.broadcast(gameID, user, notification);
        connections.delete(user);
    }

    private void resign(Session session, String authToken, int gameID, String color) throws DataAccessException, IOException {
        //server marks the game as over
        //game is updated in the database
        //sends notification to all players that root resigned. both players and observers
        String user = dataAccess.getAuth(authToken).username();
        GameData gameData = dataAccess.getGame(gameID);
        ChessGame game = gameData.game();


        if (!user.equals(gameData.whiteUsername()) && !user.equals(gameData.blackUsername())) {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "You are observing!");
            connections.send(session, error);
            return;
        }
        ChessGame.TeamColor playerColor = user.equals(gameData.whiteUsername()) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        String opponentUsername = playerColor == ChessGame.TeamColor.WHITE ? gameData.blackUsername() : gameData.whiteUsername();
        if (game.getIsOver()) {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "This is over");
            connections.send(session, error);
            return;
        }
        gameData.game().setIsOver(true);
        dataAccess.updateGame(gameData);
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION,
                String.format("%s has resigned. The game is over.", user));
        connections.broadcast(gameID, "", notification);
    }
}
