package Function_SaveGame;

import java.io.*;

public class SaveManager {

    private static final String SAVE_DIR = "saves/"; 
    private static final String EXTENSION = ".sav";
    private static final String GLOBAL_FILE = SAVE_DIR + "system_global.dat";
    private static final String CONFIG_FILE = SAVE_DIR + "config.dat";

    // ฟังก์ชันสร้างโฟลเดอร์อัตโนมัติ (ป้องกัน Error หาโฟลเดอร์ไม่เจอ)
    private static void checkAndCreateDirectory() {
        File directory = new File(SAVE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // ==========================================
    // ระบบเซฟ/โหลด สำหรับเนื้อเรื่อง (Slot)
    // ==========================================
    public static boolean saveSlot(GameSaveData data, int slotNumber) {
        checkAndCreateDirectory();
        String fileName = SAVE_DIR + "slot_" + slotNumber + EXTENSION;

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(data);
            System.out.println("[System] บันทึกเกมลงช่อง " + slotNumber + " สำเร็จ!");
            return true;
        } catch (IOException e) {
            System.out.println("[Error] บันทึกเกมล้มเหลว: " + e.getMessage());
            return false;
        }
    }

    public static GameSaveData loadSlot(int slotNumber) {
        String fileName = SAVE_DIR + "slot_" + slotNumber + EXTENSION;
        File file = new File(fileName);
        
        if (!file.exists()) {
            System.out.println("[System] ไม่มีไฟล์เซฟในช่อง " + slotNumber);
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (GameSaveData) ois.readObject();
        } catch (Exception e) {
            System.out.println("[Error] ไฟล์เซฟช่อง " + slotNumber + " เสียหาย หรืออ่านไม่ได้!");
            return null;
        }
    }

    // ==========================================
    // ระบบเซฟ/โหลด สำหรับข้อมูลส่วนกลาง (Global)
    // ==========================================
    public static void saveGlobal(GlobalSaveData globalData) {
        checkAndCreateDirectory();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GLOBAL_FILE))) {
            oos.writeObject(globalData);
            System.out.println("[System] บันทึกข้อมูล Global สำเร็จ!");
        } catch (IOException e) {
            System.out.println("[Error] บันทึก Global ล้มเหลว: " + e.getMessage());
        }
    }

    public static GlobalSaveData loadGlobal() {
        File file = new File(GLOBAL_FILE);
        if (!file.exists()) {
            // ถ้าเล่นครั้งแรกสุด จะยังไม่มีไฟล์ ให้สร้าง Object เปล่าๆ คืนไป
            return new GlobalSaveData(); 
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(GLOBAL_FILE))) {
            return (GlobalSaveData) ois.readObject();
        } catch (Exception e) {
            System.out.println("[Error] ไฟล์ Global เสียหาย สร้างไฟล์ใหม่แทนที่...");
            return new GlobalSaveData();
        }
    }

    // ==========================================
    // ฟังก์ชันที่ 1: ระบบลบไฟล์เซฟ (Delete Slot)
    // ==========================================
    // เพื่อนที่ทำปุ่ม "ลบเซฟ" จะเรียกใช้ฟังก์ชันนี้
    public static boolean deleteSlot(int slotNumber) {
        String fileName = SAVE_DIR + "slot_" + slotNumber + EXTENSION;
        File file = new File(fileName);
        
        if (file.exists()) {
            return file.delete(); // สั่งลบไฟล์และส่งค่า true กลับไปถ้าลบสำเร็จ
        }
        System.out.println("[System] ไม่พบไฟล์เซฟช่อง " + slotNumber + " ให้ลบ");
        return false;
    }

    // ==========================================
    // ฟังก์ชันที่ 2: ดึงข้อมูลเซฟทั้งหมดไปแสดงผล (Get All Saves Info)
    // ==========================================
    // เพื่อนที่ทำหน้าจอ "โหลดเกม" จะเรียกฟังก์ชันนี้ เพื่อเอาข้อมูลไปวาดปุ่ม 10 ช่อง
    public static GameSaveData[] getAllSaveSlotsInfo(int maxSlots) {
        GameSaveData[] allSaves = new GameSaveData[maxSlots];
        
        // วนลูปดึงข้อมูลตั้งแต่ช่อง 1 ถึง maxSlots
        for (int i = 1; i <= maxSlots; i++) {
            allSaves[i - 1] = loadSlot(i); // ถ้าช่องไหนไม่มีไฟล์ มันจะใส่ค่า null ให้เอง
        }
        return allSaves; // ส่ง Array ข้อมูลกลับไปให้ฝ่าย UI
    }

    // ==========================================
    // ฟังก์ชันที่ 3 & 4: เซฟและโหลดการตั้งค่า (Config)
    // ==========================================
    public static void saveConfig(Game_confing config) {
        checkAndCreateDirectory();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE))) {
            oos.writeObject(config);
            System.out.println("[System] บันทึกการตั้งค่า (Config) สำเร็จ!");
        } catch (IOException e) {
            System.out.println("[Error] บันทึกการตั้งค่าล้มเหลว: " + e.getMessage());
        }
    }

    public static Game_confing loadConfig() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            // ถ้าไม่เคยตั้งค่ามาก่อน ให้ส่งค่า Default กลับไป
            return new Game_confing(); 
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CONFIG_FILE))) {
            return (Game_confing) ois.readObject();
        } catch (Exception e) {
            System.out.println("[Error] ไฟล์ Config เสียหาย ใช้ค่าเริ่มต้นแทน...");
            return new Game_confing();
        }
    }
    
}
