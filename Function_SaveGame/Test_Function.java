package Function_SaveGame;

public class Test_Function {
    public static void main(String[] args) {
        System.out.println("--- เริ่มทดสอบระบบเซฟ ---");

        // 1. ทดสอบระบบ Global (สมมติเล่นจบได้รูป CG มา 1 รูป)
        GlobalSaveData global = SaveManager.loadGlobal();
        global.unlockedCGs.add(101); // ปลดล็อกรูปหมายเลข 101
        SaveManager.saveGlobal(global);

        // 2. ทดสอบระบบเนื้อเรื่อง (สมมติเล่นถึงบทที่ 2 แล้วกดเซฟลงช่องที่ 1)
        GameSaveData mySave = new GameSaveData(
            "บทที่ 2: งานโรงเรียน", // ชื่อบท
            "Makoto",            // ชื่อผู้เล่น
            15,                  // ID ฉากปัจจุบัน
            80,                  // คะแนนสาว A
            50                   // คะแนนสาว B
        );
        SaveManager.saveSlot(mySave, 1);

        System.out.println("\n--- ทดสอบระบบโหลด ---");

        // 3. ทดลองโหลดข้อมูลช่องที่ 1 กลับมาดู
        GameSaveData loadedData = SaveManager.loadSlot(1);
        if (loadedData != null) {
            System.out.println("โหลดสำเร็จ!");
            System.out.println("เวลาที่เซฟ: " + loadedData.saveDate);
            System.out.println("ชื่อผู้เล่น: " + loadedData.playerName);
            System.out.println("คะแนนสาว A: " + loadedData.affectionGirlA);
        }
    }
}