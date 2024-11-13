package client;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.MySQLDataAccess;
import exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import ServerFacade.ServerFacade;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    //static DataAccess dataAccess;


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
        //dataAccess = new MySQLDataAccess();
    }

//    @BeforeEach
//    void clearServer() {
//        try {
//            dataAccess.clear();
//        } catch (DataAccessException e) {
//            throw new RuntimeException(e);
//        }
//    }
//    }

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
        UserData user = new UserData("player1", "password", "");
        assertThrows(ResponseException.class, () -> facade.register(user));
    }

}
