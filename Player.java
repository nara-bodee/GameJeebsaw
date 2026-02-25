import java.util.HashSet;
import java.util.Set;

public class Player {
    private int affectionScore = 0;
    private int teaseCount = 0; 
    
    // 🌟 ระบบกระเป๋าเก็บของ (ใช้เก็บชื่อไอเทมที่ซื้อจากร้าน)
    private Set<String> inventory = new HashSet<>(); 

    public void addAffection(int amount) { this.affectionScore += amount; }
    public void addTease() { this.teaseCount++; }
    
    public int getAffectionScore() { return affectionScore; }
    public int getTeaseCount() { return teaseCount; }

    // 🌟 ฟังก์ชันสำหรับให้ UI ร้านค้าของเพื่อนเรียกใช้ตอนกดซื้อ
    public void addItem(String itemName) { 
        this.inventory.add(itemName); 
    }
    
    // 🌟 ฟังก์ชันสำหรับเช็คว่ามีของชิ้นนั้นไหม
    public boolean hasItem(String itemName) { 
        return inventory.contains(itemName); 
    }
}