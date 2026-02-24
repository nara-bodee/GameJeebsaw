import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class ShopWindow {
    private JFrame frame;
    private JLayeredPane layeredPane;
    private JPanel overlay;
    private JLabel moneyLabel;
    private JButton buyButton;
    private HashMap<Item, Integer> inventory = new HashMap<>();
    private int money = 1000;
    private Item selectedItem = null;
    private JPanel selectedCard = null;

    // ================= MODEL =================
class Item {
    String name;
    int price;
    String imagePath;
    int quantity;        // 🔥 จำนวนสินค้า
    CircleBadge badge;   // 🔥 อ้างอิง badge

    Item(String name, int price, String imagePath) {
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.quantity = 5;   // 🔥 เริ่มต้น 5 ชิ้น
    }
}

    private Item[] items = {
            new Item("ดอกกุหลาบ", 100, "images/rose.png"),
            new Item("สมุดคณิตศาสตร์", 100, "images/book.png"),
            new Item("เลม่อนโซดาสุดซ่า", 50, "images/lemon_soda.png"),
            new Item("ร่มคันใหญ่", 100, "images/umbrella.png"),
            new Item("พวงกุญแจตุ๊กตา", 50, "images/baby.png"),
            new Item("เพชรมายา", 1000000, "images/Daimon.png")
    };

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ShopWindow().createUI());
    }

    public void createUI() {
        UIManager.put("Label.font", new Font("TH Sarabun New", Font.PLAIN, 20));
        UIManager.put("Button.font", new Font("TH Sarabun New", Font.PLAIN, 20));
        UIManager.put("OptionPane.messageFont", new Font("TH Sarabun New", Font.PLAIN, 20));
        UIManager.put("OptionPane.buttonFont", new Font("TH Sarabun New", Font.PLAIN, 20));
        frame = new JFrame("SHOP");
        frame.setSize(1366, 768);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);


        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        frame.setContentPane(layeredPane);

        createMainLayer();
        createOverlayLayer();

        frame.setVisible(true);
    }

    // ================= MAIN LAYER =================
    private void createMainLayer() {

        JPanel mainPanel = new JPanel(null);
        mainPanel.setBounds(0, 0, 1366, 768);

        JButton openShop = new JButton("🛒") {

    private boolean hover = false;
    private boolean pressed = false;

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        Color base = new Color(240, 120, 160);
        Color hoverColor = base.brighter();      // อ่อนขึ้น
        Color pressedColor = base;               // กดให้กลับสีปกติ

        if (pressed) {
            g2.setColor(pressedColor);
        } else if (hover) {
            g2.setColor(hoverColor);
        } else {
            g2.setColor(base);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);

        g2.dispose();
    }

    {
        // Mouse Effect
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            public void mouseExited(MouseEvent e) {
                hover = false;
                pressed = false;
                repaint();
            }

            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }
};

        openShop.setBounds(50, 300, 120, 60);
        openShop.setFont(new Font("SansSerif", Font.PLAIN, 26));
        openShop.setFocusPainted(false);
        openShop.setContentAreaFilled(false);
        openShop.setBorderPainted(false);
        openShop.setOpaque(false);
        openShop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openShop.addActionListener(e -> overlay.setVisible(true));


        mainPanel.add(openShop);
        layeredPane.add(mainPanel, Integer.valueOf(0));

        JButton openInventory = new JButton("INVENTORY");
        openInventory.setBounds(50, 380, 150, 60);
        openInventory.setFont(new Font("TH Sarabun New", Font.BOLD, 20));
        openInventory.addActionListener(e -> showInventory());
        // ซ่อนปุ่ม -----------------------------------------------------------------------------------------
        openInventory.setVisible(false); // ซ่อนปุ่ม -----------------------------------------------------------------------------------------
        // ซ่อนปุ่ม -----------------------------------------------------------------------------------------
        mainPanel.add(openInventory);
    }

    // ================= OVERLAY =================
    private void createOverlayLayer() {

        overlay = new JPanel(null);
        overlay.setBounds(0, 0, 1366, 768);
        overlay.setBackground(new Color(0, 0, 0, 120));
        overlay.setVisible(false);

        RoundedPanel popup = new RoundedPanel(40);
        popup.setLayout(null);
        popup.setBackground(new Color(255, 120, 160));
        popup.setBounds(350, 80, 700, 600);

        // Close button
        JButton close = createCloseButton();
        close.setBounds(640, 15, 40, 40);
        close.addActionListener(e -> overlay.setVisible(false));
        popup.add(close);

        // Money label
        moneyLabel = new JLabel("Money: " + money);
        moneyLabel.setForeground(Color.WHITE);
        moneyLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 28));
        moneyLabel.setBounds(30, 20, 300, 40);
        popup.add(moneyLabel);

        // Create items
        int xStart = 80;
        int yStart = 80;
        int gapX = 200;
        int gapY = 220;

        int index = 0;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {

                int x = xStart + col * gapX;
                int y = yStart + row * gapY;

                popup.add(createItemCard(items[index], x, y));
                index++;
            }
        }

        // BUY button
        buyButton = new JButton("BUY") {

        private boolean hover = false;
        private boolean pressed = false;

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        Color base = new Color(255, 220, 120);      // สีทอง
        Color hoverColor = new Color(255, 235, 160); // อ่อนลง
        Color pressedColor = new Color(230, 200, 100);
        Color disabledColor = new Color(180, 180, 180);

        if (!isEnabled()) {
            g2.setColor(disabledColor);
        } else if (pressed) {
            g2.setColor(pressedColor);
        } else if (hover) {
            g2.setColor(hoverColor);
        } else {
            g2.setColor(base);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);

        g2.dispose();
    }

    {
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            public void mouseExited(MouseEvent e) {
                hover = false;
                pressed = false;
                repaint();
            }

            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }
};

