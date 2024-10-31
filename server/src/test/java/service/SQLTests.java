package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import passoff.server.TestServerFacade;
import server.Server;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SQLTests {
    private DataAccess dataAccess;
    private UserService userService;
    private GameService gameService;
    private static Server server;

    @BeforeEach
    public void setup() throws DataAccessException {
        dataAccess = new MySQLDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(userService, dataAccess);

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

    @Test
    public void testCreateUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("tiff", "password", "tiff@email");
        UserData user = dataAccess.createUser(newUser);
        System.out.println(user);
        assertNotNull(user);
    }

    @Test
    public void testGetUserSuccess() throws DataAccessException {
        UserData user = dataAccess.getUser("tiff");
        System.out.println(user);
        assertNotNull(user);
    }

    @Test
    public void testCreateAuthSuccess() throws DataAccessException {
        AuthData auth = dataAccess.createAuth("tiff");
        System.out.println(auth);
    }


}
