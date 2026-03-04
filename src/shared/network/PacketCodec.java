package shared.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PacketCodec {
    private static final Gson GSON = new GsonBuilder().create();

    public static String toJson(DataPacket packet) {
        return GSON.toJson(packet);
    }

    public static DataPacket fromJson(String json) {
        return GSON.fromJson(json, DataPacket.class);
    }

    public static Gson getGson() {
        return GSON;
    }
}