buyButton.setBounds(290, 520, 140, 45);
buyButton.setFont(new Font("TH Sarabun New", Font.BOLD, 20));
buyButton.setFocusPainted(false);
buyButton.setContentAreaFilled(false);
buyButton.setBorderPainted(false);
buyButton.setOpaque(false);
buyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
buyButton.setEnabled(false);
buyButton.addActionListener(e -> buyItem());

popup.add(buyButton);

        overlay.add(popup);
        layeredPane.add(overlay, Integer.valueOf(1));
    }

    // ================= ITEM CARD =================
   private JPanel createItemCard(Item item, int x, int y) {

    int CARD_WIDTH = 150;
    int CARD_HEIGHT = 200;

    JPanel card = new RoundedPanel(30);
    card.setLayout(null);
    card.setBackground(new Color(255, 150, 180));
    card.setBounds(x, y, CARD_WIDTH, CARD_HEIGHT);

    Color normal = new Color(255, 150, 180);
    Color hover = new Color(255, 170, 200);
    Color selected = new Color(255, 190, 210);

    // ================= รูป =================
    ImageIcon icon = new ImageIcon(getClass().getResource(item.imagePath));
    Image img = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
    JLabel image = new JLabel(new ImageIcon(img));
    image.setBounds((CARD_WIDTH - 90) / 2, 15, 90, 90);

    // ================= ชื่อสินค้า =================
    JLabel nameLabel = new JLabel(item.name, SwingConstants.CENTER);
    nameLabel.setForeground(Color.WHITE);
    nameLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 20));
    nameLabel.setBounds(10, 120, CARD_WIDTH - 20, 25);

    // ================= ราคา =================
    JLabel price = new JLabel(item.price + " G", SwingConstants.CENTER);
    price.setOpaque(true);
    price.setBackground(new Color(255, 220, 120));
    price.setBounds((CARD_WIDTH - 90) / 2, 155, 90, 32);

    // ================= Badge =================
    CircleBadge badge = new CircleBadge(String.valueOf(item.quantity));
    badge.setBounds(CARD_WIDTH - 45, 8, 35, 35);

    item.badge = badge;

    card.add(image);
    card.add(nameLabel);
    card.add(price);
    card.add(badge);

    // ================= Hover =================
    card.addMouseListener(new MouseAdapter() {

        public void mouseEntered(MouseEvent e) {
            if (card != selectedCard)
                card.setBackground(hover);
        }

        public void mouseExited(MouseEvent e) {
            if (card != selectedCard)
                card.setBackground(normal);
        }

        public void mouseClicked(MouseEvent e) {

            if (selectedCard != null)
                selectedCard.setBackground(normal);

            card.setBackground(selected);
            selectedCard = card;
            selectedItem = item;
            buyButton.setEnabled(true);
        }
    });

    return card;
}

   // ================= BUY =================
private void buyItem() {

    if (selectedItem == null) return;

    if (selectedItem.quantity <= 0) {
        JOptionPane.showMessageDialog(frame, "สินค้าหมดแล้ว!");
        return;
    }

    if (money >= selectedItem.price) {

        money -= selectedItem.price;
        selectedItem.quantity--;  // 🔥 ลดจำนวน
        inventory.put(selectedItem,
        inventory.getOrDefault(selectedItem, 0) + 1);
        moneyLabel.setText("Money: " + money);
        
        // 🔥 อัปเดต badge
        selectedItem.badge.setText(String.valueOf(selectedItem.quantity));

        if (selectedItem.quantity == 0) {
            selectedItem.badge.setVisible(false);
        }

        JOptionPane.showMessageDialog(frame,
                "ซื้อสำเร็จ: " + selectedItem.name);

        selectedCard.setBackground(new Color(255, 150, 180));
        selectedCard = null;
        selectedItem = null;
        buyButton.setEnabled(false);

    } else {
        JOptionPane.showMessageDialog(frame, "เงินไม่พอ!");
    }
    
}
    // ================= CLOSE BUTTON =================
    private JButton createCloseButton() {

        JButton btn = new JButton() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.BLACK);
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("TH Sarabun New", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("X")) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 3;
                g2.drawString("X", x, y);
            }
        };

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        return btn;
    }
    private void showInventory() {

    if (inventory.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Inventory ว่าง");
        return;
    }

    StringBuilder sb = new StringBuilder("INVENTORY\n\n");

    for (Item item : inventory.keySet()) {
        sb.append(item.name)
          .append(" x ")
          .append(inventory.get(item))
          .append("\n");
    }

    JOptionPane.showMessageDialog(frame, sb.toString());
}
    // ================= ROUNDED PANEL =================
    class RoundedPanel extends JPanel {

        int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }
    }
    class CircleBadge extends JLabel {

    public CircleBadge(String text) {
        super(text, SwingConstants.CENTER);
        setForeground(Color.WHITE);
        setFont(new Font("TH Sarabun New", Font.BOLD, 20));
        setPreferredSize(new Dimension(35, 35));
        setSize(35, 35);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // วาดวงกลมพื้นหลัง
        g2.setColor(new Color(255, 0, 100));
        g2.fillOval(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}

}