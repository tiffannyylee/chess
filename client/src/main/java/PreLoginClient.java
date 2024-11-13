import exception.ResponseException;
import model.AuthData;
import model.UserData;

import java.util.Arrays;

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
                case "observe" -> observeGame();
                case "play" -> playGame();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }

    }

    private String playGame() {
        return "";
    }

    private String observeGame() {
        return "";
    }

    private String listGames() {
        return "";
    }

    private String createGame(String... parameters) throws ResponseException {
        assertLoggedIn();
        if (parameters.length >= 1) {
            String gameName = parameters[0];
            server.createGame(gameName, authData);
        } else {
            throw new ResponseException(500, "game name is expected");
        }
        return "%s has been created!";
    }

    private String logout() throws ResponseException {
        assertLoggedIn();
        if (authData != null) {
            server.logout(authData);
            authData = null;
        }
        return "You have logged out";
    }

    public String register(String... parameters) throws ResponseException {
        if (parameters.length >= 3) {
            String username = parameters[0];
            String password = parameters[1];
            String email = parameters[2];
            UserData user = new UserData(username, password, email);
            authData = server.register(user);
            state = State.LOGGEDIN;
            return String.format("User %s is now registered.", username);
        } else {
            throw new ResponseException(500, "Expected more parameters");
        }
    }

    public String login(String... parameters) throws ResponseException {
        if (parameters.length >= 2) {
            String username = parameters[0];
            String password = parameters[1];
            UserData user = new UserData(username, password, null);
            authData = server.login(user);
            state = State.LOGGEDIN;
            return String.format("You are now logged in as %s!", username);
        } else {
            throw new ResponseException(500, "username and password are required");
        }
    }

    private void assertLoggedIn() throws ResponseException {
        if (state == State.LOGGEDOUT) {
            throw new ResponseException(400, "You must sign in");
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
