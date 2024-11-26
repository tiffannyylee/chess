package websocket.commands;

import chess.ChessMove;
import model.AuthData;
import model.UserData;

public class MakeMoveCommand extends UserGameCommand {
    private final ChessMove move;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move) {
        super(commandType, authToken, gameID);
        if (commandType != CommandType.MAKE_MOVE) {
            throw new IllegalArgumentException("MakeMoveCommand must have commandType MAKE_MOVE");
        }
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }
   
}
