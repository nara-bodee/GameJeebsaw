
package ui;

import core.GameSettings;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import main.GameWindow;
import online.LanDiscovery;
import online.OnlineClient;
import online.OnlineRoomInfo;
import online.ClientSessionManager;
import online.SessionInfo;
import online.OnlineServer;

public class UI extends JFrame {
    private static final String[] FONT_CANDIDATES = {
        "TH Sarabun New",
        "Leelawadee UI",
        "Tahoma",
        "Noto Sans Thai",
        "Segoe UI"
    };

    private final int BASE_WIDTH = 1280;
    private final int BASE_HEIGHT = 720;
    JLayeredPane layeredPane = new JLayeredPane();

    JPanel startLayer;
    JPanel gameLayer;

    ImageIcon bgImage = new ImageIcon("ui/bg.jpg");
    // ImageIcon girlImage = new ImageIcon("ui/girl.png"); // Commented out - add girl.png to src/ui/ folder if needed

    private JTextArea dialogueText;
    private final Runnable onStartGame;

    public UI() {
        this(null);
    }

    public static Font uiFont(int style, int size) {
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
        UIManager.put("OptionPane.messageFont", uiFont(Font.PLAIN, 20));
        UIManager.put("OptionPane.buttonFont", uiFont(Font.BOLD, 20));
        UIManager.put("Label.font", uiFont(Font.PLAIN, 20));
        UIManager.put("Button.font", uiFont(Font.BOLD, 20));
        UIManager.put("ComboBox.font", uiFont(Font.PLAIN, 19));
        UIManager.put("List.font", uiFont(Font.PLAIN, 19));
        UIManager.put("TextArea.font", uiFont(Font.PLAIN, 19));
    }

    public UI(Runnable onStartGame) {
        applyUiFonts();
        this.onStartGame = onStartGame;
        GameSettings settings = GameSettings.getInstance();
        int currentWidth = settings.getScreenWidth();
        int currentHeight = settings.getScreenHeight();

        setTitle("เกมจีบสาว");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // --- 1. ปิด Auto Layout และใส่พื้นหลังดำกันปุ่มตกขอบ ---
        getContentPane().setLayout(null);
        getContentPane().setBackground(Color.BLACK); 

        // --- 2. ตั้งขนาดหน้าต่าง ---
        getContentPane().setPreferredSize(new Dimension(currentWidth, currentHeight));
        pack();

        layeredPane.setLayout(null);
        // สร้าง UI แผ่นกระดาษหลักที่ขนาด 1280x720 เสมอ
        layeredPane.setBounds(0, 0, BASE_WIDTH, BASE_HEIGHT);

        startLayer = createStartScene();
        gameLayer = createGameScene();

        layeredPane.add(startLayer, Integer.valueOf(0));
        layeredPane.add(gameLayer, Integer.valueOf(1));
        gameLayer.setVisible(false);

        getContentPane().add(layeredPane);

        // จำพิกัดดั้งเดิม
        tagOriginalBounds(layeredPane);

        // --- 3. บังคับย่อขยายจาก "พื้นที่หน้าต่างที่กางได้จริง" เท่านั้น ---
        applyScale(getContentPane().getWidth(), getContentPane().getHeight());

        setLocationRelativeTo(null);
        setVisible(true);
    }
    // ================= RESIZE SCREEN (RESPONSIVE) =================
    // ================= RESIZE SCREEN (RESPONSIVE) =================
    private void changeScreenSize(int newWidth, int newHeight) {
        GameSettings.getInstance().applyResolution(newWidth, newHeight, false);

        // ขอกางหน้าต่างขนาดใหม่
        getContentPane().setPreferredSize(new Dimension(newWidth, newHeight));
        pack();
        setLocationRelativeTo(null); 

        // ดึงขนาดจริงที่ Windows อนุญาต (ป้องกันปัญหาเลือกจอใหญ่ 1920 แต่หน้าจอคอมจริงเล็กกว่า)
        int actualWidth = getContentPane().getWidth();
        int actualHeight = getContentPane().getHeight();

        applyScale(actualWidth, actualHeight);
    }

