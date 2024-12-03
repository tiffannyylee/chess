package websocket.messages;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ServerMessageDeserializer implements JsonDeserializer<ServerMessage> {
    @Override
    public ServerMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Get the type of message from the serverMessageType field
        String messageType = jsonObject.get("serverMessageType").getAsString();

        // Based on the messageType, return the appropriate ServerMessage subclass
        switch (messageType) {
            case "LOAD_GAME":
                return context.deserialize(jsonObject, LoadGame.class);
            case "NOTIFICATION":
                return context.deserialize(jsonObject, Notification.class);
            case "ERROR":
                return context.deserialize(jsonObject, ErrorMessage.class);
            default:
                return context.deserialize(jsonObject, ServerMessage.class);  // Fallback to generic ServerMessage
        }
    }
}
