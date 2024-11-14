import facade.ServerFacade;
import exception.ResponseException;
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


    public PreLoginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
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
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }

    }

    private String playGame(String... parameters) throws ResponseException {
        if (parameters.length < 2) {
            return "Error: You must specify the game number and the color (e.g., '1' 'WHITE').";
        }
        List<GameData> gamesList = server.listGames(authData);
        try {
            int gameNumber = Integer.parseInt(parameters[0]); // Game number selected by the user
            String color = parameters[1].toUpperCase(); // Color chosen by the user

            GameData selectedGame = gamesList.get(gameNumber - 1); // Adjust for zero-indexed list
            int gameID = selectedGame.gameID(); // Get the ID of the selected game

            // Calls the server to join the game
            server.joinGame(authData, color, gameID);
            String message = String.format("You have joined the game '%s' as %s!", selectedGame.gameName(), color);
            var out = new PrintStream(System.out);
            BoardUI.drawChessBoardBlack(out);
            BoardUI.drawChessBoardWhite(out);
            return message;

        } catch (NumberFormatException e) {
            return "Error: Invalid game number. Please enter a valid number.";
        } catch (IndexOutOfBoundsException e) {
            return "Error: Invalid game number. No game exists with that number.";
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    private String observeGame(String... parameters) throws ResponseException {
        List<GameData> gamesList = server.listGames(authData);
        try {
            int gameNumber = Integer.parseInt(parameters[0]); // Game number selected by the user
            GameData selectedGame = gamesList.get(gameNumber - 1); // Adjust for zero-indexed list
            int gameID = selectedGame.gameID(); // Get the ID of the selected game
            String message = String.format("You are watching the game '%s'!", selectedGame.gameName());
            var out = new PrintStream(System.out);
            BoardUI.drawChessBoardBlack(out);
            BoardUI.drawChessBoardWhite(out);
            return message;
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
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
        List<GameData> games = server.listGames(authData);
        return listGamesDisplay(games);
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
        } else {
            return """
                    logout - to log out
                    create <GameName> - to create a new game
                    list - to list all available games
                    play <ID> [WHITE|BLACK] - to join a game
                    observe <ID> - watch a game
                    quit - to quit playing
                    help - to see all possible commands
                    """;
        }
    }

}
