package ui;

import core.GameSettings;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import main.GameWindow;
import online.LanDiscovery;
import online.OnlineClient;
import online.OnlineRoomInfo;
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
        
        // 🔥 เพิ่ม 3 บรรทัดนี้ เพื่อบังคับให้ช่องกรอกข้อความทั้งหมดใช้ Font ที่รองรับภาษาไทย
        UIManager.put("TextField.font", uiFont(Font.PLAIN, 19));
        UIManager.put("TextPane.font", uiFont(Font.PLAIN, 19));
        UIManager.put("EditorPane.font", uiFont(Font.PLAIN, 19));
    }

    public UI(Runnable onStartGame) {
        applyUiFonts();
        this.onStartGame = onStartGame;
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
        setVisible(true);
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
        String[] recData = OnlineClient.loadReconnectData();
        List<String> optionsList = new ArrayList<>();
        optionsList.add("สร้างห้อง (Host)");
        optionsList.add("เข้าห้อง (Join)");
        
        if (recData != null && recData.length == 4) {
            optionsList.add("กลับเข้าห้องเดิม (Reconnect)");
        }
        optionsList.add("ยกเลิก");

        Object[] options = optionsList.toArray();
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

        if (selected < 0 || options[selected].equals("ยกเลิก")) return;

        String choice = options[selected].toString();

        if (choice.equals("กลับเข้าห้องเดิม (Reconnect)")) {
            startReconnectLobby(recData);
            return;
        }

        String playerName = JOptionPane.showInputDialog(this, "ใส่ชื่อผู้เล่น:", "Online LAN", JOptionPane.PLAIN_MESSAGE);
        if (playerName == null) return;
        playerName = playerName.trim();
        if (playerName.isEmpty()) playerName = "Player";

        if (choice.equals("สร้างห้อง (Host)")) {
            startHostLobby(playerName);
        } else if (choice.equals("เข้าห้อง (Join)")) {
            startJoinLobby(playerName);
        }
    }

    // 🔥 เมธอดใหม่สำหรับวาดหน้าจอหลอด LED และรายชื่อผู้เล่น
    private void updatePlayerDisplay(JTextPane playersArea, List<String> players, Map<String, Boolean> readyMap) {
        StringBuilder html = new StringBuilder("<html><body style='padding: 5px; margin: 0;'>");
        html.append("<b>ผู้เล่นในห้อง:</b><br>");
        
        if (players != null) {
            for (String playerStr : players) {
                // ลบคำว่า (DC) ออกชั่วคราวเพื่อเอาไปเช็คชื่อใน Map
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

    private void startReconnectLobby(String[] recData) {
        String ip = recData[0];
        int port = Integer.parseInt(recData[1]);
        String token = recData[2];
        String playerName = recData[3];

        OnlineClient client = new OnlineClient(ip, port, playerName);

        JDialog lobby = new JDialog(this, "Reconnecting...", false);
        lobby.setSize(500, 420);
        lobby.setLocationRelativeTo(this);
        lobby.setLayout(new BorderLayout(10, 10));

        JLabel roomInfo = new JLabel("กำลังเชื่อมต่อกลับไปที่ห้อง...");
        roomInfo.setFont(uiFont(Font.PLAIN, 20));
        roomInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // 🔥 เปลี่ยนจาก JTextArea เป็น JTextPane เพื่อรองรับ HTML (หลอด LED)
        JTextPane playersArea = new JTextPane();
        playersArea.setContentType("text/html");
        playersArea.setEditable(false);
        playersArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        playersArea.setFont(uiFont(Font.PLAIN, 20));

        JButton readyBtn = new JButton("Ready");
        JButton leaveBtn = new JButton("ออกจากห้อง");

        JPanel bottom = new JPanel();
        bottom.add(readyBtn);
        bottom.add(leaveBtn);

        final boolean[] ready = {false};
        final GameWindow[] activeGame = {null}; 
        
        // 🔥 ตัวแปรเก็บรายชื่อและสถานะ Ready
        List<String> currentPlayers = new ArrayList<>();
        Map<String, Boolean> readyMap = new HashMap<>();

        client.setListener(new OnlineClient.ClientListener() {
            @Override
            public void onStateSync(String state) {
                roomInfo.setText("เชื่อมต่อสำเร็จ! กำลังโหลดข้อมูล...");
                if (state.contains("GAME_STARTED=true")) {
                    lobby.setVisible(false); 
                    UI.this.setVisible(false); 
                    
                    activeGame[0] = new GameWindow(playerName, score -> {
                        client.sendScore(score); 
                        if (activeGame[0] != null) {
                            activeGame[0].dispose();
                            activeGame[0] = null;
                        }
                        UI.this.setVisible(true);
                        lobby.setVisible(true);
                    });
                    activeGame[0].setVisible(true);
                }
            }
            
            @Override
            public void onPlayerListChanged(List<String> players) {
                currentPlayers.clear();
                currentPlayers.addAll(players);
                updatePlayerDisplay(playersArea, currentPlayers, readyMap);
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
                UI.this.setVisible(false);
                activeGame[0] = new GameWindow(playerName, score -> {
                    client.sendScore(score);
                    if (activeGame[0] != null) {
                        activeGame[0].dispose();
                        activeGame[0] = null;
                    }
                    UI.this.setVisible(true);
                    lobby.setVisible(true);
                });
                activeGame[0].setVisible(true);
            }
            
            @Override
            public void onError(String error) {
                JOptionPane.showMessageDialog(UI.this, "ไม่สามารถกลับเข้าห้องได้: " + error + "\n(อาจเกินเวลา Grace Period หรือเซิร์ฟเวอร์ปิดไปแล้ว)");
                OnlineClient.clearReconnectData(); 
                lobby.dispose();
                UI.this.setVisible(true);
            }
            
            @Override 
            public void onScoreboard(String board) {
                if (activeGame[0] != null) {
                    activeGame[0].dispose();
                }
                JOptionPane.showMessageDialog(lobby, board, "สรุปผลคะแนน", JOptionPane.INFORMATION_MESSAGE);
                UI.this.setVisible(true);
                lobby.setVisible(true);
                readyBtn.setText("Ready");
                ready[0] = false;
                client.sendUnready();
            }
            
            @Override 
            public void onDisconnected() {
                if (activeGame[0] != null) {
                    activeGame[0].dispose();
                    activeGame[0] = null;
                }
                lobby.dispose();
                JOptionPane.showMessageDialog(UI.this, "เซิร์ฟเวอร์ถูกปิด หรือการเชื่อมต่อขาดหายไป", "ตัดการเชื่อมต่อ", JOptionPane.WARNING_MESSAGE);
                UI.this.setVisible(true);
            }
            
            @Override public void onConnected(String a, String b, int c, int d) {}
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
            client.disconnect();
            lobby.dispose();
            UI.this.setVisible(true);
        });

        lobby.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        lobby.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                client.disconnect();
                lobby.dispose();
                UI.this.setVisible(true);
            }
        });

        lobby.add(roomInfo, BorderLayout.NORTH);
        lobby.add(new JScrollPane(playersArea), BorderLayout.CENTER);
        lobby.add(bottom, BorderLayout.SOUTH);

        try {
            client.connectWithToken(token);
            lobby.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "เชื่อมต่อเซิร์ฟเวอร์ไม่สำเร็จ");
            OnlineClient.clearReconnectData();
        }
    }

    private void startHostLobby(String playerName) {
        try {
            OnlineServer server = new OnlineServer("Room-1", playerName, 3);
            server.start();

            // ใช้ Localhost ป้องกันปัญหา IP จำลอง
            OnlineClient hostClient = new OnlineClient("127.0.0.1",
                    server.getPort(),
                    playerName);
            

            JDialog lobby = new JDialog(this, "Online Room (Host)", false);
            lobby.setSize(500, 420);
            lobby.setLocationRelativeTo(this);
            lobby.setLayout(new BorderLayout(10, 10));

            JLabel roomInfo = new JLabel("ห้อง: " + server.getRoomName() + " | พอร์ต: " + server.getPort());
            roomInfo.setFont(uiFont(Font.PLAIN, 20));
            roomInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

            // 🔥 เปลี่ยนจาก JTextArea เป็น JTextPane
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

            // 🔥 ตัวแปรเก็บรายชื่อและสถานะ Ready
            List<String> currentPlayers = new ArrayList<>();
            Map<String, Boolean> readyMap = new HashMap<>();

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
                public void onPlayerListChanged(List<String> players) {
                    currentPlayers.clear();
                    currentPlayers.addAll(players);
                    updatePlayerDisplay(playersArea, currentPlayers, readyMap);
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
                    UI.this.setVisible(false); 
                    
                    activeGame[0] = new GameWindow(playerName, score -> {
                        hostClient.sendScore(score);
                        if (activeGame[0] != null) {
                            activeGame[0].dispose();
                            activeGame[0] = null;
                        }
                        UI.this.setVisible(true);
                        lobby.setVisible(true);
                    });
                    activeGame[0].setVisible(true);
                }

                @Override 
                public void onScoreboard(String board) {
                    if (activeGame[0] != null) {
                        activeGame[0].dispose();
                    }
                    JOptionPane.showMessageDialog(lobby, board, "สรุปผลคะแนน", JOptionPane.INFORMATION_MESSAGE);
                    UI.this.setVisible(true);
                    lobby.setVisible(true);
                    
                    readyBtn.setText("Ready");
                    ready[0] = false;
                    hostClient.sendUnready();
                }

                @Override public void onConnected(String a,String b,int c,int d){}
                @Override public void onStateSync(String s){}
                @Override public void onError(String e){}
                @Override public void onDisconnected(){}
                @Override public void onRole(String r){}
                @Override public void onAllReady(){}
            });

            server.setListener(new OnlineServer.ServerListener() {
                @Override
                public void onPlayerListChanged(List<String> players) {} // จัดการโดย hostClient แล้ว
                @Override
                public void onScoreboardReady(String scoreboardText) {}
                @Override
                public void onError(String error) {
                    JOptionPane.showMessageDialog(UI.this, error);
                }
            });

            startBtn.addActionListener(e -> {
                server.startGame();
            });

            closeBtn.addActionListener(e -> {
                server.stop();
                hostClient.disconnect();
                lobby.dispose();
                UI.this.setVisible(true); 
            });

            lobby.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            lobby.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    server.stop();
                    hostClient.disconnect();
                    lobby.dispose();
                    UI.this.setVisible(true);
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

    private void startJoinLobby(String playerName) {
        final int[] max = {0};

        List<OnlineRoomInfo> rooms = LanDiscovery.discoverRooms(1500);
        OnlineRoomInfo selectedRoom = chooseRoom(rooms);
        if (selectedRoom == null) return;

        OnlineClient client = new OnlineClient(
                selectedRoom.getHostAddress(),
                selectedRoom.getPort(),
                playerName
        );

        JDialog lobby = new JDialog(this, "Online Lobby", false);
        lobby.setSize(500, 420);
        lobby.setLocationRelativeTo(this);
        lobby.setLayout(new BorderLayout(10, 10));

        JLabel roomInfo = new JLabel("กำลังเชื่อมต่อ...");
        roomInfo.setFont(uiFont(Font.PLAIN, 20));
        roomInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // 🔥 เปลี่ยนจาก JTextArea เป็น JTextPane
        JTextPane playersArea = new JTextPane();
        playersArea.setContentType("text/html");
        playersArea.setEditable(false);
        playersArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        playersArea.setFont(uiFont(Font.PLAIN, 20));

        JButton readyBtn = new JButton("Ready");
        JButton leaveBtn = new JButton("ออกจากห้อง");

        JPanel bottom = new JPanel();
        bottom.add(readyBtn);
        bottom.add(leaveBtn);

        final boolean[] ready = {false};
        final GameWindow[] activeGame = {null}; 

        // 🔥 ตัวแปรเก็บรายชื่อและสถานะ Ready
        List<String> currentPlayers = new ArrayList<>();
        Map<String, Boolean> readyMap = new HashMap<>();

        client.setListener(new OnlineClient.ClientListener() {
            @Override
            public void onConnected(String playerId, String roomName, int maxPlayers, int currentPlayersList) {
                max[0] = maxPlayers;
                roomInfo.setText("ห้อง: " + roomName + " | " + currentPlayersList + "/" + maxPlayers);
            }

            @Override
            public void onPlayerListChanged(List<String> players) {
                currentPlayers.clear();
                currentPlayers.addAll(players);
                updatePlayerDisplay(playersArea, currentPlayers, readyMap);
                roomInfo.setText("ห้อง: " + selectedRoom.getRoomName() + " | " + players.size() + "/" + max[0]);
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
                UI.this.setVisible(false);
                
                activeGame[0] = new GameWindow(playerName, score -> {
                    client.sendScore(score); 
                    if (activeGame[0] != null) {
                        activeGame[0].dispose();
                        activeGame[0] = null;
                    }
                    UI.this.setVisible(true);
                    lobby.setVisible(true);
                });
                activeGame[0].setVisible(true);
            }

            @Override 
            public void onScoreboard(String board) {
                if (activeGame[0] != null) {
                    activeGame[0].dispose();
                }
                JOptionPane.showMessageDialog(lobby, board, "สรุปผลคะแนน", JOptionPane.INFORMATION_MESSAGE);
                UI.this.setVisible(true);
                lobby.setVisible(true);
                
                readyBtn.setText("Ready");
                ready[0] = false;
                client.sendUnready();
            }

            @Override public void onStateSync(String s) {}
            
            @Override 
            public void onError(String e) {
                System.out.println("Client Error: " + e);
            }
            
            @Override 
            public void onDisconnected() {
                if (activeGame[0] != null) {
                    activeGame[0].dispose();
                    activeGame[0] = null;
                }
                lobby.dispose();
                JOptionPane.showMessageDialog(UI.this, "เซิร์ฟเวอร์ถูกปิด หรือการเชื่อมต่อขาดหายไป", "ตัดการเชื่อมต่อ", JOptionPane.WARNING_MESSAGE);
                UI.this.setVisible(true);
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
            client.disconnect();
            lobby.dispose();
            UI.this.setVisible(true); 
        });

        lobby.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        lobby.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                client.disconnect();
                lobby.dispose();
                UI.this.setVisible(true);
            }
        });

        lobby.add(roomInfo, BorderLayout.NORTH);
        lobby.add(new JScrollPane(playersArea), BorderLayout.CENTER);
        lobby.add(bottom, BorderLayout.SOUTH);

        try {
            client.connect();
            lobby.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "เข้าห้องไม่สำเร็จ");
        }
    }

    private OnlineRoomInfo chooseRoom(List<OnlineRoomInfo> rooms) {
        if (rooms != null && !rooms.isEmpty()) {
            Object selected = JOptionPane.showInputDialog(
                    this,
                    "เลือกห้องที่ต้องการเข้า",
                    "Join Room",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    rooms.toArray(),
                    rooms.get(0)
            );

            if (selected instanceof OnlineRoomInfo) {
                return (OnlineRoomInfo) selected;
            }
            return null;
        }

        JTextField ipField = new JTextField("");
        JTextField portField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("IP:"));
        panel.add(ipField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "ใส่ IP และ Port",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String ip = ipField.getText().trim();
        String portText = portField.getText().trim();

        if (ip.isEmpty() || portText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "กรุณากรอก IP และ Port");
            return null;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Port ต้องเป็นตัวเลข");
            return null;
        }

        return new OnlineRoomInfo("Manual Room", ip, port, 0, 3, "Unknown");
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

        JButton size1 = purpleButton("1920 x 1080", 120, 60);
        JButton size2 = purpleButton("1280 x 720", 120, 140);
        JButton size3 = purpleButton("960 x 540", 120, 220);
        JButton backBtn = purpleButton("Back", 120, 300);

        size1.setVisible(false); size2.setVisible(false); size3.setVisible(false); backBtn.setVisible(false);

        JButton close = new JButton("X");
        close.setBounds(440,10,50,50);
        close.setBackground(Color.BLACK); close.setForeground(Color.WHITE); close.setFocusPainted(false);
        close.addActionListener(e -> panel.setVisible(false));

        displayScale.addActionListener(e -> {
            save.setVisible(false); load.setVisible(false); displayScale.setVisible(false); exit.setVisible(false);
            size1.setVisible(true); size2.setVisible(true); size3.setVisible(true); backBtn.setVisible(true);
        });

        backBtn.addActionListener(e -> {
            size1.setVisible(false); size2.setVisible(false); size3.setVisible(false); backBtn.setVisible(false);
            save.setVisible(true); load.setVisible(true); displayScale.setVisible(true); exit.setVisible(true);
        });

        size1.addActionListener(e -> changeScreenSize(1920, 1080));
        size2.addActionListener(e -> changeScreenSize(1280, 720));
        size3.addActionListener(e -> changeScreenSize(960, 540));
        
        exit.addActionListener(e -> System.exit(0));

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