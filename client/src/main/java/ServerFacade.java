import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class ServerFacade {
    //register,login,logout,create game, join game, list games

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData register(UserData newUser) throws ResponseException {
        String path = "/user";
        return this.makeRequest("POST", path, newUser, AuthData.class);
    }

    public AuthData login(UserData user) throws ResponseException {
        String path = "/session";
        return this.makeRequest("POST", path, user, AuthData.class);
    }

    public void logout(AuthData auth) throws ResponseException {
        String path = "/session";
        this.makeRequest("DELETE", path, auth, null);
    }

    public GameData createGame(String gameName) throws ResponseException {
        String path = "/game";
        return this.makeRequest("POST", path, gameName, GameData.class);
    }

//    public void joinGame(String playerColor, int gameID, String authToken) {
//
//    }

    public List<GameData> listGames() throws ResponseException {
        String path = "/game";
        record listGamesResponse(List<GameData> gameData) {
        }
        var response = this.makeRequest("GET", path, null, listGamesResponse.class);
        assert response != null;
        return response.gameData();
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            System.out.println("this was successful");
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private boolean isSuccessful(int status) {
        return status == 200;
    }
}
