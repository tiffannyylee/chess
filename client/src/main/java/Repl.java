import websocket.messages.Notification;

import java.util.Scanner;

import static java.awt.Color.RED;
import static ui.EscapeSequences.*;


public class Repl {
    private final PreLoginClient client;

    public Repl(String serverUrl) {
        client = new PreLoginClient(serverUrl);
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

    public void notify(Notification notification) {
        System.out.println(RED + notification.getMessage());
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

}

