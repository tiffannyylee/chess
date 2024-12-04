package websocket.commands;

import chess.ChessGame;

public class ConnectCommand extends UserGameCommand {
    ChessGame.TeamColor playerColor;

    public ConnectCommand(CommandType commandType, String authToken, Integer gameID, ChessGame.TeamColor playerColor) {
        super(commandType, authToken, gameID);
        this.playerColor = playerColor;
    }

    public ChessGame.TeamColor getColor() {
        return playerColor;
    }
}
