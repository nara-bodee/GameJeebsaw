package Function_SaveGame;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    // 1. ข้อมูลโชว์หน้า UI (Metadata)
    public String saveDate;      
    public String chapterName;   

    // 2. ข้อมูลสถานะเกม (Game State)
    public String playerName;
    public int currentSceneId;
    public int affectionGirlA;
    public int affectionGirlB;

    // Constructor รับค่าตอนสั่งเซฟ
    public GameSaveData(String chapterName, String playerName, int currentSceneId, int affA, int affB) {
        // ประทับเวลา ณ วินาทีที่กดเซฟอัตโนมัติ
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        this.saveDate = formatter.format(new Date()); 
        
        this.chapterName = chapterName;
        this.playerName = playerName;
        this.currentSceneId = currentSceneId;
        this.affectionGirlA = affA;
        this.affectionGirlB = affB;
    }
}