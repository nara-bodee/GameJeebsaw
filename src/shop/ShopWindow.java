package shop;

import core.Player;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ShopWindow extends JDialog {

    private Player player; 
    private JLayeredPane layeredPane;
    private JPanel overlay;
    private JButton buyButton;
    private Item selectedItem = null;
    private JPanel selectedCard = null;

    // ================= MODEL =================
    class Item {
        String name;
        int price; // เก็บไว้เป็นข้อมูลประดับเฉยๆ
        String imagePath;
        int quantity;        
        CircleBadge badge;   

        Item(String name, int price, String imagePath) {
            this.name = name;
            this.price = price;
            this.imagePath = imagePath;
            this.quantity = 5;   
        }
    }

    private Item[] items = {
            new Item("ดอกกุหลาบ", 100, "../images/rose.png"),
            new Item("สมุดคณิตศาสตร์", 100, "../images/book.png"),
            new Item("เลม่อนโซดาสุดซ่า", 50, "../images/lemon_soda.png"),
            new Item("ร่มคันใหญ่", 100, "../images/umbrella.png"),
            new Item("พวงกุญแจตุ๊กตา", 50, "../images/baby.png"),
            new Item("เพชรมายา", 1000000, "../images/Daimon.png")
    };

    public ShopWindow(JFrame parent, Player player) {
        super(parent, "ร้านค้า (หยิบฟรี)", true);
        this.player = player;
        
        setSize(1366, 768);
        setLocationRelativeTo(parent);
        
        UIManager.put("Label.font", new Font("TH Sarabun New", Font.PLAIN, 24));
        UIManager.put("Button.font", new Font("TH Sarabun New", Font.PLAIN, 24));

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        setContentPane(layeredPane);

        createOverlayLayer();
        overlay.setVisible(true);
    }

    private void createOverlayLayer() {
        overlay = new JPanel(null);
        overlay.setBounds(0, 0, 1366, 768);
        overlay.setBackground(new Color(0, 0, 0, 150));

        RoundedPanel popup = new RoundedPanel(40);
        popup.setLayout(null);
        popup.setBackground(new Color(255, 120, 160));
        popup.setBounds(333, 84, 700, 600);

        // เปลี่ยนหัวข้อเป็นคำต้อนรับแทนยอดเงิน
        JLabel welcomeLabel = new JLabel("ยินดีต้อนรับ! เลือกหยิบสินค้าได้เลย");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 32));
        welcomeLabel.setBounds(30, 20, 400, 40);
        popup.add(welcomeLabel);

        JButton close = createCloseButton();
        close.setBounds(640, 15, 40, 40);
        close.addActionListener(e -> dispose());
        popup.add(close);

        int xStart = 80, yStart = 80, gapX = 200, gapY = 220, index = 0;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                if (index < items.length) {
                    popup.add(createItemCard(items[index], xStart + col * gapX, yStart + row * gapY));
                    index++;
                }
            }
        }

        buyButton = createStyledBuyButton();
        buyButton.setText("TAKE IT"); // เปลี่ยนชื่อปุ่มให้เข้ากับการหยิบฟรี
        buyButton.setBounds(280, 520, 140, 45);
        buyButton.setEnabled(false);
        buyButton.addActionListener(e -> buyItem());
        popup.add(buyButton);

        overlay.add(popup);
        layeredPane.add(overlay, Integer.valueOf(1));
    }

    private JPanel createItemCard(Item item, int x, int y) {
        int CARD_WIDTH = 150, CARD_HEIGHT = 200;
        JPanel card = new RoundedPanel(30);
        card.setLayout(null);
        card.setBackground(new Color(255, 150, 180));
        card.setBounds(x, y, CARD_WIDTH, CARD_HEIGHT);

        // รูปสินค้า
        try {
            ImageIcon icon = new ImageIcon(item.imagePath);
            Image img = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(img));
            imageLabel.setBounds((CARD_WIDTH - 90) / 2, 15, 90, 90);
            card.add(imageLabel);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("No Image", SwingConstants.CENTER);
            errorLabel.setBounds(0, 15, CARD_WIDTH, 90);
            card.add(errorLabel);
        }

        JLabel nameLabel = new JLabel(item.name, SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 24));
        nameLabel.setBounds(10, 120, CARD_WIDTH - 20, 25);
        card.add(nameLabel);

        // แสดงราคาเป็น 0 G หรือ "FREE"
        JLabel priceLabel = new JLabel("FREE", SwingConstants.CENTER);
        priceLabel.setOpaque(true);
        priceLabel.setBackground(new Color(150, 255, 150)); // สีเขียวให้ดูว่าฟรี
        priceLabel.setBounds((CARD_WIDTH - 90) / 2, 155, 90, 32);
        card.add(priceLabel);

        CircleBadge badge = new CircleBadge(String.valueOf(item.quantity));
        badge.setBounds(CARD_WIDTH - 45, 8, 35, 35);
        item.badge = badge;
        card.add(badge);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (selectedCard != null) selectedCard.setBackground(new Color(255, 150, 180));
                selectedCard = card;
                selectedItem = item;
                card.setBackground(new Color(255, 190, 210));
                buyButton.setEnabled(true);
            }
        });

        return card;
    }

    private void buyItem() {
        if (selectedItem == null) return;
        
        // เช็คแค่ว่าของในร้านหมดหรือยัง
        if (selectedItem.quantity <= 0) {
            JOptionPane.showMessageDialog(this, "สินค้าชิ้นนี้หมดแล้ว!");
            return;
        }

        // --- ตัดระบบ Money ออกแล้ว ---
        selectedItem.quantity--;
        player.addItem(selectedItem.name); // ส่งของเข้า Player

        selectedItem.badge.setText(String.valueOf(selectedItem.quantity));
        if (selectedItem.quantity == 0) selectedItem.badge.setVisible(false);

        JOptionPane.showMessageDialog(this, "คุณได้รับ: " + selectedItem.name);
        
        // Reset Selection
        if(selectedCard != null) selectedCard.setBackground(new Color(255, 150, 180));
        selectedCard = null;
        selectedItem = null;
        buyButton.setEnabled(false);
    }

    // ================= UI HELPER CLASSES (เหมือนเดิม) =================
    private JButton createStyledBuyButton() {
        return new JButton("TAKE") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? new Color(150, 255, 150) : Color.GRAY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
                super.paintComponent(g);
                g2.dispose();
            }
            { setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); }
        };
    }

    private JButton createCloseButton() {
        JButton btn = new JButton() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.drawString("X", 13, 25);
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        return btn;
    }

    class RoundedPanel extends JPanel {
        int radius;
        RoundedPanel(int radius) { this.radius = radius; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }
    }

    class CircleBadge extends JLabel {
        public CircleBadge(String text) {
            super(text, SwingConstants.CENTER);
            setForeground(Color.WHITE);
            setFont(new Font("TH Sarabun New", Font.BOLD, 18));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 0, 100));
            g2.fillOval(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    }
}