package Function_SaveGame;

import java.io.Serializable;

public class Game_confing implements Serializable {
    private static final long serialVersionUID = 1L;

    // ตัวแปรการตั้งค่าต่างๆ (เพื่อนที่ทำระบบเสียงหรือ UI จะมาดึงค่าพวกนี้ไปใช้)
    public int bgmVolume;       // ความดังเพลงพื้นหลัง (0 - 100)
    public int sfxVolume;       // ความดังเสียงเอฟเฟกต์ (0 - 100)
    public int textSpeed;       // ความเร็วตัวหนังสือ (เช่น 1=ช้า, 2=ปกติ, 3=เร็ว)
    public boolean fullScreen;  // เล่นแบบเต็มจอหรือไม่

    // Constructor ตั้งค่าเริ่มต้น (Default Settings) เวลาเปิดเกมครั้งแรก
    public Game_confing() {
        this.bgmVolume = 80;
        this.sfxVolume = 80;
        this.textSpeed = 2;
        this.fullScreen = false;
    }
}
