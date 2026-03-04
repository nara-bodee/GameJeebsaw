package main;

import core.GameSettings;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class SettingsPanel extends JPanel {
    
    private MainFrame mainFrame;
    private ImageIcon bgImage = new ImageIcon("ui/bg.jpg");
    
    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));
        
        // ปุ่ม Resolution 1920x1080
        JButton res1Btn = createStyledButton("1920 x 1080", 200, 280,
            new Color(100, 200, 100), new Color(130, 230, 130));
        res1Btn.addActionListener(e -> changeResolution(1920, 1080));
        add(res1Btn);
        
        // ปุ่ม Resolution 1280x720
        JButton res2Btn = createStyledButton("1280 x 720", 200, 360,
            new Color(150, 180, 200), new Color(180, 210, 230));
        res2Btn.addActionListener(e -> changeResolution(1280, 720));
        add(res2Btn);

        // ปุ่ม Resolution 960x540
        JButton res3Btn = createStyledButton("960 x 540", 200, 440,
            new Color(200, 150, 200), new Color(230, 180, 230));
        res3Btn.addActionListener(e -> changeResolution(960, 540));
        add(res3Btn);
        
        // ปุ่ม Back to Menu
        JButton backBtn = createStyledButton("Back to Menu", 200, 520,
            new Color(255, 180, 100), new Color(255, 210, 130));
        backBtn.addActionListener(e -> mainFrame.showMenu());
        add(backBtn);
        
        // Title
        JLabel titleLabel = new JLabel("Display Settings");
        titleLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(400, 150, 480, 60);
        add(titleLabel);
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
    
    private void changeResolution(int width, int height) {
        GameSettings.getInstance().applyResolution(width, height, false);
        mainFrame.updateWindowSize();
        JOptionPane.showMessageDialog(this, "Resolution changed to " + width + " x " + height);
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
