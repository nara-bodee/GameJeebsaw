import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UI_SETTING extends JFrame {

    public UI_SETTING() {
        setTitle("Settings Menu");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        add(layeredPane);

        // ===== Background Placeholder =====
        JPanel bg = new JPanel();
        bg.setBackground(new Color(200, 200, 200));
        bg.setBounds(0, 0, 1100, 700);
        layeredPane.add(bg, Integer.valueOf(0));

        // ===== Popup =====
        JPanel popup = new GradientPopup();
        popup.setLayout(null);
        popup.setOpaque(false);
        popup.setBounds(330, 70, 450, 560);
        layeredPane.add(popup, Integer.valueOf(2));

        // ===== Close Button (วงกลมดำ X ขาว) =====
        JButton closeBtn = new JButton("X");
        closeBtn.setBounds(380, 20, 45, 45);
        closeBtn.setFocusPainted(false);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setUI(new CircleButtonUI(Color.BLACK));
        closeBtn.addActionListener(e -> popup.setVisible(false));
        popup.add(closeBtn);

        // ===== ปุ่มเมนู =====
        JButton btnSave = createMenuButton("Save game");
        JButton btnLoad = createMenuButton("Load save");
        JButton btnExit = createMenuButton("Exit");

        btnSave.setBounds(100, 160, 250, 70);
        btnLoad.setBounds(100, 260, 250, 70);
        btnExit.setBounds(100, 360, 250, 70);

        popup.add(btnSave);
        popup.add(btnLoad);
        popup.add(btnExit);

        setVisible(true);
    }

    // ===== Popup ไล่สี + ขอบดำหนา =====
    class GradientPopup extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(180, 60, 120),
                    0, getHeight(), new Color(255, 120, 150)
            );

            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 80, 80);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 80, 80);
        }
    }

    // ===== ปุ่มเมนูสีม่วงไล่สี =====
    private JButton createMenuButton(String text) {

        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setForeground(Color.BLACK);

        // ฟอนต์ Script (ถ้ามี)
        Font scriptFont = new Font("Serif", Font.ITALIC, 28);
        button.setFont(scriptFont);

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(120, 80, 255),
                        c.getWidth(), 0, new Color(160, 100, 255)
                );

                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 60, 60);

                super.paint(g2, c);
                g2.dispose();
            }
        });

        return button;
    }

    // ===== ปุ่มวงกลม =====
    class CircleButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private Color color;

        public CircleButtonUI(Color color) {
            this.color = color;
        }

        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);
            g2.fillOval(0, 0, c.getWidth(), c.getHeight());

            super.paint(g2, c);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UI_SETTING::new);
    }
}