import facade.ServerMessageObserver;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static java.awt.Color.RED;
import static ui.EscapeSequences.*;


public class Repl implements ServerMessageObserver {
    private final PreLoginClient client;

    public Repl(String serverUrl, ServerMessageObserver messageObserver) {
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

    @Override
    public void notify(ServerMessage message) {
        System.out.println(RED + message.toString());
        printPrompt();
    }
}

