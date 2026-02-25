import javax.swing.*;
import java.awt.*;

// 🌟 ให้เพื่อนทำ UI ร้านค้าในไฟล์นี้ได้เลย!
// ใช้ JDialog เพื่อให้มันเด้งทับหน้าจอเกมหลัก
public class ShopWindow extends JDialog {

    private Player player; // เอาไว้เรียกใช้ player.addItem(...)

    // Constructor รับค่าหน้าต่างหลัก (parent) และข้อมูลผู้เล่น (player)
    public ShopWindow(JFrame parent, Player player) {
        super(parent, "ร้านค้าสะดวกซื้อ", true); // true = ทำให้เกมหลักหยุดรอจนกว่าจะปิดหน้านี้
        this.player = player;

        setSize(400, 300);
        setLocationRelativeTo(parent); // ให้เด้งตรงกลางหน้าจอเกม
        setLayout(new BorderLayout());

        // ==========================================
        // 🛠️ เพื่อนของคุณสามารถลบโค้ดข้างล่างนี้ออก 
        // แล้วใส่ UI ร้านค้าของตัวเองได้ตามสบายเลย!
        // ==========================================

        JLabel titleLabel = new JLabel("ยินดีต้อนรับสู่ร้านค้า!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Leelawadee UI", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        JPanel itemPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // ตัวอย่างปุ่มกดซื้อไอเทม
        itemPanel.add(createBuyButton("เลมอนโซดา"));
        itemPanel.add(createBuyButton("สมุดสรุปคณิตศาสตร์"));
        itemPanel.add(createBuyButton("ร่มคันใหญ่"));
        itemPanel.add(createBuyButton("พวงกุญแจตุ๊กตา"));
        itemPanel.add(createBuyButton("ช่อดอกไม้"));

        add(itemPanel, BorderLayout.CENTER);

        // ปุ่มปิดร้าน
        JButton closeBtn = new JButton("ออกจากร้าน");
        closeBtn.addActionListener(e -> dispose()); // สั่งปิดหน้าต่างร้านค้า
        add(closeBtn, BorderLayout.SOUTH);
    }

    // ฟังก์ชันช่วยสร้างปุ่มซื้อของแบบง่ายๆ
    private JButton createBuyButton(String itemName) {
        JButton btn = new JButton("ซื้อ " + itemName);
        btn.setFont(new Font("Leelawadee UI", Font.PLAIN, 14));
        
        btn.addActionListener(e -> {
            // 🌟 คำสั่งสำคัญ! หยิบของใส่กระเป๋าผู้เล่น
            player.addItem(itemName);
            
            JOptionPane.showMessageDialog(this, "คุณได้รับ [" + itemName + "] แล้ว!", "ซื้อสำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            btn.setEnabled(false); // ซื้อแล้วปุ่มจะกดไม่ได้อีก
        });
        
        return btn;
    }
}