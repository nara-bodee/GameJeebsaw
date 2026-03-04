package main;

import core.GameSettings;
import core.Player;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.*;
import save.GameSaveData;
import save.SaveManager;
import shop.ShopWindow;
import story.Choice;
import story.EventManager;
import story.GameEvent;
import ui.UI;

public class GameWindow extends JFrame {

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
    
    // ตั้งค่าเริ่มต้นเป็น 0 เพื่อที่กดเริ่มเกมครั้งแรกจะกลายเป็นวันที่ 1
    private int currentDay = 0; 

    private Image backgroundImage;
    private Font gameFont = createGameFont(Font.PLAIN, 26);
    private Font buttonFont = createGameFont(Font.BOLD, 20);

    private JPanel choicePanel; 
    private JButton nextDayButton; 
    private GameEvent activeEvent = null;
    
    // สำหรับระบบ Multiplayer Reconnect
    private JButton reconnectButton; 
    private int eventStep = 0;
    private final IntConsumer onFinalScore;
    private final String playerDisplayName;
    private boolean finalScoreSent = false;
    private final Runnable onReconnectAttempt; 
    
    // ตัวแปรสำหรับ save game
    private static final int MAX_SAVE_SLOTS = 5;
    private static final int AUTO_SAVE_SLOT = 1;

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

    private static void applyUiFonts() {
        UIManager.put("OptionPane.messageFont", createGameFont(Font.PLAIN, 20));
        UIManager.put("OptionPane.buttonFont", createGameFont(Font.BOLD, 20));
        UIManager.put("Label.font", createGameFont(Font.PLAIN, 20));
        UIManager.put("Button.font", createGameFont(Font.BOLD, 20));
        UIManager.put("ComboBox.font", createGameFont(Font.PLAIN, 19));
        UIManager.put("List.font", createGameFont(Font.PLAIN, 19));
    }

    public GameWindow() {
        this("Player", null, null);
    }

