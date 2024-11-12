import chess.*;
import model.UserData;

public class Main {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        new Repl(serverUrl).run();
//        ServerFacade serverfacade = new ServerFacade(serverUrl);
//        UserData user = new UserData("bleh", "bleh", "tiff@byu.edu");
//        serverfacade.register(user);
    }
}