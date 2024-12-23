package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {
    private MemoryDataAccess dataAccess;
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    public void setup() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(userService, dataAccess);
    }

    @Test
    public void testCreateGameSuccess() throws DataAccessException {
        // Register a user and login to obtain an auth token
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userService.register(user);
        AuthData authData = userService.login(new UserData("testUser", "password123", null));

        // Attempt to create a game
        GameData createdGame = gameService.createGame("Test Game", authData.authToken());

        // Assertions
        assertNotNull(createdGame);
        assertEquals("Test Game", createdGame.gameName());
    }

    @Test
    public void testCreateGameUnauthorized() {
        // Attempt to create a game with an invalid auth token
        String invalidAuthToken = "invalid-auth-token";

        assertThrows(DataAccessException.class, () -> {
            gameService.createGame("Unauthorized Game", invalidAuthToken);
        });
    }

    @Test
    public void testCreateGameWithoutLogin() {
        // Attempt to create a game without registering or logging in
        assertThrows(DataAccessException.class, () -> {
            gameService.createGame("Game Without User", null);
        });
    }

    @Test
    public void testJoinGameSuccess() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userService.register(user);
        AuthData authData = userService.login(new UserData("testUser", "password123", null));
        GameData createdGame = gameService.createGame("Test Game", authData.authToken());
        assertDoesNotThrow(() -> gameService.joinGame("WHITE", createdGame.gameID(), authData.authToken()));
    }

    @Test
    public void testJoinGameFail() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userService.register(user);
        AuthData authData = userService.login(new UserData("testUser", "password123", null));
        GameData createdGame = gameService.createGame("Test Game", authData.authToken());
        gameService.joinGame("WHITE", createdGame.gameID(), authData.authToken());
        assertThrows(DataAccessException.class, () -> gameService.joinGame("WHITE", createdGame.gameID(), authData.authToken()));
    }

    @Test
    public void testListGameSuccess() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userService.register(user);
        AuthData authData = userService.login(new UserData("testUser", "password123", null));
        GameData createdGame = gameService.createGame("Test Game", authData.authToken());
        GameData createGame2 = gameService.createGame("another", authData.authToken());
        List<GameData> games = gameService.listGames(authData.authToken());

        assertEquals(2, games.size(), "There should be exactly one game in the list.");
    }

    @Test
    public void testListGamesFail() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userService.register(user);
        AuthData authData = userService.login(new UserData("testUser", "password123", null));

        String invalidAuthToken = "invalidToken12345";
        assertThrows(UnauthorizedException.class, () -> {
            gameService.listGames(invalidAuthToken);
        }, "Expected UnauthorizedException when using an invalid auth token.");
    }
}
