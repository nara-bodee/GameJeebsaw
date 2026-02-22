import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UI_Save extends JFrame {

    public UI_Save() {
        setTitle("SAVE / LOAD UI");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        add(layeredPane);

        // ===== Background Placeholder =====
        JPanel bg = new JPanel();
        bg.setBackground(new Color(210, 210, 210));
        bg.setBounds(0, 0, 1100, 700);
        layeredPane.add(bg, Integer.valueOf(0));

        // ===== Popup =====
        JPanel popup = new GradientPopup();
        popup.setLayout(null);
        popup.setOpaque(false);
        popup.setBounds(200, 70, 700, 560);
        layeredPane.add(popup, Integer.valueOf(2));

        // ===== Close Button (วงกลมดำ X ขาว) =====
        JButton closeBtn = new JButton("X");
        closeBtn.setBounds(620, 20, 50, 50);
        closeBtn.setFocusPainted(false);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 22));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setUI(new CircleButtonUI(Color.BLACK));
        closeBtn.addActionListener(e -> popup.setVisible(false));
        popup.add(closeBtn);

        // ===== Save Slots =====
        JButton slot1 = createSlotButton("17.00 / 22 / 2 / 2565");
        JButton slot2 = createSlotButton("18.00 / 21 / 1 / 2565");
        JButton slot3 = createSlotButton("20.00 / 25 / 31 / 2564");

        slot1.setBounds(120, 150, 460, 80);
        slot2.setBounds(120, 270, 460, 80);
        slot3.setBounds(120, 390, 460, 80);

        popup.add(slot1);
        popup.add(slot2);
        popup.add(slot3);

        setVisible(true);
    }

    // ===== Popup ไล่สีชมพู-ม่วง + ขอบดำหนา =====
    class GradientPopup extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(170, 60, 120),
                    getWidth(), getHeight(),
                    new Color(255, 120, 150)
            );

            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 90, 90);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 90, 90);
        }
    }

    // ===== Save Slot Button =====
    private JButton createSlotButton(String text) {

        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Serif", Font.ITALIC, 28));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            public void paint(Graphics g, JComponent c) {

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(100, 80, 220),
                        c.getWidth(), 0,
                        new Color(160, 110, 255)
                );

                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 60, 60);

                super.paint(g2, c);
                g2.dispose();
            }
        });

        return button;
    }

    // ===== วงกลมปุ่ม X =====
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
        SwingUtilities.invokeLater(UI_Save::new);
    }
}