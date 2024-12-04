package websocket.commands;

import chess.ChessGame;

public class ConnectCommand extends UserGameCommand {
    String playerColor;

    public ConnectCommand(CommandType commandType, String authToken, Integer gameID, String playerColor) {
        super(commandType, authToken, gameID);
        this.playerColor = playerColor;
    }

    public String getColor() {
        return playerColor;
    }
}
