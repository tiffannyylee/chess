package websocket.commands;

public class ResignCommand extends UserGameCommand {
    String playerColor;

    public ResignCommand(CommandType commandType, String authToken, Integer gameID, String playerColor) {
        super(commandType, authToken, gameID);
        this.playerColor = playerColor;
    }

    public String getColor() {
        return playerColor;
    }
}
