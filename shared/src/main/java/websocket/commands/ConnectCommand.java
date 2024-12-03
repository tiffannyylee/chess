package websocket.commands;

import chess.ChessGame;

public class ConnectCommand extends UserGameCommand {
    ChessGame.TeamColor playerColor;

    public ConnectCommand(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
