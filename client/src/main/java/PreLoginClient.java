import chess.*;
import facade.ServerFacade;
import exception.ResponseException;
import facade.ServerMessageObserver;
import facade.WebSocketFacade;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class PreLoginClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGEDOUT;
    private AuthData authData;
    private WebSocketFacade ws;
    private final ServerMessageObserver messageObserver;


    public PreLoginClient(String serverUrl, ServerMessageObserver messageObserver) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.messageObserver = messageObserver;
    }

    public String eval(String input) throws ResponseException {
        try {
            var tokens = input.toLowerCase().split(" ");
            var command = (tokens.length > 0) ? tokens[0] : "help";
            var parameters = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "help" -> help();
                case "register" -> register(parameters);
                case "login" -> login(parameters);
                case "logout" -> logout();
                case "create" -> createGame(parameters);
                case "list" -> listGames();
                case "observe" -> observeGame(parameters);
                case "play" -> playGame(parameters);
                case "redraw" -> redrawBoard();
                case "leave" -> leaveGame();
                case "move" -> makeMove(parameters);
                case "resign" -> resignGame();
                case "highlight" -> highlightMoves();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }

    }

    private String highlightMoves() {
        return null;
    }

    private String resignGame() {
        return null;
    }

    private String makeMove() {
        return null;
    }

    private String leaveGame() {
        return null;
    }

    private String redrawBoard() {
        return null;
    }

    private String playGame(String... parameters) throws ResponseException {
        if (parameters.length < 2) {
            return "Error: You must specify the game number and the color (e.g., '1' 'WHITE').";
        }
        List<GameData> gamesList = server.listGames(authData);
        try {
            int gameNumber = Integer.parseInt(parameters[0]); // Game number selected by the user
            String color = parameters[1].toUpperCase(); // Color chosen by the user
            ChessGame.TeamColor playerColor = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                return "please enter a valid color (white or black)";
            }
            GameData selectedGame = gamesList.get(gameNumber - 1); // Adjust for zero-indexed list
            int gameID = selectedGame.gameID(); // Get the ID of the selected game

            // Calls the server to join the game
            server.joinGame(authData, color, gameID);
            ws = new WebSocketFacade(serverUrl, messageObserver, playerColor);
            ws.connect(authData, gameID, color);
            state = State.GAMEPLAY;
            String message = String.format("You have joined the game '%s' as %s!", selectedGame.gameName(), color);
            var out = new PrintStream(System.out);
//            BoardUI.drawChessBoardBlack(out);
//            BoardUI.drawChessBoardWhite(out);
            return message;

        } catch (NumberFormatException e) {
            return "Error: Invalid game number. Please enter a valid number.";
        } catch (IndexOutOfBoundsException e) {
            return "Error: Invalid game number. No game exists with that number.";
        } catch (ResponseException e) {
            return "Something went wrong! Make sure you are authorized.";
        }
    }

    private String observeGame(String... parameters) throws ResponseException {
        //ws.connect
        List<GameData> gamesList = server.listGames(authData);
        try {
            if (parameters.length < 1) {
                return "you need to enter a game number!";
            }
            int gameNumber = Integer.parseInt(parameters[0]); // Game number selected by the user
            if (gameNumber > gamesList.size() || gameNumber <= 0) {
                return "This game does not exist";
            } else {
                GameData selectedGame = gamesList.get(gameNumber - 1); // Adjust for zero-indexed list
                int gameID = selectedGame.gameID(); // Get the ID of the selected game
                String message = String.format("You are watching the game '%s'!", selectedGame.gameName());
                var out = new PrintStream(System.out);
//                BoardUI.drawChessBoardBlack(out);
//                BoardUI.drawChessBoardWhite(out);
                state = State.GAMEPLAY;
                return message;
            }
        } catch (NumberFormatException e) {
            return "Please enter a number.";
        }
    }

    public String listGamesDisplay(List<GameData> games) {
        StringBuilder display = new StringBuilder();
        int count = 1;
        for (GameData game : games) {
            String gameName = game.gameName();
            String whitePlayer = game.whiteUsername() != null ? game.whiteUsername() : "TBD";
            String blackPlayer = game.blackUsername() != null ? game.blackUsername() : "TBD";
            display.append(String.format("%d. Game Name: %s | Players: %s vs %s\n", count, gameName, whitePlayer, blackPlayer));
            count++;
        }
        return display.toString();
    }


    private String listGames() throws ResponseException {
        assertLoggedIn();
        try {
            List<GameData> games = server.listGames(authData);
            if (games.isEmpty()) {
                return "There are no games currently. Why don't you create one?";
            } else {
                return listGamesDisplay(games);
            }
        } catch (ResponseException e) {
            return "Something went wrong!";
        }

    }

    private String createGame(String... parameters) throws ResponseException {
        assertLoggedIn();
        try {
            if (parameters.length == 1) {
                String gameName = parameters[0];
                server.createGame(gameName, authData);
            } else if (parameters.length < 1) {
                return "game name is expected";
            } else {
                return "Please keep the name to one word!";
            }
            return String.format("%s has been created!", parameters[0]);
        } catch (ResponseException e) {
            return "Something went wrong. Be sure to enter a unique game name.";
        }

    }

    private String logout() throws ResponseException {
        assertLoggedIn();
        if (authData != null) {
            server.logout(authData);
            authData = null;
        }
        state = State.LOGGEDOUT;
        return "You have logged out";
    }

    public String register(String... parameters) throws ResponseException {
        try {
            if (parameters.length == 3) {
                String username = parameters[0];
                String password = parameters[1];
                String email = parameters[2];
                UserData user = new UserData(username, password, email);
                authData = server.register(user);
                state = State.LOGGEDIN;
                return String.format("User %s is now registered.", username);
            } else if (parameters.length < 3) {
                return "You are missing credentials. Please enter a username, password, and email.";
            } else {
                return "You have entered too many arguments! please try again.";
            }
        } catch (ResponseException e) {
            return "Unable to register. Please make sure you are not already registered";
        }

    }

    public String login(String... parameters) throws ResponseException {
        try {
            if (parameters.length == 2) {
                String username = parameters[0];
                String password = parameters[1];
                UserData user = new UserData(username, password, null);
                authData = server.login(user);
                state = State.LOGGEDIN;
                return String.format("You are now logged in as %s!", username);
            } else if (parameters.length < 2) {
                return "Username and password are required!";
            } else {
                return "you have entered too many things! just username and password please:)";
            }
        } catch (ResponseException e) {
            return "Error: Unable to log in. Please check your username and password, and try again.";

        }

    }

    //    public ChessBoard redrawBoard(){
