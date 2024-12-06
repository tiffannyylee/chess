package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String userName, Session session, Integer gameID) {
        var connection = new Connection(userName, session, gameID);
        connections.put(userName, connection);
    }

    public void delete(String userName) {
        connections.remove(userName);
    }

    public void broadcast(Integer gameID, String excludeUserName, ServerMessage message) throws IOException {
        Gson gson = new Gson();
        ArrayList<Connection> badConnections = new ArrayList<>();
        for (Connection c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.userName.equals(excludeUserName) && c.gameID.equals(gameID)) {
                    c.session.getRemote().sendString(gson.toJson(message));
                }
            } else {
                badConnections.add(c);
            }
        }
        for (var c : badConnections) {
            connections.remove(c.userName);
        }
    }

    public void send(Session session, ServerMessage message) throws IOException {
        Gson gson = new Gson();
        String jsonMessage = gson.toJson(message);
        session.getRemote().sendString(jsonMessage);
        System.out.println("Sent message to client: " + jsonMessage);
//        Connection connection = connections.get(userName);
//        String jsonMessage = gson.toJson(message);
//        if (connection != null && connection.session.isOpen()) {
//            connection.send(jsonMessage);
//        } else {
//            System.err.println("Failed to send message to " + userName + ": Connection is closed or does not exist.");
//        }
    }
}
