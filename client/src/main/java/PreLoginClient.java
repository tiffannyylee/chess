import exception.ResponseException;
import model.UserData;

import java.util.Arrays;

public class PreLoginClient {
    //    Help	Displays text informing the user what actions they can take.
//Quit	Exits the program.
//Login	Prompts the user to input login information. Calls the server login API to login the user.
// When successfully logged in, the client should transition to the Postlogin UI.
//Register	Prompts the user to input registration information. Calls the server register API to register and login the user.
// If successfully registered, the client should be logged in and transition to the Postlogin UI.
    private final ServerFacade server;
    private final String serverUrl;

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
                default -> help();
            };
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }

    }

    public String register(String... parameters) throws ResponseException {
        if (parameters.length >= 3) {
            String username = parameters[0];
            String password = parameters[1];
            String email = parameters[2];
            UserData user = new UserData(username, password, email);
            server.register(user);
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
            server.login(user);
            return String.format("You are now logged in as %s!", username);
        } else {
            throw new ResponseException(500, "username and password are required");
        }
    }

    public String help() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL> - to register a user
                login <USERNAME> <PASSWORD> - to log in
                quit - to quit playing
                help - to see all possible commands""";
    }
}
