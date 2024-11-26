package websocket.messages;

public class Notification extends ServerMessage {
    private final String message; // Field to store the notification message

    public Notification(ServerMessageType type, String message) {

        super(type);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
