import java.util.HashMap;
import java.util.Map;

public class GameState {
    
    // ตัวแปรเก็บค่าต่างๆ ในเกม
    public int affection = 0;
    public String playerName = "Player";
    public final Map<String, Boolean> flags = new HashMap<>();

    // Constructor (สร้าง Object)
    public GameState() {
    }

    // เมธอดสำหรับเช็คค่า Flag
    public boolean getFlag(String key) {
        return flags.getOrDefault(key, false);
    }

    // เมธอดสำหรับบันทึกค่า Flag
    public void setFlag(String key, boolean value) {
        this.flags.put(key, value);
    }
}