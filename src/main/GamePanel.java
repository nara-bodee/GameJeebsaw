package main;

import core.Player;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import save.GameSaveData;
import save.SaveManager;
import story.Choice;
import story.EventManager;
import story.GameEvent;

public class GamePanel extends JPanel {
    
    private MainFrame mainFrame;
    
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
    private Image backgroundImage;
    private Font gameFont = createGameFont(Font.PLAIN, 26);
    private Font buttonFont = createGameFont(Font.BOLD, 20);

    private JPanel choicePanel; 
    private JButton nextDayButton; 
    private GameEvent activeEvent = null;
    private int eventStep = 0;
    private int introIndex = 0;
    
    private static final int MAX_SAVE_SLOTS = 5;
    
    private JPanel mainScene;

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

    public GamePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
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

        // เพิ่มปุ่ม menu มุมซ้ายบน และ affection score มุมบนขวา
        JButton menuButton = createMenuButton();
        menuButton.addActionListener(e -> showMenuDialog());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // เพิ่ม menu button ทางซ้าย
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(menuButton);
        topPanel.add(leftPanel, BorderLayout.WEST);
        
        // เพิ่ม affection score ทางขวา
        JLabel affectionLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // วาด background สีเข้ม โปร่งใส
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // วาดหัวใจ
                g2.setColor(new Color(255, 100, 150));
                int heartX = 10;
                int heartY = 8;
                drawHeart(g2, heartX, heartY, 30);
                
