package clientrequests;

public record JoinGameRequest(String authToken, String playerColor, int gameID) {
}
