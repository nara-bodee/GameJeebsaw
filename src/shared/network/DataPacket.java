package shared.network;

import java.util.HashMap;
import java.util.Map;

public class DataPacket {
    public String type;
    public String roomId;
    public String playerId;
    public String token;
    public long ts;
    public Map<String, Object> payload = new HashMap<>();

    public DataPacket() {}

    public static DataPacket of(String type) {
        DataPacket p = new DataPacket();
        p.type = type;
        p.ts = System.currentTimeMillis();
        return p;
    }

    public DataPacket with(String key, Object value) {
        payload.put(key, value);
        return this;
    }
}