    // เมธอดสำหรับคำนวณและสั่งย่อขยาย
    // เมธอดสำหรับคำนวณและสั่งย่อขยาย
    private void applyScale(int actualWidth, int actualHeight) {
        // ใช้ Math.min รักษาสัดส่วน 16:9 ภาพจะไม่เบี้ยว ปุ่มจะไม่เป็นวงรี
        double scaleX = (double) actualWidth / BASE_WIDTH;
        double scaleY = (double) actualHeight / BASE_HEIGHT;
        double scale = Math.min(scaleX, scaleY);

        // คำนวณความกว้าง/สูงใหม่
        int scaledWidth = (int) Math.round(BASE_WIDTH * scale);
        int scaledHeight = (int) Math.round(BASE_HEIGHT * scale);

        // คำนวณจุดจัดกึ่งกลาง (ทำให้มีขอบดำซ้ายขวา/บนล่าง คล้ายดูหนัง ถ้าจอสัดส่วนแปลกๆ)
        int offsetX = (actualWidth - scaledWidth) / 2;
        int offsetY = (actualHeight - scaledHeight) / 2;

        // สั่งย่อปุ่มและฟอนต์ทุกชิ้น
        scaleFromBase(layeredPane, scale);

        // บังคับจัดตำแหน่ง Layer หลักให้อยู่ตรงกลางเป๊ะๆ ไม่ให้ตกขอบ
        layeredPane.setBounds(offsetX, offsetY, scaledWidth, scaledHeight);
        startLayer.setBounds(0, 0, scaledWidth, scaledHeight);
        gameLayer.setBounds(0, 0, scaledWidth, scaledHeight);

        revalidate();
        repaint();
    }
    // เมธอดช่วยจำตำแหน่งและขนาดดั้งเดิมของ UI (รันครั้งเดียวตอนเปิดเกม)
    // เมธอดช่วยจำตำแหน่ง ขนาด และ "ฟอนต์" ดั้งเดิมของ UI (รันครั้งเดียวตอนเปิดเกม)
    private void tagOriginalBounds(Container container) {
        if (container instanceof JComponent) {
            JComponent comp = (JComponent) container;
            // จำตำแหน่งและขนาดกล่อง
            comp.putClientProperty("baseBounds", comp.getBounds());
            
            // จำรูปแบบและขนาดฟอนต์
            if (comp.getFont() != null) {
                comp.putClientProperty("baseFont", comp.getFont());
            }
        }
        for (Component c : container.getComponents()) {
            if (c instanceof Container) {
                tagOriginalBounds((Container) c);
            }
        }
    }

