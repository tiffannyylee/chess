package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;


import java.util.*;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authentication = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();

    @Override
    public UserData createUser(UserData userData) throws DataAccessException {
        if (users.containsKey(userData.username())) {
            throw new DataAccessException("User already exists");
        }

        // Add the new user to the map
        users.put(userData.username(), userData);
        return userData;
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authentication.put(authToken, authData);
        System.out.println("Created AuthData: " + authData); // Check stored authData

        return authData;
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authentication.get(authToken);
    }

    @Override
    public AuthData deleteAuth(String authToken) throws DataAccessException{
        authentication.remove(authToken);
        return null;
    }

    @Override
    public List<GameData> getGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public GameData createGame(String gameName, String authToken) throws DataAccessException {
        AuthData authData = authentication.get(authToken);
        if (authData == null) {
            System.out.println("Auth token not found(this is dataaccess creategame method): " + authToken); // Log to check what's happening
            throw new DataAccessException("Invalid authentication token");
        }

        int gameID = games.size() + 1;
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(gameID, authData.username(), null, gameName, game);
        games.put(gameID, gameData);

        return gameData;
    }



    @Override
    public void clear() throws DataAccessException {
        users.clear();
        authentication.clear();
        games.clear();
    }

    @Override
    public void joinGame() throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }
}
