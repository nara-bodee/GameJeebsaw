package main;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class MenuPanel extends JPanel {
    
    private MainFrame mainFrame;
    private ImageIcon bgImage = new ImageIcon("ui/bg.jpg");
    
    public MenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));
        
        // ปุ่ม New Game
        JButton newGameBtn = createStyledButton("New Game", 200, 280, 
            new Color(100, 200, 100), new Color(130, 230, 130));
        newGameBtn.addActionListener(e -> mainFrame.startNewGame());
        add(newGameBtn);
        
        // ปุ่ม Load Game
        JButton loadGameBtn = createStyledButton("Load Game", 200, 360,
            new Color(150, 180, 200), new Color(180, 210, 230));
        loadGameBtn.addActionListener(e -> mainFrame.showLoadGame());
        add(loadGameBtn);

        // ปุ่ม Multiplayer
        JButton multiplayerBtn = createStyledButton("Multiplayer", 200, 440,
            new Color(180, 120, 255), new Color(210, 150, 255));
        multiplayerBtn.addActionListener(e -> mainFrame.openMultiplayer());
        add(multiplayerBtn);
        
        // ปุ่ม Exit
        JButton exitBtn = createStyledButton("Exit", 200, 520,
            new Color(255, 120, 160), new Color(255, 150, 180));
        exitBtn.addActionListener(e -> System.exit(0));
        add(exitBtn);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // วาดพื้นหลัง
        g.drawImage(bgImage.getImage(), 0, 0, getWidth(), getHeight(), null);
        
        // overlay สีเข้ม
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(20, 30, 70, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
    
    private JButton createStyledButton(String text, int x, int y, Color normal, Color hover) {
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(
                    0, 0,
                    isHover ? hover.brighter() : normal,
                    getWidth(), getHeight(),
                    isHover ? hover : normal.darker()
                );
                
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 30, 30);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setBounds(x, y, 250, 60);
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