                // แสดงข้อความ
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("TH Sarabun New", Font.BOLD, 20));
                int score = player.getAffectionScore();
                g2.drawString(score + "", heartX + 40, heartY + 25);
                
                super.paintComponent(g);
            }
            
            private void drawHeart(Graphics2D g, int x, int y, int size) {
                int[] xPoints = {
                    x + size/2, x + size, x + size, x + size/2,
                    x, x, x + size/2
                };
                int[] yPoints = {
                    y + size, y + size/3, y, y + size/3,
                    y, y + size/3, y + size
                };
                g.fillPolygon(xPoints, yPoints, xPoints.length);
            }
        };
        
        affectionLabel.setPreferredSize(new Dimension(120, 50));
        affectionLabel.setOpaque(false);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(affectionLabel);
        topPanel.add(rightPanel, BorderLayout.EAST);
        
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
        nextDayButton.addActionListener(e -> advanceDay());
        controlPanel.add(nextDayButton);
        bottomPanel.add(controlPanel, BorderLayout.EAST);

        mainScene.add(bottomPanel, BorderLayout.SOUTH);

        add(mainScene);
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
    
    public void initializeGame() {
        // รีเซ็ตเกมให้เริ่มต้นใหม่
        resetGame();
        checkForSavedGame();
    }
    
    private void resetGame() {
        currentDay = 0;
        eventStep = 0;
        introIndex = 0;
        player = new Player();
        activeEvent = null;
        backgroundImage = new ImageIcon("../images_Story/ปก.png").getImage();
        dialogText.setText("ยินดีต้อนรับสู่เกม 7 Days! เป้าหมายคือพิชิตใจเลม่อนให้ได้ภายใน 7 วัน");
        nextDayButton.setText("เริ่มเกม");
        nextDayButton.setEnabled(true);
        choicePanel.removeAll();
        choicePanel.revalidate();
        mainScene.repaint();
    }
    
    public void startNewGame() {
        resetGame();
    }
    
    public void loadExistingSave() {
        Integer slot = chooseSaveSlot(true);
        if (slot != null) {
            loadGame(slot, false);
        }
    }
    
    public void loadExistingSaveFromSlot(int slotNumber) {
        loadGame(slotNumber, false);
    }
    
    public Player getPlayer() {
        return player;
    }
    
    private void showMenuDialog() {
        JDialog menuDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Menu", true);
        menuDialog.setLayout(new GridLayout(4, 1, 10, 10));
        menuDialog.setSize(300, 320);
        menuDialog.setLocationRelativeTo(this);

        Font menuFont = createGameFont(Font.BOLD, 22);

        // ปุ่ม Continue
        JButton continueBtn = new JButton("Continue");
        continueBtn.setFont(menuFont);
        continueBtn.setBackground(new Color(100, 200, 100));
        continueBtn.setForeground(Color.WHITE);
        continueBtn.setFocusPainted(false);
        continueBtn.addActionListener(e -> menuDialog.dispose());

        // ปุ่ม New Save
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

        // ปุ่ม Load Save
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

        // ปุ่ม Exit
        JButton exitBtn = new JButton("Exit to Menu");
        exitBtn.setFont(menuFont);
        exitBtn.setBackground(new Color(200, 100, 100));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFocusPainted(false);
        exitBtn.addActionListener(e -> {
            menuDialog.dispose();
            mainFrame.showMenu();
        });

        menuDialog.add(continueBtn);
        menuDialog.add(newGameBtn);
        menuDialog.add(loadSaveBtn);
        menuDialog.add(exitBtn);

        menuDialog.setVisible(true);
    }
    
    private void showSettingsDialog() {
        try {
            if (mainFrame != null) {
                mainFrame.showSettings();
            } else {
                JOptionPane.showMessageDialog(this, "Error: mainFrame is null", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
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
                nextDayButton.setText("กลับหน้าหลัก");
                nextDayButton.setEnabled(true);
                
                for (ActionListener al : nextDayButton.getActionListeners()) {
                    nextDayButton.removeActionListener(al);
                }
                nextDayButton.addActionListener(e -> mainFrame.showMenu());
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
                    mainFrame.showShop(eventId); 
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

    // Save/Load methods
    private void saveGame(int slotNumber) {
        String chapterName = activeEvent != null
            ? activeEvent.getEventName()
            : "วันที่ " + currentDay;
        
        GameSaveData saveData = new GameSaveData(
            chapterName, currentDay, eventStep, introIndex,
            player, activeEvent
        );

        if (SaveManager.saveSlot(saveData, slotNumber)) {
            JOptionPane.showMessageDialog(this, 
                "บันทึกเกมสำเร็จ (ช่อง " + slotNumber + ")",
                "Save", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "เกิดข้อผิดพลาดในการบันทึก",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadGame(int slotNumber, boolean showMessage) {
        GameSaveData saveData = SaveManager.loadSlot(slotNumber);
        
        if (saveData == null) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this,
                    "ไม่พบไฟล์เซฟในช่อง " + slotNumber,
                    "Load", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        currentDay = saveData.getCurrentDay();
        eventStep = saveData.getEventStep();
        introIndex = saveData.getIntroIndex();
        player = saveData.getPlayer();
        activeEvent = saveData.getActiveEvent();

        if (activeEvent != null) {
            if (eventStep == 1 && introIndex < activeEvent.getIntroTexts().size()) {
                dialogText.setText("<html>วันที่ " + currentDay + " : <font color='yellow'>[ EVENT ]</font><br>" + activeEvent.getIntroTexts().get(introIndex) + "</html>");
                changeBackground(activeEvent.getIntroBgPaths().get(introIndex));
                nextDayButton.setText("ไปต่อ");
                nextDayButton.setEnabled(true);
            } else if (eventStep == 2) {
                dialogText.setText("<html>" + activeEvent.getQuestionText() + "</html>");
                changeBackground(activeEvent.getQuestionBgPath());
                showChoices(activeEvent.getChoices());
                // ไม่ต้องตั้ง nextDayButton เพราะ showChoices() จะจัดการเอง
            }
        } else {
            dialogText.setText("เกมโหลดสำเร็จ วันที่ " + currentDay);
            nextDayButton.setText("ไปต่อ");
            nextDayButton.setEnabled(true);
        }

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
}
