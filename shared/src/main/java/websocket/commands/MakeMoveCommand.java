package websocket.commands;

import chess.ChessMove;
import model.AuthData;
import model.UserData;

public class MakeMoveCommand extends UserGameCommand {
    private final ChessMove move;
    String playerColor;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move, String playerColor) {
        super(commandType, authToken, gameID);
        if (commandType != CommandType.MAKE_MOVE) {
            throw new IllegalArgumentException("MakeMoveCommand must have commandType MAKE_MOVE");
        }
        this.move = move;
        this.playerColor = playerColor;
    }

    public ChessMove getMove() {
        return move;
    }

    public String getPlayerColor() {
        return playerColor;
    }

}
