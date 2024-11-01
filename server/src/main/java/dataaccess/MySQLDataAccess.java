package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.sql.Statement;
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
        List<GameData> gameList = new ArrayList<>();
        Gson gson = new Gson();

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query);
             var rs = stmt.executeQuery()) {
            while (rs.next()) {
                int gameId = rs.getInt("gameID");
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                String gameName = rs.getString("gameName");
                String gameJson = rs.getString("game");
                ChessGame game = gson.fromJson(gameJson, ChessGame.class);
                GameData gameData = new GameData(gameId, whiteUsername, blackUsername, gameName, game);
                gameList.add(gameData);
            }
            return gameList;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve games", e);
        }
    }

    @Override
    public GameData createGame(String gameName, String authToken) throws DataAccessException {
        AuthData authData = getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid authentication token");
        }

        Gson gson = new GsonBuilder().create();

        // New ChessGame instance
        ChessGame game = new ChessGame();
        String gameJson = gson.toJson(game); // Serialize ChessGame to JSON

        String query = "INSERT INTO Games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        int gameID;
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, null);
            stmt.setString(2, null);
            stmt.setString(3, gameName);

            stmt.setString(4, gameJson);
            stmt.executeUpdate();

            // Get the generated game ID
            try (var generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    gameID = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating game failed, no ID obtained.");
                }
            }

            return new GameData(gameID, null, null, gameName, game);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game", e);
        }

    }

    @Override
    public void clear() throws DataAccessException {
        String queryUsers = "DELETE FROM Users";
        String queryAuth = "DELETE FROM Auth";
        String queryGames = "DELETE FROM Games";
        try (var conn = DatabaseManager.getConnection();
             var stmt1 = conn.prepareStatement(queryUsers);
             var stmt2 = conn.prepareStatement(queryAuth);
             var stmt3 = conn.prepareStatement(queryGames)) {
            stmt1.executeUpdate();
            stmt2.executeUpdate();
            stmt3.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear database", e);
        }

    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        String query = "SELECT * FROM Games WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, gameId);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    String gameJson = rs.getString("game");
                    Gson gson = new Gson();
                    ChessGame game = gson.fromJson(gameJson, ChessGame.class);
                    return new GameData(gameId, whiteUsername, blackUsername, gameName, game);
                } else {
                    throw new DataAccessException("Game not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve game data", e);
        }
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        String query = "UPDATE Games SET whiteUsername=?, blackUsername=?, gameName=? WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query)) {
            stmt.setString(1, gameData.whiteUsername());
            stmt.setString(2, gameData.blackUsername());
            stmt.setString(3, gameData.gameName());
            stmt.setInt(4, gameData.gameID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game", e);
        }

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