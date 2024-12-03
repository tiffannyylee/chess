package facade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exception.ResponseException;
import model.AuthData;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessageDeserializer;


import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    //start connection when websocket object is created
    // have a place for breaking down the message sent from server
    //write user command messages that send to the server
    Session session;
    ServerMessageObserver messageObserver;

    public WebSocketFacade(String url, ServerMessageObserver messageObserver) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.messageObserver = messageObserver;

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ServerMessage.class, new ServerMessageDeserializer())
                    .create();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    System.out.println("Raw message: " + message);

                    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                    messageObserver.notify(serverMessage);
                    System.out.println(serverMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        System.out.println("Websocket is open");
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
        System.out.println("ws connection closed: " + closeReason.getReasonPhrase());
    }

    @Override
    public void onError(Session session, Throwable thr) {
        super.onError(session, thr);
        System.out.println("ws error occurred: " + thr.getMessage());
    }

    //sends connect command serialized
    public void connect(AuthData authData, int gameID) {
        //send connect request
        //receive game, everyone else receive notification
        try {
            // Create a connect command message
            UserGameCommand connectMessage = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authData.authToken(), gameID);

            // Convert the message to JSON
            String jsonMessage = new Gson().toJson(connectMessage);

            // Send the message to the server
            session.getBasicRemote().sendText(jsonMessage);

            System.out.println("Connected to the server and sent connect message.");
        } catch (IOException e) {
            System.err.println("Error sending connect message: " + e.getMessage());
        }
    }

    public void makeMove() {
    }

    public void leave() {
    }

    public void resign() {
    }

}
