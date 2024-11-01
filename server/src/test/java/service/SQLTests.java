package service;

import dataaccess.DataAccess;
import dataaccess.DatabaseManager;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import dataaccess.UserAlreadyExistsException;
import model.AuthData;
import model.UserData;
import model.GameData;
import org.junit.jupiter.api.*;
import passoff.server.TestServerFacade;
import server.Server;

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
    public void setup() throws DataAccessException, SQLException {
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
    public static void startServer() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @AfterEach
    public void cleanup() throws SQLException {
        dropTables(); // Clean up after each test
    }

    @Test
    public void testCreateUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        UserData user = dataAccess.createUser(newUser);
        assertNotNull(user);
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
    public void testCreateAuthSuccess() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        AuthData auth = dataAccess.createAuth("tiff");
        assertNotNull(auth);
        assertEquals("tiff", auth.username());
    }

    @Test
    public void testLoginSuccess() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        AuthData auth = userService.login(newUser);
        assertNotNull(auth);
    }

    @Test
    public void testLoginFailure() {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        assertThrows(DataAccessException.class, () -> {
            userService.login(newUser); // User does not exist yet
        });
    }

    @Test
    public void testLogoutSuccess() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        AuthData auth = userService.login(newUser);
        userService.logout(auth);
        assertThrows(DataAccessException.class, () -> {
            dataAccess.getAuth(auth.authToken()); // Should throw exception
        });
    }

    @Test
    public void testCreateGameSuccess() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        AuthData auth = userService.login(newUser);
        GameData game = gameService.createGame("Test Game", auth.authToken());
        assertNotNull(game);
        assertEquals("Test Game", game.gameName());
    }

    @Test
    public void testListGamesSuccess() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        AuthData auth = userService.login(newUser);
        gameService.createGame("Test Game", auth.authToken());

        List<GameData> games = gameService.listGames(auth.authToken());
        assertNotEquals(0, games.size());
    }

    @Test
    public void testJoinGameSuccess() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        AuthData auth = userService.login(newUser);
        GameData game = gameService.createGame("Test Game", auth.authToken());

        gameService.joinGame("WHITE", game.gameID(), auth.authToken());
        GameData updatedGame = dataAccess.getGame(game.gameID());
        assertEquals(newUser.username(), updatedGame.whiteUsername());
    }

    @Test
    public void testJoinGameFailureGameNotFound() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        dataAccess.createUser(newUser);
        AuthData auth = userService.login(newUser);
        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("WHITE", 999, auth.authToken()); // Invalid game ID
        });
    }

    @Test
    public void testJoinGameFailureUserAlreadyExists() throws DataAccessException {
        UserData user1 = new UserData("tiff", "password", "tiff@email");
        UserData user2 = new UserData("john", "password", "john@email");
        dataAccess.createUser(user1);
        dataAccess.createUser(user2);

        AuthData auth1 = userService.login(user1);
        GameData game = gameService.createGame("Test Game", auth1.authToken());

        gameService.joinGame("WHITE", game.gameID(), auth1.authToken()); // User1 joins

        AuthData auth2 = userService.login(user2);
        assertThrows(UserAlreadyExistsException.class, () -> {
            gameService.joinGame("WHITE", game.gameID(), auth2.authToken()); // User2 tries to join
        });
    }
}
