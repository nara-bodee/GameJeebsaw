package main;

import core.Player;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ShopPanel extends JPanel {

    private MainFrame mainFrame;
    private Player player; 
    private JButton buyButton;
    private JButton closeButton;
    private Item selectedItem = null;
    private JPanel selectedCard = null;
    private boolean hasPurchased = false;
    private String currentEventId = null;

    // ================= MODEL =================
    class Item {
        String name;
        int price;
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
            new Item("ช่อดอกไม้", 100, "../images/rose.png"),
            new Item("สมุดสรุปคณิตศาสตร์", 100, "../images/book.png"),
            new Item("เลมอนโซดา", 50, "../images/lemon_soda.png"),
            new Item("ร่มคันใหญ่", 100, "../images/umbrella.png"),
            new Item("พวงกุญแจตุ๊กตา", 50, "../images/baby.png"),
            new Item("เพชรมายา", 1000000, "../images/Daimon.png")
    };

    public ShopPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        
        UIManager.put("Label.font", new Font("TH Sarabun New", Font.PLAIN, 24));
        UIManager.put("Button.font", new Font("TH Sarabun New", Font.PLAIN, 24));

        setLayout(null);
        setBackground(new Color(0, 0, 0, 150));
    }
    
    public void refreshShop(Player player, String eventId) {
        this.player = player;
            this.currentEventId = eventId;
        this.hasPurchased = false;
        removeAll();
        createShopUI();
        revalidate();
        repaint();
    }

    private void createShopUI() {
        RoundedPanel popup = new RoundedPanel(40);
        popup.setLayout(null);
        popup.setBackground(new Color(255, 120, 160));
        
        int popupWidth = 700;
        int popupHeight = 600;
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        if (panelWidth <= 0) panelWidth = 1280;
        if (panelHeight <= 0) panelHeight = 720;
        
        int x = (panelWidth - popupWidth) / 2;
        int y = (panelHeight - popupHeight) / 2;
        
        popup.setBounds(x, y, popupWidth, popupHeight);

        JLabel welcomeLabel = new JLabel("ยินดีต้อนรับ! เลือกหยิบสินค้าได้เลย");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 32));
        welcomeLabel.setBounds(30, 20, 400, 40);
        popup.add(welcomeLabel);

        closeButton = createCloseButton();
        closeButton.setBounds(640, 15, 40, 40);
        closeButton.setEnabled(false);
        closeButton.addActionListener(e -> mainFrame.returnToGame());
        popup.add(closeButton);

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
        buyButton.setText("TAKE IT");
        buyButton.setBounds(280, 520, 140, 45);
        buyButton.setEnabled(false);
        buyButton.addActionListener(e -> buyItem());
        popup.add(buyButton);

        add(popup);
    }

    private JPanel createItemCard(Item item, int x, int y) {
        int CARD_WIDTH = 150, CARD_HEIGHT = 200;
        JPanel card = new RoundedPanel(30);
        card.setLayout(null);
        card.setBackground(new Color(255, 150, 180));
        card.setBounds(x, y, CARD_WIDTH, CARD_HEIGHT);

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

        JLabel priceLabel = new JLabel("FREE", SwingConstants.CENTER);
        priceLabel.setOpaque(true);
        priceLabel.setBackground(new Color(150, 255, 150));
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
        
        if (selectedItem.quantity <= 0) {
            JOptionPane.showMessageDialog(this, "สินค้าชิ้นนี้หมดแล้ว!");
            return;
        }
        
        // ตรวจสอบว่าต้องซื้อของที่ถูกต้องหรือไม่สำหรับอีเวนต์นี้
        String requiredItem = getRequiredItemForEvent(currentEventId);
        if (requiredItem != null && !requiredItem.equals(selectedItem.name)) {
                JOptionPane.showMessageDialog(this, 
                    "เดี๋ยวนะ! ตอนนี้ต้องซื้อ " + requiredItem + " ก่อน!",
                    "ซื้อของไม่ถูกต้อง", 
                    JOptionPane.WARNING_MESSAGE);
                return;
        }

        selectedItem.quantity--;
        player.addItem(selectedItem.name);

        selectedItem.badge.setText(String.valueOf(selectedItem.quantity));
        if (selectedItem.quantity == 0) selectedItem.badge.setVisible(false);

        JOptionPane.showMessageDialog(this, "คุณได้รับ: " + selectedItem.name);
        
        if(selectedCard != null) selectedCard.setBackground(new Color(255, 150, 180));
        selectedCard = null;
        selectedItem = null;
        buyButton.setEnabled(false);
        
        // เปิดใช้ปุ่มปิดหลังจากซื้อของ 1 ชิ้น
        hasPurchased = true;
        closeButton.setEnabled(true);
    }

    private String getRequiredItemForEvent(String eventId) {
        if ("DAY2_G".equals(eventId)) return "เลมอนโซดา";
        if ("DAY3_G".equals(eventId)) return "สมุดสรุปคณิตศาสตร์";
        if ("DAY4_G".equals(eventId)) return "ร่มคันใหญ่";
        if ("DAY5_G".equals(eventId)) return "พวงกุญแจตุ๊กตา";
        if ("DAY6_G".equals(eventId)) return "ช่อดอกไม้";
        return null;
    }

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
                Color bgColor = isEnabled() ? Color.BLACK : new Color(100, 100, 100);
                g2.setColor(bgColor);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.drawString("X", 13, 25);
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
