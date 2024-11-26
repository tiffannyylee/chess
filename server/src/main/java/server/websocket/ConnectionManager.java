package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String userName, Session session) {
        var connection = new Connection(userName, session);
        connections.put(userName, connection);
    }

    public void delete(String userName) {
        connections.remove(userName);
    }

    public void broadcast(String excludeUserName, ServerMessage message) throws IOException {
        ArrayList<Connection> badConnections = new ArrayList<>();
        for (Connection c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.userName.equals(excludeUserName)) {
                    c.send(message.toString());
                }
            } else {
                badConnections.add(c);
            }
        }
        for (var c : badConnections) {
            connections.remove(c.userName);
        }
    }

    public void send(String userName, ServerMessage message) throws IOException {
        Connection connection = connections.get(userName);
        if (connection != null && connection.session.isOpen()) {
            connection.send(message.toString());
        } else {
            System.err.println("Failed to send message to " + userName + ": Connection is closed or does not exist.");
        }
    }
}
