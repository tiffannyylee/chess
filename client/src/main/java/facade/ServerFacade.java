package facade;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.RegisterRequest;

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
        RegisterRequest request = new RegisterRequest(newUser.username(), newUser.password(), newUser.email());
        return this.makeRequest("POST", path, request, AuthData.class, null);
    }

    public AuthData login(UserData user) throws ResponseException {
        String path = "/session";
        return this.makeRequest("POST", path, user, AuthData.class, null);
    }

    public void logout(AuthData auth) throws ResponseException {
        String path = "/session";
        this.makeRequest("DELETE", path, null, null, auth);
    }

    public void createGame(String gameName, AuthData auth) throws ResponseException {
        String path = "/game";
        CreateGameRequest request = new CreateGameRequest(gameName);
        this.makeRequest("POST", path, request, GameData.class, auth);
    }

    public void joinGame(AuthData auth, String playerColor, int gameID) throws ResponseException {
        String path = "/game";
        JoinGameRequest request = new JoinGameRequest(auth.authToken(), playerColor, gameID);
        this.makeRequest("PUT", path, request, null, auth);
    }

    public List<GameData> listGames(AuthData auth) throws ResponseException {
        String path = "/game";
        record ListGamesResponse(List<GameData> games) {
        }
        var response = this.makeRequest("GET", path, null, ListGamesResponse.class, auth);
        assert response != null;
        if (response == null || response.games() == null) {
            throw new ResponseException(500, "Failed to retrieve game data");
        }
        return response.games();
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, AuthData auth) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if (auth != null && auth.authToken() != null) {
                http.setRequestProperty("Authorization", auth.authToken());
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
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
        int status = http.getResponseCode();
        if (!isSuccessful(status)) {
            String errorMsg = "failure: " + status;
            try (InputStream errorStream = http.getErrorStream()) {
                if (errorStream != null) {
                    errorMsg += " - " + new String(errorStream.readAllBytes());
                }
            }
            throw new ResponseException(status, errorMsg);
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
