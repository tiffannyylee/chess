package service;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private MemoryDataAccess memoryDataAccess;
    private UserService userService;

    @BeforeEach
    public void setup() {
        memoryDataAccess = new MemoryDataAccess();
        userService = new UserService(memoryDataAccess);
    }

    @Test
    public void testRegisterUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("testUser", "password123", "test@example.com");

        AuthData authData = userService.register(newUser);

        assertNotNull(authData);
        assertEquals(newUser.username(), authData.username());
    }

    @Test
    public void testRegisterUserAlreadyExists() throws DataAccessException {
        UserData existingUser = new UserData("existingUser", "password123", "test@example.com");
        userService.register(existingUser);  // First registration should succeed

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.register(existingUser);
        });

        assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    public void testLoginSuccess() throws DataAccessException{
        UserData user = new UserData("tiffany", "1234", "test@example.com");
        userService.register(user);

        UserData loginUser = new UserData("tiffany", "1234", null); // No need for email during login
        AuthData authData = userService.login(loginUser);

        assertNotNull(authData); // Ensure authData is not null
        assertEquals(user.username(), authData.username()); // Ensure the username matches
        assertNotNull(authData.authToken()); // Ensure the auth token is created
    }

    @Test
    public void testLoginBadPassword() throws DataAccessException{
        UserData user = new UserData("tiffany", "1234", "test@example.com");
        userService.register(user);

        UserData loginUser = new UserData("tiffany", "wrongpassword", null);

        assertThrows(UnauthorizedException.class, () -> {
            userService.login(loginUser);
        });    }
}
