package ui;

import core.Player;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import story.Choice;
import story.EventManager;
import story.GameEvent;

/**
 * Game content panel สำหรับ Multiplayer mode
 */
public class MultiplayerGamePanel extends JPanel {
    
    private static final String[] FONT_CANDIDATES = {
        "TH Sarabun New",
        "Leelawadee UI",
        "Tahoma",
        "Noto Sans Thai",
        "Segoe UI"
    };

    private JLabel dialogText;
    private EventManager eventManager;
    private Player player;
    
    private int currentDay = 0;
    private GameEvent activeEvent = null;
    private int eventStep = 0;
    private int introIndex = 0;
    private Image backgroundImage;
    private Font gameFont = createGameFont(Font.PLAIN, 26);
    private Font buttonFont = createGameFont(Font.BOLD, 20);

    private JPanel choicePanel;
    private JPanel mainScene;
    private JButton nextDayButton;
    
    // Shop-related fields
    private JLayeredPane layeredPane;
    private JPanel shopOverlay;
    private Item selectedItem = null;
    private JPanel selectedCard = null;
    private JButton buyButton;
    
    // Game completion callback
    private GameCompletionListener gameCompletionListener;
    private int totalPlayers = 1;
    
    // Waiting room UI
    private JPanel waitingRoomPanel;

    // Interface for game completion
    public interface GameCompletionListener {
        void onGameCompleted(int affectionScore, int totalPlayers);
    }

    // Item class for shop
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

