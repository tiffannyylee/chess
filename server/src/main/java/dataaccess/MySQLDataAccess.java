package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQLDataAccess implements DataAccess {
    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }


    @Override
    public UserData createUser(UserData userData) throws DataAccessException {
        String query = "INSERT INTO Users (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userData.username());
            stmt.setString(2, BCrypt.hashpw(userData.password(), BCrypt.gensalt()));
            stmt.setString(3, userData.email());
            stmt.executeUpdate();
            return userData;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user: " + e.getMessage(), e);
        }
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        String query = "INSERT INTO Auth (username, authToken) VALUES (?,?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, authToken);
            stmt.executeUpdate();
            return new AuthData(authToken, username);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create auth token", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String query = "SELECT * FROM Users WHERE username=?"; //get user data from table using username
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String password = rs.getString("password");
                    String email = rs.getString("email");
                    return new UserData(username, password, email);
                } else {
                    throw new DataAccessException("User not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to retrieve user data", e);
        }

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String query = "SELECT * FROM Auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query)) {
            stmt.setString(1, authToken);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    return new AuthData(authToken, username);
                } else {
                    throw new DataAccessException("Auth not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to retrieve auth data", e);
        }
    }

    @Override
    public AuthData deleteAuth(String authToken) throws DataAccessException {
        String query = "DELETE FROM Auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query)) {
            stmt.setString(1, authToken);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("No auth token found for deletion");
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth token", e);
        }
    }

    @Override
    public List<GameData> getGames() throws DataAccessException {
        String query = "SELECT * FROM Games";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query);
             var rs = stmt.executeQuery()) {
            List<GameData> games = new ArrayList<>();
            while (rs.next()) {
                int gameId = rs.getInt("gameID");
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                String gameName = rs.getString("gameName");
                // Assuming `game` is some kind of serializable object stored as BLOB
                GameData gameData = new GameData(gameId, whiteUsername, blackUsername, gameName, null); // Replace null with actual game data if needed
                games.add(gameData);
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve games", e);
        }
    }

    @Override
    public GameData createGame(String gameName, String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        return null;
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {

    }

    private final String[] createStatements = {
            """
      CREATE TABLE IF NOT EXISTS Users (
      id INT NOT NULL AUTO_INCREMENT,
      username VARCHAR(256) NOT NULL UNIQUE,
      password VARCHAR(256) NOT NULL,
      email VARCHAR(256) NOT NULL,
      PRIMARY KEY (id)
    )
    
    """,
            """
CREATE TABLE IF NOT EXISTS Games (
            gameID INT NOT NULL AUTO_INCREMENT,
            whiteUsername VARCHAR(256),
            blackUsername VARCHAR(256),
            gameName VARCHAR(256),
            game BLOB,
            PRIMARY KEY (gameID)
            )
""",
            """
CREATE TABLE IF NOT EXISTS Auth (
authToken VARCHAR(256) NOT NULL UNIQUE,
username VARCHAR(256) NOT NULL,
PRIMARY KEY (username, authToken)
)
"""

    };


    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }


}