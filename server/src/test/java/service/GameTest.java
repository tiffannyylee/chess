package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {
    private MemoryDataAccess memoryDataAccess;
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    public void setup() {
        memoryDataAccess = new MemoryDataAccess();
        userService = new UserService(memoryDataAccess);
        gameService = new GameService(userService, memoryDataAccess);
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
        assertEquals("testUser", createdGame.whiteUsername());
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
}
