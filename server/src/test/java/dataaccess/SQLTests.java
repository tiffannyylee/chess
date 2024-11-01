package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.UserData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import service.GameService;
import service.UserService;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SQLTests {
    private DataAccess dataAccess;
    private UserService userService;
    private GameService gameService;
    private static Server server;

    @BeforeEach
    void setup() throws DataAccessException, SQLException {
        dropTables(); // Drop tables before each test
        dataAccess = new MySQLDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(userService, dataAccess);
    }

    private void dropTables() throws SQLException {
        try (var conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS Auth");
            stmt.executeUpdate("DROP TABLE IF EXISTS Games");
            stmt.executeUpdate("DROP TABLE IF EXISTS Users");
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @BeforeAll
    static void startServer() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @AfterEach
    void cleanup() throws SQLException {
        dropTables(); // Clean up after each test
    }

    @Test
    public void testCreateUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        UserData user = dataAccess.createUser(newUser);
        assertNotNull(user);
    }

    @Test
    public void testCreateUserTwiceFail() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        UserData user = dataAccess.createUser(newUser);
        assertThrows(DataAccessException.class, () -> dataAccess.createUser(newUser));
    }

    @Test
    public void testGetUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        UserData user = dataAccess.getUser("tiff");
        assertNotNull(user);
        assertEquals("tiff", user.username());
    }

    @Test
    public void testGetUserBadAuth() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        UserData result = dataAccess.getUser("bad");
        assertNull(result, "Expected null when retrieving a non-existent user");
    }

    @Test
    public void testCreateAuthSuccess() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        AuthData auth = dataAccess.createAuth("tiff");
        assertNotNull(auth);
        assertEquals("tiff", auth.username());
    }

    @Test
    public void testCreateAuthFail() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        assertThrows(DataAccessException.class, () -> dataAccess.createAuth(null));
    }

    @Test
    void testGetAuthPositive() throws DataAccessException {
        String username = "testUser";
        UserData user = new UserData(username, "password123", "test@example.com");
        dataAccess.createUser(user);
        AuthData createdAuth = dataAccess.createAuth(username);

        AuthData retrievedAuth = dataAccess.getAuth(createdAuth.authToken());

        assertNotNull(retrievedAuth, "Auth data should be retrieved successfully");
        assertEquals(createdAuth.authToken(), retrievedAuth.authToken(), "Auth token should match");
        assertEquals(username, retrievedAuth.username(), "Username should match the original username");
    }

    @Test
    void testGetAuthAuthTokenNotFound() {
        String invalidAuthToken = "nonexistentToken";

        assertThrows(DataAccessException.class, () -> {
            dataAccess.getAuth(invalidAuthToken);
        }, "Attempting to get a nonexistent auth token should throw a DataAccessException");
    }

    @Test
    void testDeleteAuthPositive() throws DataAccessException {
        String username = "testUser";
        UserData user = new UserData(username, "password123", "test@example.com");
        dataAccess.createUser(user);
        AuthData createdAuth = dataAccess.createAuth(username);

        dataAccess.deleteAuth(createdAuth.authToken());

        assertThrows(DataAccessException.class, () -> {
            dataAccess.getAuth(createdAuth.authToken());
        }, "Deleted auth token should not be retrievable and should throw a DataAccessException");
    }

    @Test
    void testDeleteAuthAuthTokenNotFound() {
        String invalidAuthToken = "nonexistentToken";

        assertThrows(DataAccessException.class, () -> {
            dataAccess.deleteAuth(invalidAuthToken);
        }, "Attempting to delete a nonexistent auth token should throw a DataAccessException");
    }

    @Test
    void testGetGamesPositive() throws DataAccessException {
        String authToken = dataAccess.createAuth("player1").authToken();
        GameData game1 = dataAccess.createGame("Game 1", authToken);
        GameData game2 = dataAccess.createGame("Game 2", authToken);

        List<GameData> games = dataAccess.getGames();

        assertNotNull(games, "The game list should not be null");
        assertTrue(games.size() >= 2, "The game list should contain at least the games added");

        boolean game1Exists = games.stream().anyMatch(g -> g.gameID() == game1.gameID());
        boolean game2Exists = games.stream().anyMatch(g -> g.gameID() == game2.gameID());
        assertTrue(game1Exists, "Game 1 should be in the retrieved list of games");
        assertTrue(game2Exists, "Game 2 should be in the retrieved list of games");
    }

    @Test
    void testGetGamesNoGamesFound() throws DataAccessException {
        dataAccess.clear();

        List<GameData> games = dataAccess.getGames();

        assertNotNull(games, "The game list should not be null");
        assertTrue(games.isEmpty(), "The game list should be empty when there are no games in the database");
    }

    @Test
    void testCreateGamePositive() throws DataAccessException {
        AuthData authData = dataAccess.createAuth("player1");
        String authToken = authData.authToken();
        String gameName = "Test Game";

        GameData createdGame = dataAccess.createGame(gameName, authToken);

        assertNotNull(createdGame, "The created game should not be null");
        assertEquals(gameName, createdGame.gameName(), "The game name should match the provided name");
        assertNull(createdGame.whiteUsername(), "White username should initially be null");
        assertNull(createdGame.blackUsername(), "Black username should initially be null");
        assertTrue(createdGame.gameID() > 0, "The game ID should be a positive number, indicating it was stored in the database");
    }

    @Test
    void testCreateGameInvalidAuthToken() {
        String invalidAuthToken = "invalid-token";
        String gameName = "Invalid Test Game";

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            dataAccess.createGame(gameName, invalidAuthToken);
        });

        assertTrue(exception.getMessage().contains("Auth not found"),
                "Exception message should indicate invalid authentication token");
    }

    @Test
    void testGetGamePositive() throws DataAccessException {
        AuthData authData = dataAccess.createAuth("player1");
        String authToken = authData.authToken();
        GameData createdGame = dataAccess.createGame("Positive Test Game", authToken);
        int gameId = createdGame.gameID();

        GameData retrievedGame = dataAccess.getGame(gameId);

        assertNotNull(retrievedGame, "The retrieved game should not be null");
        assertEquals(createdGame.gameID(), retrievedGame.gameID(), "Game ID should match");
        assertEquals(createdGame.gameName(), retrievedGame.gameName(), "Game name should match");
    }

    @Test
    void testGetGameNonExistentGame() throws DataAccessException {
        int invalidGameId = 999999;

        assertNull(dataAccess.getGame(invalidGameId), "game is null");
    }

    @Test
    void testUpdateGamePositive() throws DataAccessException {
        AuthData authData = dataAccess.createAuth("player1");
        String authToken = authData.authToken();
        GameData createdGame = dataAccess.createGame("Update Test Game", authToken);

        GameData updatedGameData = new GameData(
                createdGame.gameID(),
                "player1",      // Set as white player
                "player2",      // Set as black player
                "Updated Game Name",
                createdGame.game()
        );

        dataAccess.updateGame(updatedGameData);

        GameData retrievedGame = dataAccess.getGame(updatedGameData.gameID());

        assertEquals(updatedGameData.whiteUsername(), retrievedGame.whiteUsername(), "White username should match updated value");
        assertEquals(updatedGameData.blackUsername(), retrievedGame.blackUsername(), "Black username should match updated value");
        assertEquals(updatedGameData.gameName(), retrievedGame.gameName(), "Game name should match updated value");
    }

    @Test
    void testUpdateGameInvalidGameID() {
        // Arrange: Create a game data object with a non-existent game ID
        GameData nonExistentGame = new GameData(
                999999,              // Non-existent game ID
                "nonPlayer1",
                "nonPlayer2",
                "Non-Existent Game",
                new ChessGame()      // New ChessGame object
        );

        // Act and Assert: Expect a DataAccessException for a non-existent game ID
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            dataAccess.updateGame(nonExistentGame);
        });

    }

    @Test
    public void testClear() throws DataAccessException {
        // Before clear, assert that the tables have data
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        dataAccess.createAuth("tiff");

        dataAccess.clear();

        assertNull(dataAccess.getUser("tiff"), "User should not exist after clear");

        assertTrue(dataAccess.getGames().isEmpty(), "Games table should be empty after clear");
    }


    @Test
    public void testLoginFailure() {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        assertThrows(DataAccessException.class, () -> {
            userService.login(newUser); // User does not exist yet
        });
    }

}
