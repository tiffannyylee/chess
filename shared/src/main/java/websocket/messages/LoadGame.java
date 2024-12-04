package websocket.messages;

import chess.ChessGame;
import model.GameData;

public class LoadGame extends ServerMessage {
    private GameData game;
    private ChessGame.TeamColor playerColor;

    public LoadGame(ServerMessageType type, GameData game, ChessGame.TeamColor playerColor) {
        super(type);
        this.game = game;
        this.playerColor = playerColor;
    }

    public GameData getGame() {
        return game;
    }

    // Optionally, add a setter if you need to modify the game later
    public void setGame(GameData game) {
        this.game = game;
    }

    public ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }

}
