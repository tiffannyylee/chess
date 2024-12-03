import facade.ServerMessageObserver;
import websocket.messages.LoadGame;
import websocket.messages.ErrorMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;


public class Repl implements ServerMessageObserver {
    private final PreLoginClient client;

    public Repl(String serverUrl) {
        client = new PreLoginClient(serverUrl, this);
    }

    public void run() {
        System.out.println("Welcome to chess! Sign in or register to start.");
        System.out.print(client.help());
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }


    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    //    @Override
//    public void notify(ServerMessage message) {
//        System.out.println(RED + message.toString());
//        printPrompt();
//    }
    @Override
    public void notify(ServerMessage serverMessage) {
        // Based on the type of message, take the appropriate action
        switch (serverMessage.getServerMessageType()) {
            case ServerMessage.ServerMessageType.LOAD_GAME:
                handleLoadGame((LoadGame) serverMessage);
                break;

            case ServerMessage.ServerMessageType.NOTIFICATION:
                handleNotification((Notification) serverMessage);
                break;

            case ServerMessage.ServerMessageType.ERROR:
                handleError((ErrorMessage) serverMessage);
                break;

            // Add more cases for other message types as needed
            default:
                System.err.println("Unknown message type: " + serverMessage.getServerMessageType());
        }
    }

    // Handle the LOAD_GAME message
    private void handleLoadGame(LoadGame message) {
        System.out.println("Received Load Game Message: " + message.getGame());
        // Update the game state with the new game state (e.g., render the board)
        // You might need to update your game UI or internal state here
    }

    // Handle the NOTIFICATION message
    private void handleNotification(Notification notification) {
        System.out.println("Received Notification: " + notification.getMessage());
        // Show the notification to the user, or trigger any other actions
    }

    // Handle the ERROR message
    private void handleError(ErrorMessage errorMessage) {
        System.err.println("Received Error Message: " + errorMessage.getErrorMessage());
        // Display error to the user, handle recovery, or retry logic
    }
}


