import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class GameSettings {
    private int screenWidth;
    private int screenHeight;
    private boolean isFullScreen;
    private final String CONFIG_FILE = "settings.properties"; // ไฟล์สำหรับเซฟการตั้งค่า

    // ใช้ Singleton Pattern เพื่อให้ทั้งเกมเรียกใช้ Settings ตัวเดียวกัน
    private static GameSettings instance;

    private GameSettings() {
        loadSettings(); // โหลดค่าทันทีที่เริ่มระบบ
    }

    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }

    // ฟังก์ชันสำหรับให้ UI เรียกใช้เมื่อผู้เล่นกดเปลี่ยนขนาดจอ
    public void applyResolution(int width, int height, boolean fullscreen) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.isFullScreen = fullscreen;
        saveSettings(); // เซฟลงไฟล์อัตโนมัติ
        
        System.out.println("ระบบ: อัปเดตขนาดจอเป็น " + width + "x" + height + " | Fullscreen: " + fullscreen);
        // หมายเหตุ: ตรงนี้เพื่อนคนที่ทำคลาส Main/UI จะต้องมาดึงค่า width, height ไป `setSize()` ให้หน้าต่าง JFrame อีกที
    }

    // บันทึกการตั้งค่าลงไฟล์
    private void saveSettings() {
        Properties prop = new Properties();
        prop.setProperty("width", String.valueOf(screenWidth));
        prop.setProperty("height", String.valueOf(screenHeight));
        prop.setProperty("fullscreen", String.valueOf(isFullScreen));

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            prop.store(out, "Game Resolution Settings");
        } catch (IOException e) {
            System.out.println("เกิดข้อผิดพลาดในการบันทึกไฟล์ตั้งค่า: " + e.getMessage());
        }
    }

    // โหลดการตั้งค่าจากไฟล์
    private void loadSettings() {
        Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            prop.load(in);
            this.screenWidth = Integer.parseInt(prop.getProperty("width", "1280"));
            this.screenHeight = Integer.parseInt(prop.getProperty("height", "720"));
            this.isFullScreen = Boolean.parseBoolean(prop.getProperty("fullscreen", "false"));
        } catch (IOException e) {
            // ถ้าไม่เคยมีไฟล์ตั้งค่ามาก่อน ให้ใช้ค่าเริ่มต้น
            this.screenWidth = 1280;
            this.screenHeight = 720;
            this.isFullScreen = false;
        }
    }

    // --- Getters ให้เพื่อนๆ ดึงค่าไปใช้ ---
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public boolean isFullScreen() { return isFullScreen; }
}