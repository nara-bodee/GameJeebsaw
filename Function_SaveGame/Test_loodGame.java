package Function_SaveGame;

import java.util.Scanner; // 1. นำเข้าตัวรับข้อมูล

public class Test_loodGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // สร้างตัวรับข้อมูลจากคีย์บอร์ด

        System.out.println("--- เริ่มทดสอบระบบโหลด ---");
        System.out.print("กรุณาใส่หมายเลขช่องเซฟที่ต้องการโหลด (เช่น 1, 2, 3): ");
        
        // 2. รอรับตัวเลขจากผู้ใช้
        int slotNumber = scanner.nextInt(); 

        System.out.println("กำลังโหลดข้อมูลจาก Slot " + slotNumber + "...");

        // 3. ส่งตัวเลขที่รับมาเข้าไปในฟังก์ชันโหลด
        try {
            GameSaveData loadedData = SaveManager.loadSlot(slotNumber);

            if (loadedData != null) {
                System.out.println("\n--- โหลดสำเร็จ! ข้อมูลในไฟล์คือ ---");
                // หมายเหตุ: เช็คชื่อตัวแปรวันที่ให้ตรงกับใน GameSaveData (เช่น .date หรือ .saveDate)
                System.out.println("วันที่ในเกม: " + loadedData.saveDate); 
                System.out.println("ชื่อผู้เล่น: " + loadedData.playerName);
                System.out.println("บทที่: " + loadedData.chapterName);
                System.out.println("คะแนนสาว A: " + loadedData.affectionGirlA);
                System.out.println("คะแนนสาว B: " + loadedData.affectionGirlB);
            } else {
                System.out.println("\n[X] ไม่พบไฟล์เซฟในช่องที่ " + slotNumber + " (หรือไฟล์ว่างเปล่า)");
            }
        } catch (Exception e) {
            System.out.println("เกิดข้อผิดพลาดในการโหลด: " + e.getMessage());
        }
        
        scanner.close(); // ปิด Scanner เมื่อใช้เสร็จ
    }
}