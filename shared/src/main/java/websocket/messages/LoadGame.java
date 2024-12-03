package websocket.messages;

import model.GameData;

public class LoadGame extends ServerMessage {
    private GameData game;

    public LoadGame(ServerMessageType type, GameData game) {
        super(type);
        this.game = game;
    }

    public GameData getGame() {
        return game;
    }

    // Optionally, add a setter if you need to modify the game later
    public void setGame(GameData game) {
        this.game = game;
    }

}
