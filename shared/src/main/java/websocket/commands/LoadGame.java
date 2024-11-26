package websocket.commands;

import chess.ChessGame;
import websocket.messages.ServerMessage;

public class LoadGame extends ServerMessage {
    public LoadGame(ServerMessageType type, ChessGame game) {
        super(type);
    }
}
