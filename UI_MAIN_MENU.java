import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UI_MAIN_MENU extends JFrame {

    // ====== เตรียมไว้สำหรับใส่ภาพในอนาคต ======
    private Image backgroundImage;   // ใส่ BG ภายหลัง
    private Image logoImage;         // ใส่โลโก้ภายหลัง

    public UI_MAIN_MENU() {
        setTitle("เกมจีบสาว");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Panel หลัก
        MenuPanel panel = new MenuPanel();
        panel.setLayout(null);
        add(panel);

        // ====== ปุ่ม ======
        JButton btnStart = createButton("Start Game");
        JButton btnLoad = createButton("Load Save");
        JButton btnExit = createButton("Exit");

        btnStart.setBounds(80, 250, 250, 60);
        btnLoad.setBounds(80, 330, 250, 60);
        btnExit.setBounds(80, 410, 250, 60);

        // ====== Action ======
        btnStart.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Start Game Clicked!");
        });

        btnLoad.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Load Save Clicked!");
        });

        btnExit.addActionListener(e -> {
            System.exit(0);
        });

        panel.add(btnStart);
        panel.add(btnLoad);
        panel.add(btnExit);

        setVisible(true);
    }

    // ====== สร้างปุ่มสไตล์ชมพู ======
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Serif", Font.ITALIC, 24));
        button.setForeground(Color.BLACK);

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // ไล่สีชมพู
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255, 182, 193),
                        0, c.getHeight(), new Color(255, 105, 180)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 40, 40);

                super.paint(g2, c);
                g2.dispose();
            }
        });

        return button;
    }

    // ====== Panel สำหรับรองรับ BG/Logo ======
    class MenuPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // ====== ใส่ BG ในอนาคต ======
            // if (backgroundImage != null) {
            //     g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            // }

            // ====== ใส่โลโก้ในอนาคต ======
            // if (logoImage != null) {
            //     g.drawImage(logoImage, 50, 50, 300, 150, this);
            // }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UI_MAIN_MENU::new);
    }
}