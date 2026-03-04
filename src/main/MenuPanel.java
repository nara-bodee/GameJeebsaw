package main;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class MenuPanel extends JPanel {
    
    private MainFrame mainFrame;
    private ImageIcon bgImage = new ImageIcon("ui/bg.jpg");
    // เก็บรายการปุ่มไว้เพื่อจัดตำแหน่งใหม่
    private List<JButton> buttons = new ArrayList<>();
    
    public MenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null); // ยังคงใช้ null layout
        setPreferredSize(new Dimension(1280, 720));
        
        // ปุ่ม Single Player
        JButton newGameBtn = createStyledButton("Single Player", 
            new Color(100, 200, 100), new Color(130, 230, 130));
        newGameBtn.addActionListener(e -> mainFrame.startNewGame());
        buttons.add(newGameBtn);
        add(newGameBtn);
        
        // ปุ่ม Load Game
        JButton loadGameBtn = createStyledButton("Load Game",
            new Color(150, 180, 200), new Color(180, 210, 230));
        loadGameBtn.addActionListener(e -> mainFrame.showLoadGame());
        buttons.add(loadGameBtn);
        add(loadGameBtn);

        // ปุ่ม Multiplayer
        JButton multiplayerBtn = createStyledButton("Multiplayer",
            new Color(180, 120, 255), new Color(210, 150, 255));
        multiplayerBtn.addActionListener(e -> mainFrame.openMultiplayer());
        buttons.add(multiplayerBtn);
        add(multiplayerBtn);

        // ปุ่ม Settings
        JButton settingsBtn = createStyledButton("Settings",
            new Color(255, 180, 100), new Color(255, 210, 130));
        settingsBtn.addActionListener(e -> mainFrame.showSettings());
        buttons.add(settingsBtn);
        add(settingsBtn);
        
        // ปุ่ม Exit
        JButton exitBtn = createStyledButton("Exit",
            new Color(255, 120, 160), new Color(255, 150, 180));
        exitBtn.addActionListener(e -> System.exit(0));
        buttons.add(exitBtn);
        add(exitBtn);

        // ดักจับการเปลี่ยนขนาดหน้าจอเพื่อจัดตำแหน่งปุ่มใหม่
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionComponents();
            }
        });
    }

    // เมธอดสำหรับจัดตำแหน่งปุ่มให้อยู่ตรงกลางเสมอ
    private void repositionComponents() {
        int width = getWidth();
        int height = getHeight();
        int buttonWidth = 250;
        int buttonHeight = 60;
        int gap = 20; // ระยะห่างระหว่างปุ่ม

        int totalHeight = (buttonHeight * buttons.size()) + (gap * (buttons.size() - 1));
        
        // คำนวณจุดเริ่มต้น y เพื่อให้กลุ่มปุ่มอยู่ตรงกลางจอ
        int startY = (height - totalHeight) / 2;
        int startX = (width - buttonWidth) / 2;

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setBounds(startX, startY + (i * (buttonHeight + gap)), buttonWidth, buttonHeight);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // วาดพื้นหลัง (ภาพจะขยายตามขนาด panel)
        g.drawImage(bgImage.getImage(), 0, 0, getWidth(), getHeight(), null);
        
        // overlay สีเข้ม
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(20, 30, 70, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
    
    // เอาพารามิเตอร์ x, y ออก
    private JButton createStyledButton(String text, Color normal, Color hover) {
        JButton button = new JButton(text) {
            private boolean isHover = false;
            
            {
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
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(
                    0, 0, isHover ? hover.brighter() : normal,
                    getWidth(), getHeight(), isHover ? hover : normal.darker()
                );
                
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 30, 30);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        // เราตั้งแค่ขนาดเริ่มต้น ส่วนตำแหน่งจะถูกจัดโดย repositionComponents
        button.setBounds(0, 0, 250, 60);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("TH Sarabun New", Font.BOLD, 22));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
}