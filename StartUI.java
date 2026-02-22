import java.awt.*;
import javax.swing.*;

public class StartUI extends JFrame {

    public StartUI() {

        setTitle("Start UI");
        setSize(800, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // ใช้กำหนดตำแหน่งเอง

        // ===== Panel ส่วนบน (สี FF7E7E) =====
        JPanel imagePanel = new JPanel();
        imagePanel.setBackground(new Color(0xFF7E7E));
        imagePanel.setBounds(150, 30, 500, 150); // ปรับตำแหน่ง/ขนาดได้
        imagePanel.setLayout(new BorderLayout());

        // ===== ใส่รูปภาพ =====
        ImageIcon icon = new ImageIcon("image.png"); // ใส่ path รูป
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);

        imagePanel.add(imageLabel, BorderLayout.CENTER);

        add(imagePanel);

        // ===== ปุ่ม =====
        JButton startBtn = new JButton("Start");
        startBtn.setBounds(330, 220, 120, 40);
        add(startBtn);

        JButton exitBtn = new JButton("Exit");
        exitBtn.setBounds(330, 280, 120, 40);
        add(exitBtn);

        setVisible(true);
    }

    public static void main(String[] args) {
        new StartUI();
    }
}
