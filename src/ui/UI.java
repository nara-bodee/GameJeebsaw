package ui;

import core.GameSettings;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import main.GameWindow;
import online.ClientSessionManager;
import online.LanDiscovery;
import online.OnlineClient;
import online.OnlineRoomInfo;
import online.OnlineServer;
import online.SessionInfo;

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

    private JTextArea dialogueText;
    private final Runnable onStartGame;
    private final boolean onlineOnlyMode;
    private JFrame mainFrameParent;

    public UI() {
        this(null, true, false, null);
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
        
        // 🔥 บังคับให้ช่องกรอกข้อความทั้งหมดใช้ Font ที่รองรับภาษาไทย
        UIManager.put("TextField.font", uiFont(Font.PLAIN, 19));
        UIManager.put("TextPane.font", uiFont(Font.PLAIN, 19));
        UIManager.put("EditorPane.font", uiFont(Font.PLAIN, 19));
    }

    public UI(Runnable onStartGame) {
        this(onStartGame, true, false, null);
    }

    public UI(Runnable onStartGame, boolean showWindow, boolean onlineOnlyMode) {
        this(onStartGame, showWindow, onlineOnlyMode, null);
    }

    public UI(Runnable onStartGame, boolean showWindow, boolean onlineOnlyMode, JFrame mainFrameParent) {
        applyUiFonts();
        this.onStartGame = onStartGame;
        this.onlineOnlyMode = onlineOnlyMode;
        this.mainFrameParent = mainFrameParent;
        GameSettings settings = GameSettings.getInstance();
        int currentWidth = settings.getScreenWidth();
        int currentHeight = settings.getScreenHeight();

        setTitle("เกมจีบสาว");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        getContentPane().setLayout(null);
        getContentPane().setBackground(Color.BLACK); 

        getContentPane().setPreferredSize(new Dimension(currentWidth, currentHeight));
        pack();

        layeredPane.setLayout(null);
        layeredPane.setBounds(0, 0, BASE_WIDTH, BASE_HEIGHT);

        startLayer = createStartScene();
        gameLayer = createGameScene();

        layeredPane.add(startLayer, Integer.valueOf(0));
        layeredPane.add(gameLayer, Integer.valueOf(1));
        gameLayer.setVisible(false);

        getContentPane().add(layeredPane);

        tagOriginalBounds(layeredPane);
        applyScale(getContentPane().getWidth(), getContentPane().getHeight());

        setLocationRelativeTo(null);
        if (showWindow) {
            setVisible(true);
        }
    }

    private void showUiFrameIfNeeded() {
        if (!onlineOnlyMode) {
            UI.this.setVisible(true);
        }
    }

    private void hideUiFrameIfNeeded() {
        if (!onlineOnlyMode) {
            UI.this.setVisible(false);
        }
    }

    public JFrame getMainFrameParent() {
        return mainFrameParent;
    }

    private void changeScreenSize(int newWidth, int newHeight) {
        GameSettings.getInstance().applyResolution(newWidth, newHeight, false);
        getContentPane().setPreferredSize(new Dimension(newWidth, newHeight));
        pack();
        setLocationRelativeTo(null); 
        applyScale(getContentPane().getWidth(), getContentPane().getHeight());
    }

    private void applyScale(int actualWidth, int actualHeight) {
        double scaleX = (double) actualWidth / BASE_WIDTH;
        double scaleY = (double) actualHeight / BASE_HEIGHT;
        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) Math.round(BASE_WIDTH * scale);
        int scaledHeight = (int) Math.round(BASE_HEIGHT * scale);

        int offsetX = (actualWidth - scaledWidth) / 2;
        int offsetY = (actualHeight - scaledHeight) / 2;

        scaleFromBase(layeredPane, scale);

        layeredPane.setBounds(offsetX, offsetY, scaledWidth, scaledHeight);
        startLayer.setBounds(0, 0, scaledWidth, scaledHeight);
        gameLayer.setBounds(0, 0, scaledWidth, scaledHeight);

        revalidate();
        repaint();
    }

    private void tagOriginalBounds(Container container) {
        if (container instanceof JComponent) {
            JComponent comp = (JComponent) container;
            comp.putClientProperty("baseBounds", comp.getBounds());
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

    JPanel createStartScene() {
        JPanel p = new JPanel(null){
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
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
        showOnlineMenu(mainFrameParent != null ? mainFrameParent : this);
    }

    private void showOnlineMenu(JFrame parentFrame) {
        SessionInfo lastSession = ClientSessionManager.loadSession();
        if (lastSession != null) {
            int choice = JOptionPane.showConfirmDialog(
                parentFrame,
                "พบเกมที่ยังไม่จบในห้อง '" + lastSession.roomName() + "'\nคุณต้องการเชื่อมต่อใหม่หรือไม่?",
                "เชื่อมต่อใหม่",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                attemptReconnect(lastSession, parentFrame);
                return;
            } else {
                ClientSessionManager.clearSession();
            }
        }

        String playerName = JOptionPane.showInputDialog(parentFrame, "ใส่ชื่อผู้เล่น:", "Online LAN", JOptionPane.PLAIN_MESSAGE);
        if (playerName == null) return;
        playerName = playerName.trim();
        if (playerName.isEmpty()) playerName = "Player";

        Object[] options = {"สร้างห้อง (Host)", "เข้าห้อง (Join)", "ยกเลิก"};
        int selected = JOptionPane.showOptionDialog(
            parentFrame,
            "เลือกโหมดออนไลน์",
            "Online LAN",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );

        if (selected < 0 || options[selected].equals("ยกเลิก")) return;

        if (options[selected].equals("สร้างห้อง (Host)")) {
            startHostLobby(playerName, parentFrame);
        } else if (options[selected].equals("เข้าห้อง (Join)")) {
            startJoinLobby(playerName, parentFrame);
        }
    }

    public void openOnlineMenu() {
        showOnlineMenu(mainFrameParent != null ? mainFrameParent : this);
    }

    // 🔥 วาดหน้าจอหลอด LED และรายชื่อผู้เล่น
    private void updatePlayerDisplay(JTextPane playersArea, List<String> players, Map<String, Boolean> readyMap) {
        StringBuilder html = new StringBuilder("<html><body style='padding: 5px; margin: 0;'>");
        html.append("<b>ผู้เล่นในห้อง:</b><br>");
        
        if (players != null) {
            for (String playerStr : players) {
                String rawName = playerStr.replace(" (DC)", "");
                boolean isReady = readyMap != null && readyMap.getOrDefault(rawName, false);

                // 🔥 สีเขียวสว่าง (Ready) และ สีเทา (Not Ready)
                String color = isReady ? "#32CD32" : "#888888"; 
                
                html.append("<span style='color: ").append(color).append("; font-size: 130%;'>●</span> ")
                    .append("<span style='color: black;'>").append(playerStr).append("</span>")
                    .append("<br>");
            }
        }
        
        html.append("</body></html>");
        playersArea.setText(html.toString());
    }

    private void attemptReconnect(SessionInfo session, JFrame parentFrame) {
        OnlineRoomInfo dummyRoom = new OnlineRoomInfo(session.roomName(), session.host(), session.port(), 0, 0, "Unknown");
        startJoinOrReconnectLobby(session.playerName(), dummyRoom, session.token(), parentFrame);
    }

    private void startJoinLobby(String playerName, JFrame parentFrame) {
        OnlineRoomInfo selectedRoom = showRoomBrowserDialog(parentFrame);
        if (selectedRoom == null) return;
        startJoinOrReconnectLobby(playerName, selectedRoom, null, parentFrame);
    }

    private void startJoinOrReconnectLobby(String playerName, OnlineRoomInfo roomInfo, String reconnectToken, JFrame parentFrame) {
        String ip = roomInfo.getHostAddress();
        int port = roomInfo.getPort();

        OnlineClient client = new OnlineClient(ip, port, playerName);

        JFrame dialogParent = parentFrame != null ? parentFrame : this;
        JDialog lobby = new JDialog(dialogParent, reconnectToken != null ? "Reconnecting..." : "Online Lobby", true);
        lobby.setSize(500, 420);
        lobby.setLocationRelativeTo(dialogParent);
        lobby.setLayout(new BorderLayout(10, 10));

        JLabel roomInfoLabel = new JLabel("กำลังเชื่อมต่อ...");
        roomInfoLabel.setFont(uiFont(Font.PLAIN, 20));
        roomInfoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // 🔥 ใช้ JTextPane รองรับ HTML
        JTextPane playersArea = new JTextPane();
        playersArea.setContentType("text/html");
        playersArea.setEditable(false);
        playersArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        playersArea.setFont(uiFont(Font.PLAIN, 20));

        JButton readyBtn = new JButton("Ready");
        JButton leaveBtn = new JButton("ออกจากห้อง");

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(readyBtn);
        bottomPanel.add(leaveBtn);

        final boolean[] ready = {false};
        final GameWindow[] activeGame = {null}; 

        List<String> currentPlayers = new ArrayList<>();
        Map<String, Boolean> readyMap = new HashMap<>();
        final int[] max = {3}; 
        final String[] assignedName = {playerName};

        client.setListener(new OnlineClient.ClientListener() {
            @Override
            public void onConnected(String actualName, String roomName, int maxPlayers, int currentPlayersList) {
                assignedName[0] = actualName;
                max[0] = maxPlayers;
                roomInfoLabel.setText("ห้อง: " + roomName + " | พอร์ต: " + port + " | " + currentPlayersList + "/" + maxPlayers);
                
                String token = client.getSessionToken();
                if (token != null) {
                    SessionInfo sessionInfo = new SessionInfo(ip, port, roomName, actualName, token);
                    ClientSessionManager.saveSession(sessionInfo);
                }
            }
            
            @Override
public void onStateSync(String state) {
    if (activeGame[0] != null) {
        // 🌟 ถ้าเชื่อมต่อกลับมาได้ตอนที่หน้าต่างเกมยังเปิดอยู่ ให้คืนค่า UI
        activeGame[0].setConnectionState(false);
        JOptionPane.showMessageDialog(activeGame[0], "เชื่อมต่อกลับเข้าห้องสำเร็จ!");
    } else if (reconnectToken != null) {
        roomInfoLabel.setText("เชื่อมต่อสำเร็จ! กำลังโหลดข้อมูล...");
        if (state.contains("GAME_STARTED=true")) {
            onStartGame();
        }
    }
}

            @Override
            public void onPlayerListChanged(List<String> players) {
                currentPlayers.clear();
                currentPlayers.addAll(players);
                updatePlayerDisplay(playersArea, currentPlayers, readyMap);
                String rName = roomInfo.getRoomName() != null ? roomInfo.getRoomName() : "Room";
                roomInfoLabel.setText("ห้อง: " + rName + " | พอร์ต: " + port + " | " + players.size() + "/" + max[0]);
            }

            @Override
            public void onReadyStatus(String status) {
                readyMap.clear();
                if (!status.isEmpty()) {
                    String[] parts = status.split(",");
                    for (String p : parts) {
                        String[] pair = p.split(":");
                        if (pair.length == 2) {
                            readyMap.put(pair[0], Boolean.parseBoolean(pair[1]));
                        }
                    }
                }
                updatePlayerDisplay(playersArea, currentPlayers, readyMap);
            }

            @Override 
public void onStartGame() {
    lobby.setVisible(false);
    hideUiFrameIfNeeded();
    
    // 🌟 ใส่ Runnable สำหรับปุ่ม Reconnect ลงไป
    activeGame[0] = new GameWindow(assignedName[0], score -> {
        client.sendScore(score); 
        if (activeGame[0] != null) {
            activeGame[0].dispose();
            activeGame[0] = null;
        }
        showUiFrameIfNeeded();
        lobby.setVisible(true);
    }, () -> {
        // โค้ดนี้จะทำงานเมื่อผู้เล่นกดปุ่ม Reconnect ใน GameWindow
        try {
            String token = client.getSessionToken();
            if (token != null) {
                client.reconnect(token);
            } else {
                JOptionPane.showMessageDialog(activeGame[0], "ไม่มีข้อมูล Token สำหรับเชื่อมต่อใหม่");
                activeGame[0].setConnectionState(true); // รีเซ็ตปุ่มให้กดใหม่ได้
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(activeGame[0], "เชื่อมต่อล้มเหลว: " + ex.getMessage());
            activeGame[0].setConnectionState(true); // รีเซ็ตปุ่มให้กดใหม่ได้
        }
    });
    
    activeGame[0].setVisible(true);
}

            @Override 
            public void onScoreboard(String board) {
                ClientSessionManager.clearSession();
                if (activeGame[0] != null) {
                    activeGame[0].dispose();
                }
                JOptionPane.showMessageDialog(lobby, board, "สรุปผลคะแนน", JOptionPane.INFORMATION_MESSAGE);
                showUiFrameIfNeeded();
                lobby.setVisible(true);
                
                readyBtn.setText("Ready");
                ready[0] = false;
                client.sendUnready();
            }

            @Override 
            public void onError(String e) {
                if ("INVALID_TOKEN".equals(e) || e.contains("ไม่สามารถกลับเข้าห้องได้")) {
                    ClientSessionManager.clearSession();
                    JOptionPane.showMessageDialog(lobby, "ไม่สามารถเชื่อมต่อใหม่ได้: " + e, "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
                    lobby.dispose();
                    showUiFrameIfNeeded();
                    return;
                }
                JOptionPane.showMessageDialog(lobby, "Error: " + e, "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
            }

            @Override 
public void onDisconnected() {
    if (activeGame[0] != null) {
        // 🌟 ถ้ากำลังอยู่ในเกม ห้าม dispose() หน้าต่างทิ้ง!
        // แต่ให้เรียกโหมดหน้าจอเน็ตหลุดแทน
        activeGame[0].setConnectionState(true);
    } else {
        // ถ้ายังอยู่ที่หน้า Lobby ค่อยเตะกลับไปเมนูหลัก
        lobby.dispose();
        JOptionPane.showMessageDialog(lobby, "เซิร์ฟเวอร์ถูกปิด หรือการเชื่อมต่อขาดหายไป", "ตัดการเชื่อมต่อ", JOptionPane.WARNING_MESSAGE);
        showUiFrameIfNeeded();
    }
}

            @Override public void onRole(String r) {}
            @Override public void onAllReady() {}
        });

        readyBtn.addActionListener(e -> {
            if (!ready[0]) {
                client.sendReady();
                readyBtn.setText("Unready");
                ready[0] = true;
            } else {
                client.sendUnready();
                readyBtn.setText("Ready");
                ready[0] = false;
            }
        });

        leaveBtn.addActionListener(e -> {
            ClientSessionManager.clearSession();
            client.disconnect();
            lobby.dispose();
            showUiFrameIfNeeded(); 
        });

        lobby.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        lobby.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                client.disconnect();
                lobby.dispose();
                showUiFrameIfNeeded();
            }
        });

        lobby.add(roomInfoLabel, BorderLayout.NORTH);
        lobby.add(new JScrollPane(playersArea), BorderLayout.CENTER);
        lobby.add(bottomPanel, BorderLayout.SOUTH);

        try {
            if (reconnectToken != null) {
                client.reconnect(reconnectToken);
            } else {
                client.connect();
            }
            lobby.setVisible(true);
        } catch (Exception e) {
            ClientSessionManager.clearSession();
            JOptionPane.showMessageDialog(this, "เชื่อมต่อไม่สำเร็จ");
        }
    }

    private void startHostLobby(String playerName, JFrame parentFrame) {
        try {
            OnlineServer server = new OnlineServer("Room-1", playerName, 3);
            server.start();

            // ใช้ Localhost เพื่อให้ Host ใช้ระบบ Client เชื่อมตัวเอง (มีปุ่ม Ready)
            OnlineClient hostClient = new OnlineClient("127.0.0.1", server.getPort(), playerName);

            JFrame dialogParent = parentFrame != null ? parentFrame : this;
            JDialog lobby = new JDialog(dialogParent, "Online Room (Host)", true);
            lobby.setSize(500, 420);
            lobby.setLocationRelativeTo(dialogParent);
            lobby.setLayout(new BorderLayout(10, 10));

            JLabel roomInfo = new JLabel("ห้อง: " + server.getRoomName() + " | พอร์ต: " + server.getPort() + " | 1/3");
            roomInfo.setFont(uiFont(Font.PLAIN, 20));
            roomInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

            JTextPane playersArea = new JTextPane();
            playersArea.setContentType("text/html");
            playersArea.setEditable(false);
            playersArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
            playersArea.setFont(uiFont(Font.PLAIN, 20));

            JButton startBtn = new JButton("เริ่มเกม");
            startBtn.setEnabled(false); 

            JButton closeBtn = new JButton("ปิดห้อง");

            JPanel bottom = new JPanel();
            bottom.add(startBtn);
            bottom.add(closeBtn);
            
            JButton readyBtn = new JButton("Ready");
            bottom.add(readyBtn);
            
            final boolean[] ready = {false};
            final GameWindow[] activeGame = {null}; 

            List<String> currentPlayers = new ArrayList<>();
            Map<String, Boolean> readyMap = new HashMap<>();
            
            final int[] maxHost = {3};

            readyBtn.addActionListener(e -> {
                if (!ready[0]) {
                    hostClient.sendReady();
                    readyBtn.setText("Unready");
                    ready[0] = true;
                } else {
                    hostClient.sendUnready();
                    readyBtn.setText("Ready");
                    ready[0] = false;
                }
            });

            hostClient.setListener(new OnlineClient.ClientListener() {
                
                @Override
                public void onConnected(String playerId, String roomName, int maxPlayers, int currentPlayersList) {
                    maxHost[0] = maxPlayers;
                    roomInfo.setText("ห้อง: " + server.getRoomName() + " | พอร์ต: " + server.getPort() + " | " + currentPlayersList + "/" + maxPlayers);
                }

                @Override
                public void onPlayerListChanged(List<String> players) {
                    currentPlayers.clear();
                    currentPlayers.addAll(players);
                    updatePlayerDisplay(playersArea, currentPlayers, readyMap);
                    roomInfo.setText("ห้อง: " + server.getRoomName() + " | พอร์ต: " + server.getPort() + " | " + players.size() + "/" + maxHost[0]);
                }

                @Override
                public void onReadyStatus(String status) {
                    boolean everyoneReady = true;
                    readyMap.clear();
                    if (!status.isEmpty()) {
                        String[] parts = status.split(",");
                        for (String p : parts) {
                            String[] pair = p.split(":");
                            if (pair.length == 2) {
                                boolean isR = Boolean.parseBoolean(pair[1]);
                                readyMap.put(pair[0], isR);
                                if (!isR) everyoneReady = false;
                            }
                        }
                    } else {
                        everyoneReady = false;
                    }
                    startBtn.setEnabled(everyoneReady);
                    updatePlayerDisplay(playersArea, currentPlayers, readyMap);
                }

                @Override 
                public void onStartGame() {
                    lobby.setVisible(false); 
                    hideUiFrameIfNeeded(); 
                    
                    activeGame[0] = new GameWindow(playerName, score -> {
                        hostClient.sendScore(score);
                        if (activeGame[0] != null) {
                            activeGame[0].dispose();
                            activeGame[0] = null;
                        }
                        showUiFrameIfNeeded();
                        lobby.setVisible(true);
                    }, null);
                    activeGame[0].setVisible(true);
                }

                @Override 
                public void onScoreboard(String board) {
                    if (activeGame[0] != null) {
                        activeGame[0].dispose();
                    }
                    JOptionPane.showMessageDialog(lobby, board, "สรุปผลคะแนน", JOptionPane.INFORMATION_MESSAGE);
                    showUiFrameIfNeeded();
                    lobby.setVisible(true);
                    
                    readyBtn.setText("Ready");
                    ready[0] = false;
                    hostClient.sendUnready();
                }

                @Override public void onStateSync(String s){}
                @Override public void onError(String e){}
                @Override public void onDisconnected(){}
                @Override public void onRole(String r){}
                @Override public void onAllReady(){}
            });

            server.setListener(new OnlineServer.ServerListener() {
                @Override
                public void onPlayerListChanged(List<String> players) {} 
                @Override
                public void onScoreboardReady(String scoreboardText) {}
                @Override
                public void onError(String error) {
                    JOptionPane.showMessageDialog(parentFrame, error);
                }
            });

            startBtn.addActionListener(e -> {
                server.startGame();
            });

            closeBtn.addActionListener(e -> {
                server.stop();
                hostClient.disconnect();
                lobby.dispose();
                showUiFrameIfNeeded(); 
            });

            lobby.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            lobby.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    server.stop();
                    hostClient.disconnect();
                    lobby.dispose();
                    showUiFrameIfNeeded();
                }
            });

            lobby.add(roomInfo, BorderLayout.NORTH);
            lobby.add(new JScrollPane(playersArea), BorderLayout.CENTER);
            lobby.add(bottom, BorderLayout.SOUTH);
            hostClient.connect();
            lobby.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "สร้างห้องไม่สำเร็จ: " + e.getMessage());
        }
    }

    private OnlineRoomInfo showRoomBrowserDialog(JFrame parentFrame) {
        final JDialog dialog = new JDialog(parentFrame, "ค้นหาห้อง", true);
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
            JTextField ipField = new JTextField("");
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

        searchAction.run(); 
        dialog.setVisible(true);
        return result[0];
    }

    // ================= GAME =================
    JPanel createGameScene() {
        JPanel p = new JPanel(null){
           protected void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(bgImage.getImage(), 0, 0, getWidth(), getHeight(), null);
            }
        };
        p.setBounds(0, 0, 1280, 720);

        JPanel topBar = new JPanel(null);
        topBar.setBounds(0, 0, 1280, 100);
        topBar.setOpaque(false);

        topBar.add(ovalLabel("📅 1", 40, 25));
        topBar.add(ovalLabel("⏰ 17.00", 200, 25));
        p.add(topBar);

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
        panel.setBounds(390, 150, 500, 450); 
        
        JButton save = purpleButton("Save game", 120, 60);
        save.addActionListener(e -> JOptionPane.showMessageDialog(UI.this, "บันทึกเกมแล้ว!"));
        JButton load = purpleButton("Load save", 120, 140);
        load.addActionListener(e -> JOptionPane.showMessageDialog(UI.this, "โหลดเซฟแล้ว!"));
        JButton displayScale = purpleButton("Display Scale", 120, 220);
        JButton exit = purpleButton("Exit", 120, 300);

        JButton close = new JButton("X");
        close.setBounds(440,10,50,50);
        close.setBackground(Color.BLACK); close.setForeground(Color.WHITE); close.setFocusPainted(false);
        close.addActionListener(e -> panel.setVisible(false));

        displayScale.addActionListener(e -> {
            if (mainFrameParent != null && mainFrameParent instanceof main.MainFrame) {
                ((main.MainFrame) mainFrameParent).showSettings();
                panel.setVisible(false);
            }
        });
        
        exit.addActionListener(e -> System.exit(0));

        panel.add(save); panel.add(load); panel.add(displayScale); panel.add(exit);
        panel.add(close);

        return panel;
    }

    // ================= DIALOGUE =================
    JPanel createDialogueBox(String name, String text){
        JPanel panel = new JPanel(){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        RoundedButton b = new RoundedButton(text, new Color(255,120,160), new Color(255,150,180));
        b.setBounds(x,y,250,60);
        return b;
    }

    JButton purpleButton(String text,int x,int y){
        RoundedButton b = new RoundedButton(text, new Color(120,100,255), new Color(150,130,255));
        b.setBounds(x,y,260,60);
        return b;
    }

    JButton circleButton(String text,int x,int y){
        RoundedButton b = new RoundedButton(text, new Color(255,100,150), new Color(255,130,170));
        b.setBounds(x,y,60,60);
        return b;
    }

    JLabel ovalLabel(String text, int x, int y){
        JLabel label = new JLabel(text, SwingConstants.CENTER){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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