    private static Font createGameFont(int style, int size) {
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String preferred : FONT_CANDIDATES) {
            for (String available : availableFonts) {
                if (preferred.equalsIgnoreCase(available)) {
                    return new Font(available, style, size);
                }
            }
        }
        return new Font(Font.SANS_SERIF, style, size);
    }

    public MultiplayerGamePanel() {
        setLayout(new BorderLayout());
        
        // ตั้งค่า UI fonts
        UIManager.put("OptionPane.messageFont", createGameFont(Font.PLAIN, 20));
        UIManager.put("OptionPane.buttonFont", createGameFont(Font.BOLD, 20));
        UIManager.put("Label.font", createGameFont(Font.PLAIN, 20));
        UIManager.put("Button.font", createGameFont(Font.BOLD, 20));
        UIManager.put("ComboBox.font", createGameFont(Font.PLAIN, 19));
        UIManager.put("List.font", createGameFont(Font.PLAIN, 19));
        
        eventManager = new EventManager();
        player = new Player();

        backgroundImage = new ImageIcon("../images_Story/ปก.png").getImage();

        // Create LayeredPane for shop overlay
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        mainScene = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                if (backgroundImage != null) {
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int imgWidth = backgroundImage.getWidth(null);
                    int imgHeight = backgroundImage.getHeight(null);

                    if (imgWidth > 0 && imgHeight > 0) {
                        double panelAspect = (double) panelWidth / panelHeight;
                        double imgAspect = (double) imgWidth / imgHeight;

                        int drawWidth, drawHeight;
                        int x = 0, y = 0;

                        if (imgAspect > panelAspect) {
                            drawWidth = panelWidth;
                            drawHeight = (int) (panelWidth / imgAspect);
                            y = (panelHeight - drawHeight) / 2; 
                        } else {
                            drawHeight = panelHeight;
                            drawWidth = (int) (panelHeight * imgAspect);
                            x = (panelWidth - drawWidth) / 2; 
                        }
                        g2d.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
                    }
                }
            }
        };
        mainScene.setLayout(new BorderLayout());

        // เพิ่มปุ่ม menu มุมซ้ายบน
        JButton menuButton = createMenuButton();
        menuButton.addActionListener(e -> showMultiplayerGameMenu());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // เพิ่ม menu button ทางซ้าย
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(menuButton);
        topPanel.add(leftPanel, BorderLayout.WEST);
        
        mainScene.add(topPanel, BorderLayout.NORTH);

        choicePanel = new JPanel(new GridBagLayout()); 
        choicePanel.setOpaque(false);
        mainScene.add(choicePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(0, 0, 0, 200)); 
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(Color.WHITE);
                g2d.drawRect(5, 5, getWidth() - 10, getHeight() - 10);
            }
        };
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(getWidth(), 150));
        bottomPanel.setOpaque(false);

        dialogText = new JLabel("ยินดีต้อนรับสู่เกม 7 Days! เป้าหมายคือพิชิตใจเลม่อนให้ได้ภายใน 7 วัน");
        dialogText.setForeground(Color.WHITE);
        dialogText.setFont(gameFont); 
        dialogText.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        bottomPanel.add(dialogText, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setOpaque(false);
        
        nextDayButton = new JButton("เริ่มเกม");
        nextDayButton.setFont(buttonFont); 
        nextDayButton.addActionListener(e -> {
            // Check if game is complete (day 7, eventStep == 0 after showing results)
            if (currentDay == 7 && eventStep == 0 && nextDayButton.getText().equals("จบเกม")) {
                // Game is completed, notify listener
                if (gameCompletionListener != null) {
                    gameCompletionListener.onGameCompleted(player.getAffectionScore(), totalPlayers);
                } else {
                    // Fallback: show waiting room directly
                    showWaitingRoom();
                }
            } else {
                advanceDay();
            }
        });
        controlPanel.add(nextDayButton);
        bottomPanel.add(controlPanel, BorderLayout.EAST);

        mainScene.add(bottomPanel, BorderLayout.SOUTH);

        // Add mainScene to layeredPane
        layeredPane.add(mainScene, Integer.valueOf(0));
        
        // Add component listener to handle resizing
        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                mainScene.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                if (shopOverlay != null) {
                    shopOverlay.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                }
            }
        });
        
        // Add layeredPane to main panel
        add(layeredPane, BorderLayout.CENTER);
        
        // Initialize game
        initGame();
    }

    private void initGame() {
        currentDay = 0;
        eventStep = 0;
        introIndex = 0;
        activeEvent = null;
        backgroundImage = new ImageIcon("../images_Story/ปก.png").getImage();
        dialogText.setText("ยินดีต้อนรับสู่เกม 7 Days! เป้าหมายคือพิชิตใจเลม่อนให้ได้ภายใน 7 วัน");
        nextDayButton.setText("เริ่มเกม");
        nextDayButton.setEnabled(true);
        choicePanel.removeAll();
        choicePanel.revalidate();
        mainScene.repaint();
    }

    public void setGameCompletionListener(GameCompletionListener listener) {
        this.gameCompletionListener = listener;
    }

    public void setTotalPlayers(int total) {
        this.totalPlayers = total;
    }

    private JButton createMenuButton() {
        JButton menuButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);

                int centerX = getWidth() / 2;
                int startY = (getHeight() / 2) - 10;
                int dotSize = 6;
                int spacing = 8;

                for (int i = 0; i < 3; i++) {
                    int y = startY + (i * spacing);
                    g2d.fillOval(centerX - (dotSize / 2), y, dotSize, dotSize);
                }
                g2d.dispose();
            }
        };
        menuButton.setText("");
        menuButton.setBackground(new Color(0, 0, 0, 150));
        menuButton.setFocusPainted(false);
        menuButton.setBorderPainted(false);
        menuButton.setPreferredSize(new Dimension(50, 50));
        menuButton.setMinimumSize(new Dimension(50, 50));
        menuButton.setMaximumSize(new Dimension(50, 50));
        menuButton.setMargin(new Insets(0, 0, 0, 0));
        
        return menuButton;
    }

    private void showMultiplayerGameMenu() {
        // สร้าง Dialog สำหรับ menu เหมือนกับ Single Player
        JDialog menuDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "เมนู", true);
        menuDialog.setSize(500, 300);
        menuDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        menuDialog.setLayout(null);
        menuDialog.getContentPane().setBackground(new Color(50, 50, 80));

        // Title
        JLabel titleLabel = new JLabel("เมนู");
        titleLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(50, 20, 400, 60);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        menuDialog.add(titleLabel);

        // Continue button (Green)
        JButton continueBtn = new JButton("ต่อเกม");
        continueBtn.setFont(new Font("TH Sarabun New", Font.BOLD, 22));
        continueBtn.setBackground(new Color(100, 200, 100));
        continueBtn.setForeground(Color.WHITE);
        continueBtn.setFocusPainted(false);
        continueBtn.setBounds(75, 100, 350, 55);
        continueBtn.addActionListener(e -> menuDialog.dispose());
        menuDialog.add(continueBtn);

        // Exit button (Pink)
        JButton exitBtn = new JButton("ออกเกมส์");
        exitBtn.setFont(new Font("TH Sarabun New", Font.BOLD, 22));
        exitBtn.setBackground(new Color(255, 120, 160));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFocusPainted(false);
        exitBtn.setBounds(75, 170, 350, 55);
        exitBtn.addActionListener(e -> {
            // 🌟 เพิ่ม Confirm Dialog ตรงนี้ให้เหมือนรูปที่ 3 🌟
            int confirm = JOptionPane.showConfirmDialog(menuDialog, 
                    "คุณต้องการออกจากเกมนี้ใช่หรือไม่?\n(หากเล่นโหมดออนไลน์ คะแนนจะถูกส่งทันที)", 
                    "ยืนยันการออก", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                menuDialog.dispose();
                
                // แจ้งเตือน Listener เพื่อส่งคะแนน (ส่งค่าเป็น -1 เพื่อบอกว่ายอมแพ้ หรือแล้วแต่การออกแบบของคุณ)
                if (gameCompletionListener != null) {
                    gameCompletionListener.onGameCompleted(player.getAffectionScore(), totalPlayers);
                }
                
                // ปิดหน้าต่าง UI 
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.dispose();
                }
            }
        });
        menuDialog.add(exitBtn);

        menuDialog.setVisible(true);
    }

    private void advanceDay() {
        if (currentDay >= 7 && activeEvent == null) {
            return; 
        }

        if (activeEvent == null) {
            currentDay++;
            activeEvent = eventManager.checkDailyEvent(currentDay, player);
            
            if (activeEvent != null) {
                eventStep = 1;
                introIndex = 0;
                dialogText.setText("<html>วันที่ " + currentDay + " : <font color='yellow'>[ EVENT ]</font><br>" + activeEvent.getIntroTexts().get(introIndex) + "</html>");
                changeBackground(activeEvent.getIntroBgPaths().get(introIndex));
                nextDayButton.setText("ไปต่อ"); 
            }
        } 
        else if (eventStep == 1) {
            introIndex++;
            
            if (introIndex < activeEvent.getIntroTexts().size()) {
                dialogText.setText("<html>" + activeEvent.getIntroTexts().get(introIndex) + "</html>");
                changeBackground(activeEvent.getIntroBgPaths().get(introIndex));
            } else {
                if (activeEvent.getChoices().isEmpty()) {
                    if (currentDay == 7) {
                        eventStep = 3;
                        dialogText.setText("<html>" + activeEvent.getQuestionText() + "</html>");
                        changeBackground(activeEvent.getQuestionBgPath());
                        nextDayButton.setText("ดูผลลัพธ์");
                        nextDayButton.setEnabled(true);
                    } else {
                        activeEvent = null;
                        eventStep = 0;
                        advanceDay(); 
                    }
                } else {
                    eventStep = 2;
                    dialogText.setText("<html>" + activeEvent.getQuestionText() + "</html>");
                    changeBackground(activeEvent.getQuestionBgPath());
                    
                    showChoices(activeEvent.getChoices()); 
                    nextDayButton.setEnabled(false); 
                }
            }
        } 
        else if (eventStep == 3) {
            activeEvent = null;
            eventStep = 0;
            choicePanel.removeAll(); 
            choicePanel.revalidate();
            choicePanel.repaint();
            
            if (currentDay == 7) {
                dialogText.setText("<html>คะแนนความสัมพันธ์ของคุณคือ: " + player.getAffectionScore() + "</html>");
                nextDayButton.setText("จบเกม");
                nextDayButton.setEnabled(true);
            } else {
                advanceDay(); 
            }
        }
    }

    private void changeBackground(String path) {
        if (path != null && !path.isEmpty()) {
            backgroundImage = new ImageIcon(path).getImage();
            mainScene.repaint();
        }
    }

    private void showChoices(List<Choice> choices) {
        choicePanel.removeAll();
        JPanel btnContainer = new JPanel(new GridLayout(0, 1, 10, 10)); 
        btnContainer.setOpaque(false);

        for (Choice c : choices) {
            JButton choiceBtn = new JButton(c.getText());
            choiceBtn.setFont(buttonFont);
            choiceBtn.setBackground(new Color(255, 240, 245));
            
            choiceBtn.addActionListener(e -> {
                if (c.isOpenShop()) {
                    String eventId = (activeEvent != null) ? activeEvent.getEventId() : null;
                    showShop(eventId); 
                }

                player.addAffection(c.getAffectionChange());
                for(int i=0; i<c.getTeaseChange(); i++) player.addTease();

                dialogText.setText("<html>" + c.getResponseText() + "</html>");
                changeBackground(c.getOutcomeBgPath());

                choicePanel.removeAll(); 
                choicePanel.revalidate();
                choicePanel.repaint();
                
                nextDayButton.setEnabled(true); 
                nextDayButton.setText(currentDay == 7 ? "ดูผลลัพธ์ " : "ข้ามวัน ");
                eventStep = 3;
            });
            btnContainer.add(choiceBtn);
        }
        choicePanel.add(btnContainer);
        choicePanel.revalidate();
        mainScene.repaint();
    }

    private void showShop(String eventId) {
        // สร้าง shop overlay บน layered pane
        shopOverlay = new JPanel(null);
        shopOverlay.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
        shopOverlay.setBackground(new Color(0, 0, 0, 150));

        RoundedPanel popup = new RoundedPanel(40);
        popup.setLayout(null);
        popup.setBackground(new Color(255, 120, 160));
        
        int popupWidth = 700;
        int popupHeight = 600;
        int panelWidth = shopOverlay.getWidth();
        int panelHeight = shopOverlay.getHeight();
        
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

        JButton closeButton = createCloseButton();
        closeButton.setBounds(640, 15, 40, 40);
        closeButton.setEnabled(false);
        closeButton.addActionListener(e -> closeShop());
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
        buyButton.addActionListener(e -> buyItem(eventId, closeButton));
        popup.add(buyButton);

        shopOverlay.add(popup);
        layeredPane.add(shopOverlay, Integer.valueOf(10));
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private void closeShop() {
        if (shopOverlay != null) {
            layeredPane.remove(shopOverlay);
            shopOverlay = null;
            selectedItem = null;
            selectedCard = null;
            layeredPane.revalidate();
            layeredPane.repaint();
        }
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

    private void buyItem(String eventId, JButton closeButton) {
        if (selectedItem == null) return;
        
        if (selectedItem.quantity <= 0) {
            JOptionPane.showMessageDialog(this, "สินค้าชิ้นนี้หมดแล้ว!");
            return;
        }
        
        // ตรวจสอบว่าต้องซื้อของที่ถูกต้องหรือไม่สำหรับอีเวนต์นี้
        String requiredItem = getRequiredItemForEvent(eventId);
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

    public Player getPlayer() {
        return player;
    }

    public void showWaitingRoom() {
        // Hide game content and show waiting room
        choicePanel.removeAll();
        choicePanel.revalidate();
        
        // Create waiting room overlay
        waitingRoomPanel = new JPanel(null);
        waitingRoomPanel.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
        waitingRoomPanel.setBackground(new Color(0, 0, 0, 150));

        RoundedPanel popup = new RoundedPanel(40);
        popup.setLayout(null);
        popup.setBackground(new Color(255, 120, 160));
        
        int popupWidth = 600;
        int popupHeight = 400;
        int panelWidth = waitingRoomPanel.getWidth();
        int panelHeight = waitingRoomPanel.getHeight();
        
        if (panelWidth <= 0) panelWidth = 1280;
        if (panelHeight <= 0) panelHeight = 720;
        
        int x = (panelWidth - popupWidth) / 2;
        int y = (panelHeight - popupHeight) / 2;
        
        popup.setBounds(x, y, popupWidth, popupHeight);

        // Title
        JLabel titleLabel = new JLabel("เกมจบแล้ว!");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 48));
        titleLabel.setBounds(50, 30, 500, 60);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        popup.add(titleLabel);

        // Score display
        JLabel scoreLabel = new JLabel("คะแนนของคุณ: " + player.getAffectionScore());
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 32));
        scoreLabel.setBounds(50, 110, 500, 50);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        popup.add(scoreLabel);

        // Waiting message
        JLabel waitingLabel = new JLabel();
        waitingLabel.setForeground(Color.WHITE);
        waitingLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 24));
        waitingLabel.setBounds(50, 180, 500, 150);
        waitingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        waitingLabel.setVerticalAlignment(SwingConstants.TOP);
        
        if (totalPlayers == 1) {
            waitingLabel.setText("กำลังโหลดผลลัพธ์...");
        } else {
            waitingLabel.setText("<html>รอให้ผู้เล่นคนอื่นจบเกม<br>(" + totalPlayers + " คน)</html>");
        }
        popup.add(waitingLabel);

        waitingRoomPanel.add(popup);
        layeredPane.add(waitingRoomPanel, Integer.valueOf(10));
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    public void hideWaitingRoom() {
        if (waitingRoomPanel != null) {
            layeredPane.remove(waitingRoomPanel);
            waitingRoomPanel = null;
            layeredPane.revalidate();
            layeredPane.repaint();
        }
    }

    public void resetGame() {
        hideWaitingRoom();
        closeShop();
        initGame();
    }
}
