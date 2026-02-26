package save;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SaveManager {

    private static final String SAVE_DIR = "saves/";
    private static final String EXTENSION = ".sav";

    private static void checkAndCreateDirectory() {
        File directory = new File(SAVE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private static String buildSlotPath(int slotNumber) {
        return SAVE_DIR + "slot_" + slotNumber + EXTENSION;
    }

    public static boolean saveSlot(GameSaveData data, int slotNumber) {
        checkAndCreateDirectory();
        String fileName = buildSlotPath(slotNumber);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(data);
            return true;
        } catch (Exception e) {
            System.out.println("[Error] บันทึกเกมล้มเหลว: " + e.getMessage());
            return false;
        }
    }

    public static GameSaveData loadSlot(int slotNumber) {
        String fileName = buildSlotPath(slotNumber);
        File file = new File(fileName);

        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (GameSaveData) ois.readObject();
        } catch (Exception e) {
            System.out.println("[Error] ไฟล์เซฟช่อง " + slotNumber + " เสียหาย หรืออ่านไม่ได้!");
            return null;
        }
    }

    public static boolean deleteSlot(int slotNumber) {
        String fileName = buildSlotPath(slotNumber);
        File file = new File(fileName);
        return file.exists() && file.delete();
    }

    public static boolean hasSlot(int slotNumber) {
        return new File(buildSlotPath(slotNumber)).exists();
    }

    public static GameSaveData[] getAllSaveSlotsInfo(int maxSlots) {
        GameSaveData[] allSaves = new GameSaveData[maxSlots];
        for (int i = 1; i <= maxSlots; i++) {
            allSaves[i - 1] = loadSlot(i);
        }
        return allSaves;
    }
}