//
//    }
//    public void leave() {
//      ws.leave
//    }
//
    private String makeMove(String... parameters) {
        try {
            if (parameters.length == 3 && parameters[0].matches("[a-h][1-8]") && parameters[1].matches("[a-h][1-8]")) {
                //promotion was entered
                ChessPosition from = new ChessPosition(parameters[0].charAt(1) - '0', parameters[0].charAt(0) - 'a' + 1); //a1 a is col 1 is row
                ChessPosition to = new ChessPosition(parameters[1].charAt(1) - '0', parameters[1].charAt(0) - 'a' + 1); //-0 so it renders char
                ChessPiece.PieceType promotion = getPiece(parameters[2]);
                ws.makeMove(new ChessMove(from, to, promotion));
                return "good job move made";
            } else if (parameters.length == 2) {
                ChessPosition from = new ChessPosition(parameters[0].charAt(1) - '0', parameters[0].charAt(0) - 'a' + 1); //a1 a is col 1 is row
                ChessPosition to = new ChessPosition(parameters[1].charAt(1) - '0', parameters[1].charAt(0) - 'a' + 1);
                ws.makeMove(new ChessMove(from, to, null));
                return "good job move made";
            } else {
                return "Please enter a from and to coordinate!";
            }
        } catch (Exception e) {
            return "failed move";
        }
    }

    private void getUser() {

    }

    private ChessPiece.PieceType getPiece(String parameter) {
        return switch (parameter.toUpperCase()) {
            case "QUEEN" -> ChessPiece.PieceType.QUEEN;
            case "BISHOP" -> ChessPiece.PieceType.BISHOP;
            case "ROOK" -> ChessPiece.PieceType.ROOK;
            case "KNIGHT" -> ChessPiece.PieceType.KNIGHT;
            case "PAWN" -> ChessPiece.PieceType.PAWN;
            default -> null;
        };
    }
//
//    public void resign() {
//      ws.resign
//    }
//    public void highlight(){
//
//    }

    private void assertLoggedIn() {
        if (state == State.LOGGEDOUT) {
            System.out.print("You must sign in to continue.");
        }
    }

    public String help() {
        if (state == State.LOGGEDOUT) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> - to register a user
                    login <USERNAME> <PASSWORD> - to log in
                    quit - to quit playing
                    help - to see all possible commands""";
        } else if (state == State.LOGGEDIN) {
            return """
                    logout - to log out
                    create <GameName> - to create a new game
                    list - to list all available games
                    play <ID> [WHITE|BLACK] - to join a game
                    observe <ID> - watch a game
                    quit - to quit playing
                    help - to see all possible commands
                    """;
        } else {
            return """
                    redraw - to redraw the chess board
                    leave - to leave the game
                    move <from> <to> <promotion> - to make a move (example: move a1 b1)
                    resign - to forfeit the game
                    highlight - to highlight legal moves for a piece
                    help - to see all possible commands
                    """;
        }
    }

}
