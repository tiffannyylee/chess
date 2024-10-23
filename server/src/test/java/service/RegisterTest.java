package service;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterTest {
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
}
