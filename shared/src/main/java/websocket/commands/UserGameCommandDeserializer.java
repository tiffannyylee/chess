package websocket.commands;

import com.google.gson.*;

import java.lang.reflect.Type;

public class UserGameCommandDeserializer implements JsonDeserializer<UserGameCommand> {

    @Override
    public UserGameCommand deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        UserGameCommand.CommandType commandType = UserGameCommand.CommandType.valueOf(jsonObject.get("commandType").getAsString());

        switch (commandType) {
            case CONNECT:
                return context.deserialize(jsonObject, ConnectCommand.class);
            case MAKE_MOVE:
                return context.deserialize(jsonObject, MakeMoveCommand.class);
            case LEAVE:
            case RESIGN:
            default:
                return context.deserialize(jsonObject, UserGameCommand.class);
        }
    }
}
