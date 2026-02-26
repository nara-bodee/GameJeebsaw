package Function_SaveGame;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class GlobalSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    // เก็บ ID ของรูป CG ที่ปลดล็อกแล้ว (ใช้ Set จะได้ไม่เก็บข้อมูลซ้ำ)
    public Set<Integer> unlockedCGs;       
    public Set<String> unlockedEndings;    

    public GlobalSaveData() {
        unlockedCGs = new HashSet<>();
        unlockedEndings = new HashSet<>();
    }
}