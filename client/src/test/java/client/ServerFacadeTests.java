package client;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import facade.ServerFacade;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    static DataAccess dataAccess;


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
        dataAccess = new MySQLDataAccess();
    }

    @BeforeEach
    void clearServer() {
        try {
            dataAccess.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    @Test
    void registerSuccess() throws Exception {
        UserData user = new UserData("player1", "password", "p1@email.com");
        var authData = facade.register(user);
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerFail() throws Exception {
        UserData user = new UserData("player1", "password", null);
        assertThrows(ResponseException.class, () -> facade.register(user));
    }

    @Test
    void loginSuccess() throws Exception {
        UserData user = new UserData("tiff", "pass", "tiff@byu.edu");
        facade.register(user);
        var authData = facade.login(user);
        assertTrue(authData.authToken().length() > 5);
    }

    @Test
    void loginFail() throws Exception {
        UserData user = new UserData("tiff", "pass", "tiff@byu.edu");
        facade.register(user);
        UserData unregistered = new UserData("tiff", "badpass", "tiff@byu.edu");
        assertThrows(ResponseException.class, () -> facade.login(unregistered));
    }

    @Test
    void logoutSuccess() throws Exception {
        UserData user = new UserData("tiff", "pass", "tiff@byu.edu");
        facade.register(user);
        AuthData auth = facade.login(user);
        facade.logout(auth);
        assertThrows(ResponseException.class, () -> facade.createGame("game", auth));
    }

    @Test
    void logoutFail() throws Exception {
        UserData user = new UserData("tiff", "pass", "tiff@byu.edu");
        facade.register(user);
        AuthData auth = facade.login(user);
        facade.logout(auth);
        assertThrows(ResponseException.class, () -> facade.logout(auth));
    }

    @Test
    void createGameSuccess() throws Exception {
        UserData user = new UserData("tiff", "pass", "tiff@byu.edu");
        AuthData auth = facade.register(user);
        facade.createGame("game", auth);
        List<GameData> games = dataAccess.getGames();
        assertTrue(games.size() == 1);
    }

    @Test
    void createGameFail() throws Exception {
        UserData user = new UserData("tiff", "pass", "tiff@byu.edu");
        AuthData auth = facade.register(user);
        facade.createGame("game", auth);
        assertThrows(ResponseException.class, () -> facade.createGame("game", auth));
    }

    @Test
    void joinGameSuccess() throws Exception {
        UserData user = new UserData("tiff", "pass", "tiff@byu.edu");
        AuthData auth = facade.register(user);
        facade.createGame("game", auth);
        List<GameData> games = facade.listGames(auth);
        int gameID = games.stream()
                .filter(game -> "game".equals(game.gameName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Game not found"))
                .gameID();
        assertDoesNotThrow(() -> facade.joinGame(auth, "WHITE", gameID));
    }

    @Test
    void joinGameFail() throws Exception {
        UserData user = new UserData("tiff", "pass", "tiff@byu.edu");
        AuthData auth = facade.register(user);
        facade.createGame("game", auth);
        List<GameData> games = facade.listGames(auth);
        int gameID = games.stream()
                .filter(game -> "game".equals(game.gameName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Game not found"))
                .gameID();
        facade.joinGame(auth, "WHITE", gameID);
        assertThrows(ResponseException.class, () -> facade.joinGame(auth, "WHITE", gameID));
    }

    @Test
    void listGamesSuccess() throws Exception {
        UserData user = new UserData("tiff", "pass", "tiff@byu.edu");
        AuthData auth = facade.register(user);
        facade.createGame("game1", auth);
        facade.createGame("game2", auth);
        facade.createGame("game3", auth);
        assertEquals(3, facade.listGames(auth).size());
    }

    @Test
    void listGamesFail() throws Exception {
        UserData user = new UserData("tiff", "pass", "tiff@byu.edu");
        AuthData auth = facade.register(user);
        AuthData badauth = new AuthData("bad", "tiff");
        assertThrows(ResponseException.class, () -> facade.listGames(badauth));


    }

}