    public GameWindow(String playerDisplayName, IntConsumer onFinalScore, Runnable onReconnectAttempt) {
        this.playerDisplayName = (playerDisplayName == null || playerDisplayName.trim().isEmpty()) ? "Player" : playerDisplayName.trim();
        this.onFinalScore = onFinalScore;
        this.onReconnectAttempt = onReconnectAttempt;

        // ตั้งค่า font ให้ JOptionPane และ dialog ทั้งหมด
        applyUiFonts();
        
        setTitle("เกมจีบสาว 7 Days - " + this.playerDisplayName);
        setLayout(new BorderLayout());

        // ดักจับการกดกากบาท (X) เพื่อให้ส่งคะแนน/ยอมแพ้ แทนที่จะปิดโปรแกรมทิ้งไปเลย
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 🌟 แก้ไขข้อความในส่วนนี้ให้ตรงกับรูปที่ 2 🌟
                int confirm = JOptionPane.showConfirmDialog(GameWindow.this, 
                        "คุณต้องการยอมแพ้และออกจากเกมนี้ใช่หรือไม่?\n(คะแนนของคุณจะถูกส่งทันที)", 
                        "ยืนยันการออก", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (GameWindow.this.onFinalScore != null) {
                        reportFinalScoreIfNeeded(); 
                    } else {
                        System.exit(0); 
                    }
                }
            }
        });

        eventManager = new EventManager();
        player = new Player(); 

        // รูปหน้าปกเกม
        backgroundImage = new ImageIcon("../images_Story/ปก.png").getImage();

        JPanel mainScene = new JPanel() {
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
        menuButton.addActionListener(e -> showMenuDialog());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setOpaque(false);
        topPanel.add(menuButton);
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

        // ข้อความต้อนรับเข้าเกม
        dialogText = new JLabel("ยินดีต้อนรับสู่เกม 7 Days! เป้าหมายคือพิชิตใจเลม่อนให้ได้ภายใน 7 วัน");
        dialogText.setForeground(Color.WHITE);
        dialogText.setFont(gameFont); 
        dialogText.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        bottomPanel.add(dialogText, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setOpaque(false);
        nextDayButton = new JButton("เริ่มเกม");
        nextDayButton.setFont(buttonFont); 
        nextDayButton.addActionListener(e -> advanceDay());

        // Reconnect button for multiplayer, initially hidden
        reconnectButton = new JButton("Reconnect");
        reconnectButton.setFont(buttonFont);
        reconnectButton.setVisible(false);
        reconnectButton.addActionListener(e -> {
            if (this.onReconnectAttempt != null) {
                reconnectButton.setText("Reconnecting...");
                reconnectButton.setEnabled(false);
                this.onReconnectAttempt.run();
            }
        });

        controlPanel.add(reconnectButton);
        controlPanel.add(nextDayButton); // Add original button
        bottomPanel.add(controlPanel, BorderLayout.EAST);

        mainScene.add(bottomPanel, BorderLayout.SOUTH);

        add(mainScene);
        setSize(800, 600); // กำหนดขนาดเริ่มต้น
        setLocationRelativeTo(null);
    }

    /**
     * Toggles the UI to show a "Reconnect" button when connection is lost.
     * This is intended for multiplayer mode.
     * @param isDisconnected true to show reconnect UI, false to show normal game UI.
     */
    public void setConnectionState(boolean isDisconnected) {
        if (isDisconnected) {
            dialogText.setText("<html><font color='red'>Connection Lost!</font><br>Please reconnect to continue the game.</html>");
            reconnectButton.setVisible(true);
            reconnectButton.setText("Reconnect");
            reconnectButton.setEnabled(true);
            nextDayButton.setVisible(false); // Hide normal game progression
            choicePanel.setVisible(false); // Hide choices
        } else {
            // On successful reconnect, the server will send a new state, which will update the UI.
            reconnectButton.setVisible(false);
            nextDayButton.setVisible(true);
            choicePanel.setVisible(true);
        }
        repaint();
    }

    // แสดง Menu Dialog
    private void showMenuDialog() {
        JDialog menuDialog = new JDialog(this, "Menu", true);
        menuDialog.setLayout(new GridLayout(5, 1, 10, 10));
        menuDialog.setSize(300, 380);
        menuDialog.setLocationRelativeTo(this);

        Font menuFont = createGameFont(Font.BOLD, 22);

        // ปุ่ม Continue
        JButton continueBtn = new JButton("Continue");
        continueBtn.setFont(menuFont);
        continueBtn.setBackground(new Color(100, 200, 100));
        continueBtn.setForeground(Color.WHITE);
        continueBtn.setFocusPainted(false);
        continueBtn.addActionListener(e -> menuDialog.dispose());

        // ปุ่ม New Save (บันทึกเกมปัจจุบัน)
        JButton newGameBtn = new JButton("New Save");
        newGameBtn.setFont(menuFont);
        newGameBtn.setBackground(new Color(255, 200, 100));
        newGameBtn.setForeground(Color.WHITE);
        newGameBtn.setFocusPainted(false);
        newGameBtn.addActionListener(e -> {
            Integer slot = chooseSaveSlot(false);
            if (slot != null) {
                saveGame(slot);
                menuDialog.dispose();
            }
        });

        // ปุ่ม Load Save (โหลด save ล่าสุด)
        JButton loadSaveBtn = new JButton("Load Save");
        loadSaveBtn.setFont(menuFont);
        loadSaveBtn.setBackground(new Color(150, 180, 200));
        loadSaveBtn.setForeground(Color.WHITE);
        loadSaveBtn.setFocusPainted(false);
        loadSaveBtn.addActionListener(e -> {
            Integer slot = chooseSaveSlot(true);
            if (slot != null) {
                loadGame(slot, true);
                menuDialog.dispose();
            }
        });

        // ปุ่ม Settings
        JButton settingsBtn = new JButton("Settings");
        settingsBtn.setFont(menuFont);
        settingsBtn.setBackground(new Color(100, 150, 255));
        settingsBtn.setForeground(Color.WHITE);
        settingsBtn.setFocusPainted(false);
        settingsBtn.addActionListener(e -> {
            menuDialog.dispose();
            showSettingsDialog();
        });

        // 🔥 ปุ่ม Exit
        JButton exitBtn = new JButton("Exit");
        exitBtn.setFont(menuFont);
        exitBtn.setBackground(new Color(255, 100, 100));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFocusPainted(false);
        exitBtn.addActionListener(e -> {
            // 🌟 แก้ไขข้อความในส่วนนี้ให้ตรงกับรูปที่ 3 🌟
            int confirm = JOptionPane.showConfirmDialog(menuDialog, 
                    "คุณต้องการออกจากเกมนี้ใช่หรือไม่?\n(หากเล่นโหมดออนไลน์ คะแนนจะถูกส่งทันที)", 
                    "ยืนยันการออก", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                menuDialog.dispose(); 
                
                if (onFinalScore != null) {
                    reportFinalScoreIfNeeded(); 
                } else {
                    dispose();
                    SwingUtilities.invokeLater(() -> new UI(() -> new GameWindow().setVisible(true)));
                }
            }
        });

        menuDialog.add(continueBtn);
        menuDialog.add(newGameBtn);
        menuDialog.add(loadSaveBtn);
        menuDialog.add(settingsBtn);
        menuDialog.add(exitBtn);

        menuDialog.setVisible(true);
    }

    // แสดง Settings Dialog
    private void showSettingsDialog() {
        JDialog settingsDialog = new JDialog(this, "Settings", true);
        settingsDialog.setLayout(new BorderLayout(10, 10));
        settingsDialog.setSize(400, 300);
        settingsDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("เลือกความละเอียดหน้าจอ:");
        titleLabel.setFont(createGameFont(Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        String[] resolutions = {"800x600", "1024x768", "1280x720", "1366x768", "1920x1080"};
        JComboBox<String> resolutionBox = new JComboBox<>(resolutions);
        resolutionBox.setFont(createGameFont(Font.PLAIN, 18));
        resolutionBox.setMaximumSize(new Dimension(200, 30));
        resolutionBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // ตั้งค่าเริ่มต้นตามความละเอียดปัจจุบัน
        GameSettings settings = GameSettings.getInstance();
        String currentRes = settings.getScreenWidth() + "x" + settings.getScreenHeight();
        resolutionBox.setSelectedItem(currentRes);

        panel.add(resolutionBox);
        panel.add(Box.createVerticalStrut(30));

        JButton applyBtn = new JButton("Apply");
        applyBtn.setFont(createGameFont(Font.BOLD, 18));
        applyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyBtn.addActionListener(e -> {
            String selected = (String) resolutionBox.getSelectedItem();
            if (selected != null) {
                String[] parts = selected.split("x");
                int width = Integer.parseInt(parts[0]);
                int height = Integer.parseInt(parts[1]);
                
                settings.applyResolution(width, height, false);
                setSize(width, height);
                setLocationRelativeTo(null);
                revalidate();
                repaint();
                
                JOptionPane.showMessageDialog(settingsDialog, 
                    selected ,
                    "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            }
            settingsDialog.dispose();
        });

        panel.add(applyBtn);
        settingsDialog.add(panel, BorderLayout.CENTER);
        settingsDialog.setVisible(true);
    }

    private int introIndex = 0; // 🌟 เพิ่มตัวแปรนี้ไว้นับหน้าฉาก

    private void advanceDay() {
        if (currentDay >= 7 && activeEvent == null) {
            return; 
        }

        if (activeEvent == null) {
            currentDay++;
            activeEvent = eventManager.checkDailyEvent(currentDay, player);
            
            if (activeEvent != null) {
                eventStep = 1;
                introIndex = 0; // เริ่มที่ฉากที่ 1
                dialogText.setText("<html>วันที่ " + currentDay + " : <font color='yellow'>[ EVENT ]</font><br>" + activeEvent.getIntroTexts().get(introIndex) + "</html>");
                changeBackground(activeEvent.getIntroBgPaths().get(introIndex));
                nextDayButton.setText("ไปต่อ"); 
            }
        } 
        else if (eventStep == 1) {
            introIndex++; // เปลี่ยนเป็นฉากต่อไป
            
            // เช็คว่ายังมีฉาก Intro ให้เปลี่ยนอีกไหม?
            if (introIndex < activeEvent.getIntroTexts().size()) {
                // ถ้ามี โชว์ข้อความและรูปหน้าต่อไปเลย
                dialogText.setText("<html>" + activeEvent.getIntroTexts().get(introIndex) + "</html>");
                changeBackground(activeEvent.getIntroBgPaths().get(introIndex));
            } else {
                // ถ้าหมด Intro แล้ว ก็เข้าสู่หน้าคำถามและโชว์ปุ่มตัวเลือก
                if (activeEvent.getChoices().isEmpty()) {
                    // ถ้าเป็นวันที่ 7 (ending) ให้แสดงข้อความและใช้ปุ่มมุมขวาล่าง
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
                reportFinalScoreIfNeeded();
                dialogText.setText("<html>คะแนนความสัมพันธ์ของคุณคือ: " + player.getAffectionScore() + "</html>");
                nextDayButton.setText("กลับหน้าหลัก");
                nextDayButton.setEnabled(true);
                
                // เมื่อกดปุ่มจะกลับไปหน้าหลัก
                for (ActionListener al : nextDayButton.getActionListeners()) {
                    nextDayButton.removeActionListener(al);
                }
                nextDayButton.addActionListener(e -> {
                    dispose();
                    // ตรวจสอบก่อนว่าไม่ได้อยู่ในโหมดออนไลน์ (ถ้าออนไลน์จะมี Popup สรุปคะแนนจัดการแล้ว)
                    if(onFinalScore == null) {
                        SwingUtilities.invokeLater(() -> new UI(() -> new GameWindow().setVisible(true)));
                    }
                });
            } else {
                advanceDay(); 
            }
        }
    }

    private void changeBackground(String path) {
        if (path != null && !path.isEmpty()) {
            backgroundImage = new ImageIcon(path).getImage();
            this.repaint();
        }
    }

    private void reportFinalScoreIfNeeded() {
        if (finalScoreSent) {
            return;
        }
        finalScoreSent = true;
        if (onFinalScore != null) {
            onFinalScore.accept(player.getAffectionScore());
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
                
                // 🌟 1. เช็คว่าถ้าปุ่มนี้ตั้งค่าว่าต้องเปิดร้าน ให้เรียกหน้าร้านค้าขึ้นมาก่อน!
                if (c.isOpenShop()) {
                    openShopUI(); 
                }

                // 2. แจกคะแนนตามปกติ
                player.addAffection(c.getAffectionChange());
                for(int i=0; i<c.getTeaseChange(); i++) player.addTease();

                // 3. แสดงข้อความโต้ตอบ
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
        this.repaint();
        
    }
    // ==========================================
    // 🌟 ระบบเปิดหน้าร้านค้า
    // ==========================================
    private void openShopUI() {
        // เรียกใช้ไฟล์ ShopWindow.java ที่เพื่อนจะสร้าง
        // ใช้ this เพื่ออ้างอิงหน้าต่างหลัก และส่ง player ไปให้ร้านค้าจัดการกระเป๋า
        ShopWindow shop = new ShopWindow(this, player);
        
        // คำสั่งนี้จะทำให้เกมหยุดรอ จนกว่าหน้าต่าง ShopWindow จะถูกปิดลง
        shop.setVisible(true); 
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UI(() -> new GameWindow().setVisible(true)); // In a real scenario, this would launch the lobby first.
        });
    }

    // =============== SAVE GAME ===============
    private void saveGame(int slotNumber) {
        String chapterName = activeEvent != null
            ? activeEvent.getEventName()
            : "Day " + Math.max(1, currentDay);

        GameSaveData saveData = new GameSaveData(
            chapterName,
            currentDay,
            eventStep,
            introIndex,
            player,
            activeEvent
        );

        boolean success = SaveManager.saveSlot(saveData, slotNumber);
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "บันทึกเกมสำเร็จ (ช่อง " + slotNumber + ")",
                "Save", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "เกิดข้อผิดพลาดในการบันทึกเกม",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =============== AUTO SAVE GAME ===============
    private void autoSaveGame() {
        String chapterName = activeEvent != null
            ? activeEvent.getEventName()
            : "Day " + Math.max(1, currentDay);
        GameSaveData saveData = new GameSaveData(chapterName, currentDay, eventStep, introIndex, player, activeEvent);
        SaveManager.saveSlot(saveData, AUTO_SAVE_SLOT);
    }

    // =============== LOAD GAME ===============
    private void loadGame(int slotNumber, boolean showMessage) {
        GameSaveData saveData = SaveManager.loadSlot(slotNumber);
        if (saveData == null) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, 
                    "ไม่พบไฟล์บันทึกในช่อง " + slotNumber + "!",
                    "Load", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        currentDay = saveData.getCurrentDay();
        eventStep = saveData.getEventStep();
        introIndex = saveData.getIntroIndex();
        player = saveData.getPlayer();
        activeEvent = saveData.getActiveEvent();

        // อัปเดตหน้าจอหลังจากโหลด
        if (activeEvent != null) {
            if (eventStep == 1 && introIndex < activeEvent.getIntroTexts().size()) {
                dialogText.setText("<html>วันที่ " + currentDay + " : <font color='yellow'>[ EVENT ]</font><br>" + activeEvent.getIntroTexts().get(introIndex) + "</html>");
                changeBackground(activeEvent.getIntroBgPaths().get(introIndex));
            } else if (eventStep == 2) {
                dialogText.setText("<html>" + activeEvent.getQuestionText() + "</html>");
                changeBackground(activeEvent.getQuestionBgPath());
                showChoices(activeEvent.getChoices());
                nextDayButton.setEnabled(false);
            }
        } else {
            dialogText.setText("เกมโหลดสำเร็จ วันที่ " + currentDay);
        }

        nextDayButton.setText("ไปต่อ");
        nextDayButton.setEnabled(true);

        if (showMessage) {
            JOptionPane.showMessageDialog(this, 
                "โหลดเกมสำเร็จ (ช่อง " + slotNumber + ")",
                "Load", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private Integer chooseSaveSlot(boolean existingOnly) {
        GameSaveData[] allSlots = SaveManager.getAllSaveSlotsInfo(MAX_SAVE_SLOTS);
        List<String> labels = new ArrayList<>();
        List<Integer> slotNumbers = new ArrayList<>();

        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            GameSaveData data = allSlots[i - 1];
            if (existingOnly && data == null) {
                continue;
            }

            String label;
            if (data == null) {
                label = "ช่อง " + i + " - ว่าง";
            } else {
                label = "ช่อง " + i + " - " + data.getChapterName() + " (" + data.getSaveDate() + ")";
            }

            labels.add(label);
            slotNumbers.add(i);
        }

        if (labels.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "ยังไม่มีไฟล์บันทึกในทุกช่อง",
                "Load",
                JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        String title = existingOnly ? "เลือกช่องโหลดเกม" : "เลือกช่องบันทึกเกม";
        String message = existingOnly ? "เลือกช่องเซฟที่ต้องการโหลด:" : "เลือกช่องเซฟที่ต้องการบันทึก:";

        Object selected = JOptionPane.showInputDialog(
            this,
            message,
            title,
            JOptionPane.PLAIN_MESSAGE,
            null,
            labels.toArray(),
            labels.get(0)
        );

        if (selected == null) {
            return null;
        }

        int selectedIndex = labels.indexOf(selected.toString());
        if (selectedIndex < 0) {
            return null;
        }
        return slotNumbers.get(selectedIndex);
    }
    
    // =============== CHECK FOR SAVED GAME ===============
    private void checkForSavedGame() {
        if (hasAnySaveSlots()) {
            int choice = JOptionPane.showConfirmDialog(this,
                "พบเกมที่บันทึกไว้ ต้องการเล่นต่อหรือไม่?",
                "เกมที่บันทึกไว้",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                Integer slot = chooseSaveSlot(true);
                if (slot != null) {
                    loadGame(slot, false);
                }
            }
            // ถ้าเลือก NO จะเริ่มเกมใหม่ตามปกติ
        }
    }

    private boolean hasAnySaveSlots() {
        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            if (SaveManager.hasSlot(i)) {
                return true;
            }
        }
        return false;
    }
    
    // =============== START NEW GAME ===============
    private void startNewGame() {
        // ลบไฟล์ save เก่าทั้งหมด
        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            SaveManager.deleteSlot(i);
        }
        
        // รีเซ็ตค่าทั้งหมด
        currentDay = 0;
        eventStep = 0;
        introIndex = 0;
        player = new Player();
        activeEvent = null;
        finalScoreSent = false;
        
        // รีเซ็ต UI
        backgroundImage = new ImageIcon("../images_Story/ปก.png").getImage();
        dialogText.setText("ยินดีต้อนรับสู่เกม 7 Days! เป้าหมายคือพิชิตใจเลม่อนให้ได้ภายใน 7 วัน");
        nextDayButton.setText("เริ่มเกม");
        nextDayButton.setEnabled(true);
        choicePanel.removeAll();
        choicePanel.revalidate();
        repaint();
        
        JOptionPane.showMessageDialog(this,
            "เริ่มเกมใหม่แล้ว!",
            "New Game",
            JOptionPane.INFORMATION_MESSAGE);
    }
}