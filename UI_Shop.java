import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UI_Shop extends JFrame {

    private Image itemImage; // ใส่รูปในอนาคต

    public UI_Shop() {
        setTitle("Dating Game - Shop");
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

        // ===== Cart Button (Left Circle) =====
        JButton cartBtn = new JButton("🛒");
        cartBtn.setFont(new Font("SansSerif", Font.PLAIN, 26));
        cartBtn.setBounds(70, 300, 80, 80);
        cartBtn.setFocusPainted(false);
        cartBtn.setBackground(new Color(240, 120, 160));
        cartBtn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        layeredPane.add(cartBtn, Integer.valueOf(2));

        // ===== Popup =====
        JPanel popup = new RoundedPopup();
        popup.setLayout(null);
        popup.setOpaque(false);
        popup.setBounds(280, 60, 650, 580);
        layeredPane.add(popup, Integer.valueOf(3));

        // ===== Close Button =====
        JButton close = new JButton("X");
        close.setBounds(575, 15, 45, 45);
        close.setForeground(Color.WHITE);
        close.setFont(new Font("SansSerif", Font.BOLD, 18));
        close.setFocusPainted(false);
        close.setBackground(Color.BLACK);
        close.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        close.addActionListener(e -> popup.setVisible(false));
        popup.add(close);

        // ===== Grid Items =====
        int startX = 90;
        int startY = 100;
        int gapX = 180;
        int gapY = 220;

        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 3; c++) {

                int x = startX + (c * gapX);
                int y = startY + (r * gapY);

                JPanel item = createItemCard();
                item.setBounds(x, y, 130, 170);
                popup.add(item);
            }
        }

        setVisible(true);
    }

    // ===== Popup Panel (Gradient + Thick Border) =====
    class RoundedPopup extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 120, 160),
                    0, getHeight(), new Color(220, 80, 130)
            );

            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 60, 60);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 60, 60);
        }
    }

    // ===== Item Card =====
    private JPanel createItemCard() {

        JPanel panel = new JPanel(null);
        panel.setOpaque(false);

        // ===== Image Box =====
        JPanel imageBox = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255, 190, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.setColor(Color.BLACK);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);

                g2.drawString("ITEM", 35, 45);

                // ===== ใส่รูปในอนาคต =====
                // if (itemImage != null) {
                //     g2.drawImage(itemImage, 5, 5, getWidth()-10, getHeight()-10, this);
                // }
            }
        };
        imageBox.setBounds(0, 0, 130, 90);
        panel.add(imageBox);

        // ===== Badge Circle =====
        JLabel badge = new JLabel("1", SwingConstants.CENTER);
        badge.setBounds(95, -8, 35, 35);
        badge.setOpaque(true);
        badge.setBackground(new Color(255, 40, 100));
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 16));
        badge.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        panel.add(badge);

        // ===== Price =====
        JLabel price = new JLabel("150", SwingConstants.CENTER);
        price.setBounds(30, 95, 70, 28);
        price.setOpaque(true);
        price.setBackground(new Color(255, 215, 120));
        price.setFont(new Font("SansSerif", Font.BOLD, 16));
        price.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        panel.add(price);

        // ===== Buy Button =====
        JButton buy = new JButton("buy");
        buy.setBounds(40, 130, 60, 28);
        buy.setFocusPainted(false);
        buy.setBackground(new Color(130, 110, 255));
        buy.setForeground(Color.WHITE);
        buy.setFont(new Font("SansSerif", Font.BOLD, 14));
        buy.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        panel.add(buy);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UI_Shop::new);
    }
}