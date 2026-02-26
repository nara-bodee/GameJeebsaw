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
        gameDate currentDate = new gameDate(5, 10, 2024);
        System.out.println("วันที่ปัจจุบัน: " + currentDate);

        System.out.println("\n--- ทดสอบระบบโหลด ---");

        // 3. ทดลองโหลดข้อมูลช่องที่ 1 กลับมาดู
        GameSaveData loadedData = SaveManager.loadSlot(1);
        if (loadedData != null) {
            System.out.println("โหลดสำเร็จ!");
            System.out.println("เวลาที่เซฟ: " + loadedData.saveDate);
            System.out.println("ชื่อผู้เล่น: " + loadedData.playerName);
            System.out.println("คะแนนสาว A: " + loadedData.affectionGirlA);
            for (int i = 1; i <= 5; i++) {
            currentDate.advanceDay(); // สั่งให้ข้ามวัน
            System.out.println("ผ่านไป " + i + " วัน -> เป็น " + currentDate.toString());

            // ตัวอย่าง: เช็คเงื่อนไขเหตุการณ์ตามวันที่ (เช่น วันที่ 2 หรือ 30)
            if (currentDate.day == 2) {
                System.out.println("   [EVENT] วันเงินเดือนออก!");
            }
            if (currentDate.day == 30) {
                System.out.println("   [EVENT] วันสิ้นเดือน!");
            }
        }
        }
    }
}