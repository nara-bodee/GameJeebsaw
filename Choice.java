public class Choice {
    private String text; 
    private int affectionChange; 
    private int teaseChange; 
    private String responseText; 
    private String outcomeBgPath; 
    private boolean openShop; // 🌟 เพิ่มตัวแปรเช็คว่าต้องเปิดหน้าร้านค้าไหม

    // Constructor เดิม (สำหรับตัวเลือกปกติที่ไม่ได้ไปร้าน)
    public Choice(String text, int affectionChange, int teaseChange, String responseText, String outcomeBgPath) {
        this(text, affectionChange, teaseChange, responseText, outcomeBgPath, false); // ค่าเริ่มต้นคือไม่เปิดร้าน
    }

    // Constructor ใหม่ 🌟 (สำหรับปุ่ม "วิ่งไปซื้อ" โดยเฉพาะ)
    public Choice(String text, int affectionChange, int teaseChange, String responseText, String outcomeBgPath, boolean openShop) {
        this.text = text;
        this.affectionChange = affectionChange;
        this.teaseChange = teaseChange;
        this.responseText = responseText;
        this.outcomeBgPath = outcomeBgPath;
        this.openShop = openShop;
    }

    public String getText() { return text; }
    public int getAffectionChange() { return affectionChange; }
    public int getTeaseChange() { return teaseChange; }
    public String getResponseText() { return responseText; }
    public String getOutcomeBgPath() { return outcomeBgPath; }
    public boolean isOpenShop() { return openShop; } // 🌟 Getter ให้หน้าต่างเกมเช็ค
}