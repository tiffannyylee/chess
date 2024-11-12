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

    public String help() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL> - to register a user
                login <USERNAME> <PASSWORD> - to log in
                quit - to quit playing
                help - to see all possible commands""";
    }
}