    // เมธอดดึงตำแหน่งดั้งเดิมมาคูณด้วยอัตราส่วน (กดย่อขยายกี่รอบก็ไม่เพี้ยน)
    // เมธอดดึงตำแหน่งดั้งเดิมมาคูณอัตราส่วน และย่อขนาดฟอนต์
    // เมธอดดึงตำแหน่งดั้งเดิมมาคูณอัตราส่วน และย่อขนาดฟอนต์
    private void scaleFromBase(Container container, double scale) {
        if (container instanceof JComponent) {
            JComponent comp = (JComponent) container;
            
            Rectangle base = (Rectangle) comp.getClientProperty("baseBounds");
            if (base != null) {
                comp.setBounds(
                        (int) Math.round(base.x * scale),
                        (int) Math.round(base.y * scale),
                        (int) Math.round(base.width * scale),
                        (int) Math.round(base.height * scale)
                );
            }
            
            Font baseFont = (Font) comp.getClientProperty("baseFont");
            if (baseFont != null) {
                // เพิ่ม Math.max เพื่อป้องกัน Font ขนาดติดลบจนล่องหน
                float newSize = Math.max(1.0f, (float) (baseFont.getSize() * scale * 0.95f));
                comp.setFont(baseFont.deriveFont(newSize));
            }
        }
        for (Component c : container.getComponents()) {
            if (c instanceof Container) {
                scaleFromBase((Container) c, scale);
            }
        }
    }
    // ================= START =================
    JPanel createStartScene() {

    JPanel p = new JPanel(null){
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            // เปลี่ยนจาก 1280, 820 เป็น getWidth(), getHeight()
            g.drawImage(bgImage.getImage(), 0, 0, getWidth(), getHeight(), null);

            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(20,30,70,180));
            g2.fillRect(0,0,getWidth(),getHeight());
        }
    };

    p.setBounds(0,0,1280,720);

    JButton start = pinkButton("Start game", 200, 350);
    start.addActionListener(e -> {
        if (onStartGame != null) {
            dispose();
            onStartGame.run();
        } else {
            startLayer.setVisible(false);
            gameLayer.setVisible(true);
        }
    });

    JButton exit = pinkButton("Exit", 200, 430);
    exit.addActionListener(e -> System.exit(0));

    JButton online = pinkButton("Online LAN", 200, 510);
    online.addActionListener(e -> showOnlineMenu());

    p.add(start);
    p.add(exit);
    p.add(online);

    return p;
}

    private void showOnlineMenu() {
        SessionInfo lastSession = ClientSessionManager.loadSession();
        if (lastSession != null) {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "พบเกมที่ยังไม่จบในห้อง '" + lastSession.roomName() + "'\nคุณต้องการเชื่อมต่อใหม่หรือไม่?",
                "เชื่อมต่อใหม่",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                attemptReconnect(lastSession);
                return;
            } else {
                ClientSessionManager.clearSession(); // User chose not to reconnect, so clear the old session
            }
        }

        String playerName = JOptionPane.showInputDialog(
            this,
            "ใส่ชื่อผู้เล่น:",
            "Online LAN",
            JOptionPane.PLAIN_MESSAGE
        );

        if (playerName == null) {
            return;
        }
        playerName = playerName.trim();
        if (playerName.isEmpty()) {
            playerName = "Player";
        }

        Object[] options = {"สร้างห้อง (Host)", "เข้าห้อง (Join)", "ยกเลิก"};
        int selected = JOptionPane.showOptionDialog(
            this,
            "เลือกโหมดออนไลน์",
            "Online LAN",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );

        if (selected == 0) {
            startHostLobby(playerName);
        } else if (selected == 1) {
            startJoinLobby(playerName);
        }
    }

    private void startHostLobby(String playerName) {
        try {
            OnlineServer server = new OnlineServer("Room-" + playerName, playerName, 3);
            server.start();

            JDialog lobby = new JDialog(this, "Online Room (Host)", false);
            lobby.setSize(440, 360);
            lobby.setLocationRelativeTo(this);
            lobby.setLayout(new BorderLayout(10, 10));

            JLabel roomInfo = new JLabel("ห้อง: " + server.getRoomName() + " | พอร์ต: " + server.getPort());
            roomInfo.setFont(uiFont(Font.PLAIN, 20));
            roomInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

            JTextArea playersArea = new JTextArea("ผู้เล่นในห้อง:\n- " + playerName);
            playersArea.setFont(uiFont(Font.PLAIN, 20));
            playersArea.setEditable(false);
            playersArea.setLineWrap(true);
            playersArea.setWrapStyleWord(true);

            JButton startBtn = new JButton("เริ่มเกม");
            JButton closeBtn = new JButton("ปิดห้อง");
            startBtn.setFont(uiFont(Font.BOLD, 20));
            closeBtn.setFont(uiFont(Font.BOLD, 20));

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.add(startBtn);
            bottomPanel.add(closeBtn);

            lobby.add(roomInfo, BorderLayout.NORTH);
            lobby.add(new JScrollPane(playersArea), BorderLayout.CENTER);
            lobby.add(bottomPanel, BorderLayout.SOUTH);

            server.setListener(new OnlineServer.ServerListener() {
                @Override
                public void onPlayerListChanged(List<String> players) {
                    playersArea.setText(buildPlayerText(players));
                }

                @Override
                public void onScoreboardReady(String scoreboardText) {
                    JOptionPane.showMessageDialog(UI.this, scoreboardText, "ผลคะแนนออนไลน์", JOptionPane.INFORMATION_MESSAGE);
                    server.stop();
                }

                @Override
                public void onError(String error) {
                    JOptionPane.showMessageDialog(UI.this, error, "Online Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            startBtn.addActionListener(e -> {
                server.startGame();
                lobby.dispose();
                dispose();
                SwingUtilities.invokeLater(() -> new GameWindow(playerName, server::submitHostScore, null).setVisible(true));
            });

            closeBtn.addActionListener(e -> {
                server.stop();
                lobby.dispose();
            });

            lobby.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    server.stop();
                }
            });

            lobby.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "สร้างห้องไม่สำเร็จ: " + e.getMessage(), "Online", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attemptReconnect(SessionInfo session) {
        OnlineRoomInfo dummyRoom = new OnlineRoomInfo(session.roomName(), session.host(), session.port(), 0, 0, "Unknown");
        initiateOnlineSession(session.playerName(), dummyRoom, session.token());
    }

    private void startJoinLobby(String playerName) {
        OnlineRoomInfo selectedRoom = showRoomBrowserDialog();
        if (selectedRoom == null) {
            return;
        }
        initiateOnlineSession(playerName, selectedRoom, null);
    }

    private void initiateOnlineSession(String playerName, OnlineRoomInfo roomInfo, String reconnectToken) {
        OnlineClient client = new OnlineClient(roomInfo.getHostAddress(), roomInfo.getPort(), playerName);

        JDialog lobby = new JDialog(this, "Online Lobby (Join)", false);
        lobby.setSize(440, 360);
        lobby.setLocationRelativeTo(this);
        lobby.setLayout(new BorderLayout(10, 10));

        JLabel roomLabel = new JLabel("เชื่อมต่อ: " + roomInfo.getHostAddress() + ":" + roomInfo.getPort());
        roomLabel.setFont(uiFont(Font.PLAIN, 20));
        roomLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JTextArea playersArea = new JTextArea("กำลังเชื่อมต่อ...");
        playersArea.setFont(uiFont(Font.PLAIN, 20));
        playersArea.setEditable(false);
        playersArea.setLineWrap(true);
        playersArea.setWrapStyleWord(true);

        JButton leaveBtn = new JButton("ออกจากห้อง");
        leaveBtn.setFont(uiFont(Font.BOLD, 20));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(leaveBtn);

        lobby.add(roomLabel, BorderLayout.NORTH);
        lobby.add(new JScrollPane(playersArea), BorderLayout.CENTER);
        lobby.add(bottomPanel, BorderLayout.SOUTH);

        final String[] assignedName = {playerName};
        final boolean[] started = {false};

        client.setListener(new OnlineClient.ClientListener() {
            @Override
            public void onConnected(String actualName, String roomName, int maxPlayers, int currentPlayers) {
                assignedName[0] = actualName;
                roomLabel.setText("ห้อง: " + roomName); // Will be updated by player list

                // Save session for potential reconnect
                String token = client.getSessionToken();
                if (token != null) {
                    SessionInfo sessionInfo = new SessionInfo(
                        client.getHostAddress(),
                        client.getPort(),
                        roomName,
                        actualName,
                        token
                    );
                    ClientSessionManager.saveSession(sessionInfo);
                }
            }

            @Override
            public void onPlayerListChanged(List<String> players) {
                playersArea.setText(buildPlayerText(players));
                roomLabel.setText("ห้อง: " + roomInfo.getRoomName() + " | ผู้เล่น " + players.size() + "/" + roomInfo.getMaxPlayers());
            }

            @Override
            public void onStartGame() {
                started[0] = true;
                lobby.dispose();
                dispose();
                SwingUtilities.invokeLater(() -> new GameWindow(assignedName[0], client::sendScore, null).setVisible(true));
            }

            @Override
            public void onScoreboard(String scoreboardText) {
                ClientSessionManager.clearSession();
                JOptionPane.showMessageDialog(UI.this, scoreboardText, "ผลคะแนนออนไลน์", JOptionPane.INFORMATION_MESSAGE);
                client.disconnect();
            }

            @Override
            public void onError(String error) {
                if ("INVALID_TOKEN".equals(error)) {
                    ClientSessionManager.clearSession();
                    JOptionPane.showMessageDialog(UI.this, "ไม่สามารถเชื่อมต่อใหม่ได้ (เซสชั่นหมดอายุหรือห้องปิดไปแล้ว)", "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
                    lobby.dispose();
                    return;
                }
                JOptionPane.showMessageDialog(UI.this, error, "Online Error", JOptionPane.ERROR_MESSAGE);
            }

            @Override
            public void onDisconnected() {
                if (!started[0] && lobby.isDisplayable()) {
                    lobby.dispose();
                }
            }
        });

        leaveBtn.addActionListener(e -> {
            ClientSessionManager.clearSession();
            client.disconnect();
            lobby.dispose();
        });

        lobby.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Don't clear session on accidental close, only on explicit leave/game end
                client.disconnect();
            }
        });

        try {
            if (reconnectToken != null) {
                client.reconnect(reconnectToken);
            } else {
                client.connect();
            }
            lobby.setVisible(true);
        } catch (Exception e) {
            ClientSessionManager.clearSession();
            client.disconnect();
            JOptionPane.showMessageDialog(this, "เข้าห้องไม่สำเร็จ: " + e.getMessage(), "Online", JOptionPane.ERROR_MESSAGE);
        }
    }

    private OnlineRoomInfo showRoomBrowserDialog() {
        final JDialog dialog = new JDialog(this, "ค้นหาห้อง", true);
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        DefaultListModel<OnlineRoomInfo> listModel = new DefaultListModel<>();
        JList<OnlineRoomInfo> roomList = new JList<>(listModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setFont(uiFont(Font.PLAIN, 16));
        roomList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof OnlineRoomInfo) {
                    label.setText(value.toString());
                    label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                }
                return label;
            }
        });

        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton refreshButton = new JButton("รีเฟรช");
        JButton joinButton = new JButton("เข้าห้อง");
        JButton manualButton = new JButton("ใส่ IP เอง");
        JButton cancelButton = new JButton("ยกเลิก");

        joinButton.setEnabled(false);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(manualButton);
        bottomPanel.add(refreshButton);
        bottomPanel.add(joinButton);
        bottomPanel.add(cancelButton);

        dialog.add(statusLabel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(roomList), BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        final OnlineRoomInfo[] result = { null };

        Runnable searchAction = () -> {
            refreshButton.setEnabled(false);
            joinButton.setEnabled(false);
            statusLabel.setText("กำลังค้นหาห้องในเครือข่าย...");
            listModel.clear();

            new SwingWorker<List<OnlineRoomInfo>, Void>() {
                @Override
                protected List<OnlineRoomInfo> doInBackground() {
                    return LanDiscovery.discoverRooms(2000);
                }

                @Override
                protected void done() {
                    try {
                        List<OnlineRoomInfo> rooms = get();
                        if (rooms.isEmpty()) {
                            statusLabel.setText("ไม่พบห้อง (ลองกดรีเฟรช)");
                        } else {
                            rooms.forEach(listModel::addElement);
                            statusLabel.setText("พบ " + rooms.size() + " ห้อง");
                        }
                    } catch (Exception e) {
                        statusLabel.setText("เกิดข้อผิดพลาดในการค้นหา");
                    } finally {
                        refreshButton.setEnabled(true);
                    }
                }
            }.execute();
        };

        refreshButton.addActionListener(e -> searchAction.run());
        cancelButton.addActionListener(e -> dialog.dispose());
        joinButton.addActionListener(e -> {
            result[0] = roomList.getSelectedValue();
            dialog.dispose();
        });
        manualButton.addActionListener(e -> {
            JPanel manualPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            manualPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JTextField ipField = new JTextField("127.0.0.1");
            JTextField portField = new JTextField();

            manualPanel.add(new JLabel("IP Address:"));
            manualPanel.add(ipField);
            manualPanel.add(new JLabel("Port:"));
            manualPanel.add(portField);

            int option = JOptionPane.showConfirmDialog(
                dialog,
                manualPanel,
                "ใส่ IP และ Port",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (option == JOptionPane.OK_OPTION) {
                String ip = ipField.getText().trim();
                String portStr = portField.getText().trim();
                if (ip.isEmpty() || portStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "กรุณากรอกทั้ง IP และ Port", "ผิดพลาด", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    int port = Integer.parseInt(portStr);
                    result[0] = new OnlineRoomInfo("Manual Room", ip, port, 0, 3, "Unknown");
                    dialog.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "พอร์ตไม่ถูกต้อง (ต้องเป็นตัวเลขเท่านั้น)", "ผิดพลาด", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        roomList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                joinButton.setEnabled(roomList.getSelectedIndex() != -1);
            }
        });

        searchAction.run(); // Initial search
        dialog.setVisible(true);
        return result[0];
    }

    private String buildPlayerText(List<String> players) {
        StringBuilder text = new StringBuilder("ผู้เล่นในห้อง:\n");
        for (String player : players) {
            text.append("- ").append(player).append("\n");
        }
        return text.toString().trim();
    }

    // ================= GAME =================
    JPanel createGameScene() {

        JPanel p = new JPanel(null){
           protected void paintComponent(Graphics g){
                super.paintComponent(g);
                // เปลี่ยนจาก 1280, 820 เป็น getWidth(), getHeight()
                g.drawImage(bgImage.getImage(), 0, 0, getWidth(), getHeight(), null);
            }
        };
        p.setBounds(0, 0, 1280, 720);

        // TOP BAR
        JPanel topBar = new JPanel(null);
        topBar.setBounds(0, 0, 1280, 100);
        topBar.setOpaque(false);

        topBar.add(ovalLabel("📅 1", 40, 25));
        topBar.add(ovalLabel("⏰ 17.00", 200, 25));
        p.add(topBar);

        // JLabel girl = new JLabel(girlImage);
        // girl.setBounds(450,100,400,500);
        // p.add(girl);

        JButton shop = circleButton("🛒", 50, 200);
        shop.addActionListener(e -> JOptionPane.showMessageDialog(UI.this, "ร้านค้ามีไอเทมให้ซื้อ!"));
        JButton menu = circleButton("≡", 1180, 20);

        p.add(shop);
        p.add(menu);


        int y = 120;
        for(int i=1;i<=5;i++){
            JButton b = purpleButton("ตัวเลือกที่ " + i, 950, y);
            int choice = i;
            b.addActionListener(e -> {
                switch(choice){
                    case 1: dialogueText.setText("สาวน้อย: ดีจังเลย! นายชอบกินอะไรเหรอ?"); break;
                    case 2: dialogueText.setText("สาวน้อย: อืม... นายมาจากไหนเหรอ?"); break;
                    case 3: dialogueText.setText("สาวน้อย: วันนี้อากาศดีมากเลยนะ"); break;
                    case 4: dialogueText.setText("สาวน้อย: นายมีงานอดิเรกอะไรบ้าง?"); break;
                    case 5: dialogueText.setText("สาวน้อย: อยากไปเดินเล่นด้วยกันไหม?"); break;
                }
            });
            y += 80;
            p.add(b);
        }

        JPanel dialogue = createDialogueBox(
                "สาวน้อย",
                "สวัสดี... วันนี้อากาศดีนะ นายมาหาฉันอีกแล้วเหรอ?"
        );
        dialogue.setBounds(200, 520, 880, 180);
        p.add(dialogue);

        // POPUP MENU
        JPanel menuPopup = createPopup();
        menuPopup.setVisible(false);
        p.add(menuPopup);

        menu.addActionListener(e -> menuPopup.setVisible(true));

        return p;
    }

    // ================= POPUP =================
    JPanel createPopup(){
        JPanel panel = new JPanel(){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,new Color(255,120,160,220), getWidth(),getHeight(), new Color(120,100,255,220));
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),40,40);
            }
        };

        panel.setLayout(null);
        panel.setBounds(390, 150, 500, 450); // ปรับขนาดกล่อง Popup ให้พอดี
        
        // --- 1. กลุ่มปุ่มเมนูหลัก ---
        JButton save = purpleButton("Save game", 120, 60);
        save.addActionListener(e -> JOptionPane.showMessageDialog(UI.this, "บันทึกเกมแล้ว!"));
        JButton load = purpleButton("Load save", 120, 140);
        load.addActionListener(e -> JOptionPane.showMessageDialog(UI.this, "โหลดเซฟแล้ว!"));
        JButton displayScale = purpleButton("Display Scale", 120, 220);
        JButton exit = purpleButton("Exit", 120, 300);

        // --- 2. กลุ่มปุ่มเลือกขนาดจอ (ซ่อนไว้ตอนแรก) ---
        // แนะนำให้ใช้ขนาดที่สัดส่วนใกล้เคียงกัน ภาพจะได้ไม่ยืด/หดจนผิดรูป
        JButton size1 = purpleButton("1920 x 1080", 120, 60);
        JButton size2 = purpleButton("1280 x 720", 120, 140);
        JButton size3 = purpleButton("960 x 540", 120, 220);
        JButton backBtn = purpleButton("Back", 120, 300);

        size1.setVisible(false); size2.setVisible(false); size3.setVisible(false); backBtn.setVisible(false);

        // --- 3. ปุ่มปิด (X) ---
        JButton close = new JButton("X");
        close.setBounds(440,10,50,50);
        close.setBackground(Color.BLACK); close.setForeground(Color.WHITE); close.setFocusPainted(false);
        close.addActionListener(e -> panel.setVisible(false));

        // --- 4. จัดการ Event สลับเมนู ---
        displayScale.addActionListener(e -> {
            // ซ่อนเมนูหลัก แสดงเมนูขนาดจอ
            save.setVisible(false); load.setVisible(false); displayScale.setVisible(false); exit.setVisible(false);
            size1.setVisible(true); size2.setVisible(true); size3.setVisible(true); backBtn.setVisible(true);
        });

        backBtn.addActionListener(e -> {
            // ซ่อนเมนูขนาดจอ แสดงเมนูหลัก
            size1.setVisible(false); size2.setVisible(false); size3.setVisible(false); backBtn.setVisible(false);
            save.setVisible(true); load.setVisible(true); displayScale.setVisible(true); exit.setVisible(true);
        });

        // --- 5. Event การเปลี่ยนขนาดหน้าจอ ---
        size1.addActionListener(e -> changeScreenSize(1920, 1080));
        size2.addActionListener(e -> changeScreenSize(1280, 720));
        size3.addActionListener(e -> changeScreenSize(960, 540));
        
        exit.addActionListener(e -> System.exit(0));

        // แอดทุกปุ่มลงใน Panel
        panel.add(save); panel.add(load); panel.add(displayScale); panel.add(exit);
        panel.add(size1); panel.add(size2); panel.add(size3); panel.add(backBtn);
        panel.add(close);

        return panel;
    }

    // ================= DIALOGUE =================
    JPanel createDialogueBox(String name, String text){

        JPanel panel = new JPanel(){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255,255,255,230));
                g2.fillRoundRect(0,0,getWidth()-10,getHeight()-10,40,40);
                g2.dispose();
            }
        };

        panel.setLayout(null);
        panel.setOpaque(false);
        panel.setBounds(200,600,880,180);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setBounds(40,10,200,40);
        nameLabel.setOpaque(true);
        nameLabel.setBackground(new Color(255,120,160));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(uiFont(Font.BOLD, 22));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JTextArea dialogueText = new JTextArea(text);
        dialogueText.setBounds(40,60,780,80);
        dialogueText.setFont(uiFont(Font.PLAIN, 22));
        dialogueText.setLineWrap(true);
        dialogueText.setWrapStyleWord(true);
        dialogueText.setOpaque(false);
        dialogueText.setEditable(false);

        panel.add(nameLabel);
        panel.add(dialogueText);

        this.dialogueText = dialogueText;

        return panel;
    }

    // ================= DESIGN =================
    JButton pinkButton(String text,int x,int y){
        RoundedButton b = new RoundedButton(
                text,
                new Color(255,120,160),
                new Color(255,150,180)
        );
        b.setBounds(x,y,250,60);
        return b;
    }

    JButton purpleButton(String text,int x,int y){
        RoundedButton b = new RoundedButton(
                text,
                new Color(120,100,255),
                new Color(150,130,255)
        );
        b.setBounds(x,y,260,60);
        return b;
    }

    JButton circleButton(String text,int x,int y){
        RoundedButton b = new RoundedButton(
                text,
                new Color(255,100,150),
                new Color(255,130,170)
        );
        b.setBounds(x,y,60,60);
        return b;
    }

    JLabel ovalLabel(String text, int x, int y){
        JLabel label = new JLabel(text, SwingConstants.CENTER){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200,80,130));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),50,50);
                super.paintComponent(g);
            }
        };
        label.setBounds(x,y,150,50);
        label.setForeground(Color.WHITE);
        label.setFont(uiFont(Font.BOLD, 22));
        label.setOpaque(false);
        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UI::new);
    }
}

// ================= CUSTOM ROUNDED BUTTON =================

class RoundedButton extends JButton {

    private Color normalColor;
    private Color hoverColor;
    private boolean isHover = false;
    private int radius = 30;

    public RoundedButton(String text, Color normal, Color hover) {
        super(text);
        this.normalColor = normal;
        this.hoverColor = hover;

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(UI.uiFont(Font.BOLD, 22));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                isHover = true;
                repaint();
            }
            public void mouseExited(MouseEvent e) {
                isHover = false;
                repaint();
            }
        });
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(
                0,0,
                isHover ? hoverColor.brighter() : normalColor,
                getWidth(),getHeight(),
                isHover ? hoverColor : normalColor.darker()
        );

        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth()-5, getHeight()-5, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }

    protected void paintBorder(Graphics g) {}
}