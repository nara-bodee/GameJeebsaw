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
        // 🌟 ใส่โค้ดชุดใหม่นี้แทน: บังคับยืดทุกอย่างตามหน้าต่าง Game Panel หลักโดยตรง
        // 🌟 บังคับให้ขยายตามจอ 100% เสมอ
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = getWidth();
                int h = getHeight();
                
                layeredPane.setBounds(0, 0, w, h);
                mainScene.setBounds(0, 0, w, h);
                
                // ให้ Overlay อัปเดตขนาดตามหน้าจอ
                if (shopOverlay != null) shopOverlay.setBounds(0, 0, w, h);
                if (waitingRoomPanel != null) waitingRoomPanel.setBounds(0, 0, w, h);
                
                mainScene.revalidate();
                mainScene.repaint();
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

        // 🌟 1. สร้าง Player ใหม่ทุกครั้ง เพื่อให้คะแนน (Affection) กลับเป็น 0 🌟
        player = new Player(); 
        
        // 🌟 2. รีเซ็ตสต๊อกร้านค้าให้กลับมาเต็มเหมือนเดิม 🌟
        items = new Item[] {
            new Item("ช่อดอกไม้", 100, "../images/rose.png"),
            new Item("สมุดสรุปคณิตศาสตร์", 100, "../images/book.png"),
            new Item("เลมอนโซดา", 50, "../images/lemon_soda.png"),
            new Item("ร่มคันใหญ่", 100, "../images/umbrella.png"),
            new Item("พวงกุญแจตุ๊กตา", 50, "../images/baby.png"),
            new Item("เพชรมายา", 1000000, "../images/Daimon.png")
        };

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
    // ==========================================
    // 🌟 ระบบร้านค้าใหม่ (รองรับการย่อขยายจออัตโนมัติ)
    // ==========================================
    private void showShop(String eventId) {
        // ใช้ GridBagLayout เพื่อจัดให้อยู่กึ่งกลางเสมอ
        shopOverlay = new JPanel(new GridBagLayout()); 
        shopOverlay.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
        shopOverlay.setBackground(new Color(0, 0, 0, 150));

        RoundedPanel popup = new RoundedPanel(40);
        popup.setLayout(new BorderLayout(10, 20)); // ใช้ BorderLayout 
        popup.setBackground(new Color(255, 120, 160));
        popup.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ส่วนหัว (ข้อความ + ปุ่มปิด)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel welcomeLabel = new JLabel("ยินดีต้อนรับ! เลือกหยิบสินค้าได้เลย", SwingConstants.CENTER);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 36));
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        JButton closeButton = createCloseButton();
        closeButton.setPreferredSize(new Dimension(40, 40));
        closeButton.setEnabled(false);
        closeButton.addActionListener(e -> closeShop());
        topPanel.add(closeButton, BorderLayout.EAST);
        popup.add(topPanel, BorderLayout.NORTH);

        // ส่วนกลาง (ตารางไอเทม)
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        gridPanel.setOpaque(false);
        for (Item item : items) {
            gridPanel.add(createItemCard(item)); // ส่งแค่ item ไป
        }
        popup.add(gridPanel, BorderLayout.CENTER);

        // ส่วนท้าย (ปุ่ม Take)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        buyButton = createStyledBuyButton();
        buyButton.setPreferredSize(new Dimension(150, 50));
        buyButton.setEnabled(false);
        buyButton.addActionListener(e -> buyItem(eventId, closeButton));
        bottomPanel.add(buyButton);
        popup.add(bottomPanel, BorderLayout.SOUTH);

        shopOverlay.add(popup);
        layeredPane.add(shopOverlay, Integer.valueOf(10));
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private JPanel createItemCard(Item item) {
        JPanel card = new RoundedPanel(20);
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(255, 150, 180));
        card.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // ป้ายตัวเลขมุมขวา
        JPanel topArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topArea.setOpaque(false);
        CircleBadge badge = new CircleBadge(String.valueOf(item.quantity));
        badge.setPreferredSize(new Dimension(35, 35));
        item.badge = badge;
        topArea.add(badge);
        card.add(topArea, BorderLayout.NORTH);

        // รูปภาพไอเทม (รักษาสัดส่วน ไม่ให้ภาพบีบ/แบน)
        JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        try {
            ImageIcon icon = new ImageIcon(item.imagePath);
            Image original = icon.getImage();
            int ow = original.getWidth(null);
            int oh = original.getHeight(null);
            double aspect = (double) ow / oh;
            int targetW = 80, targetH = 80;
            if (ow > oh) targetH = (int) (80 / aspect);
            else targetW = (int) (80 * aspect);
            
            Image img = original.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            imageLabel.setText("No Image");
        }
        card.add(imageLabel, BorderLayout.CENTER);

        // ข้อความและป้าย FREE
        JPanel bottomArea = new JPanel(new GridLayout(2, 1, 0, 5));
        bottomArea.setOpaque(false);
        JLabel nameLabel = new JLabel(item.name, SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 22));
        bottomArea.add(nameLabel);

        JLabel priceLabel = new JLabel("FREE", SwingConstants.CENTER);
        priceLabel.setOpaque(true);
        priceLabel.setBackground(new Color(150, 255, 150));
        bottomArea.add(priceLabel);
        card.add(bottomArea, BorderLayout.SOUTH);

        // กดเพื่อเลือกไอเทม
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
                
                // จัดตัวอักษร X ให้อยู่กึ่งกลางปุ่มเสมอ
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("X")) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString("X", x, y);
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
        choicePanel.removeAll();
        choicePanel.revalidate();
        
        // ใช้ GridBagLayout เพื่อจัดกึ่งกลางอัตโนมัติ
        waitingRoomPanel = new JPanel(new GridBagLayout());
        waitingRoomPanel.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
        waitingRoomPanel.setBackground(new Color(0, 0, 0, 150));

        RoundedPanel popup = new RoundedPanel(40);
        popup.setLayout(new BoxLayout(popup, BoxLayout.Y_AXIS));
        popup.setBackground(new Color(255, 120, 160));
        popup.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        JLabel titleLabel = new JLabel("เกมจบแล้ว!", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 48));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        popup.add(titleLabel);
        
        popup.add(Box.createVerticalStrut(20));

        JLabel scoreLabel = new JLabel("คะแนนของคุณ: " + player.getAffectionScore(), SwingConstants.CENTER);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 32));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        popup.add(scoreLabel);
        
        popup.add(Box.createVerticalStrut(30));

        JLabel waitingLabel = new JLabel();
        waitingLabel.setForeground(Color.WHITE);
        waitingLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 24));
        waitingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (totalPlayers == 1) {
            waitingLabel.setText("กำลังโหลดผลลัพธ์...");
        } else {
            waitingLabel.setText("<html><center>รอให้ผู้เล่นคนอื่นจบเกม<br>(" + totalPlayers + " คน)</center></html>");
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
