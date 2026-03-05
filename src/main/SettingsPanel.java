package main;

import core.GameSettings;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class SettingsPanel extends JPanel {
    
    private MainFrame mainFrame;
    private ImageIcon bgImage = new ImageIcon("ui/bg.jpg");
    private List<JButton> buttons = new ArrayList<>();
    private JLabel titleLabel;
    
    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));
        
        // ปุ่ม Resolution 1920x1080
        JButton res1Btn = createStyledButton("1920 x 1080",
            new Color(100, 200, 100), new Color(130, 230, 130));
        res1Btn.addActionListener(e -> changeResolution(1920, 1080));
        buttons.add(res1Btn);
        add(res1Btn);
        
        // ปุ่ม Resolution 1280x720
        JButton res2Btn = createStyledButton("1280 x 720",
            new Color(150, 180, 200), new Color(180, 210, 230));
        res2Btn.addActionListener(e -> changeResolution(1280, 720));
        buttons.add(res2Btn);
        add(res2Btn);

        // ปุ่ม Resolution 960x540
        JButton res3Btn = createStyledButton("960 x 540",
            new Color(200, 150, 200), new Color(230, 180, 230));
        res3Btn.addActionListener(e -> changeResolution(960, 540));
        buttons.add(res3Btn);
        add(res3Btn);
        
        // ปุ่ม Back to Menu
        JButton backBtn = createStyledButton("Back to Menu",
            new Color(255, 180, 100), new Color(255, 210, 130));
        backBtn.addActionListener(e -> mainFrame.showMenu());
        buttons.add(backBtn);
        add(backBtn);
        
        // Title
        titleLabel = new JLabel("Display Settings", SwingConstants.CENTER); // ตั้งค่าให้ข้อความอยู่ตรงกลาง Label
        titleLabel.setFont(new Font("TH Sarabun New", Font.BOLD, 48)); // เพิ่มขนาดฟอนต์นิดหน่อยให้ดูชัดขึ้น
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel);

        // ดักจับการเปลี่ยนขนาด
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionComponents();
            }
        });
    }

    private void repositionComponents() {
        int width = getWidth();
        int height = getHeight();
        int buttonWidth = 250;
        int buttonHeight = 60;
        int gap = 20;

        int totalHeight = (buttonHeight * buttons.size()) + (gap * (buttons.size() - 1));
        
        // คำนวณจุดให้กลุ่มปุ่มอยู่ต่ำกว่าตรงกลางนิดหน่อย เพื่อเผื่อที่ให้ Title
        int startY = (height - totalHeight) / 2 + 30; 
        int startX = (width - buttonWidth) / 2;

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setBounds(startX, startY + (i * (buttonHeight + gap)), buttonWidth, buttonHeight);
        }

        // จัดตำแหน่ง Title Label ให้อยู่ตรงกลางบนปุ่ม
        int titleWidth = 400;
        int titleHeight = 60;
        titleLabel.setBounds((width - titleWidth) / 2, startY - titleHeight - 30, titleWidth, titleHeight);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bgImage.getImage(), 0, 0, getWidth(), getHeight(), null);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(20, 30, 70, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
    
    private void changeResolution(int width, int height) {
        GameSettings.getInstance().applyResolution(width, height, false);
        mainFrame.updateWindowSize();
        JOptionPane.showMessageDialog(this, "Resolution changed to " + width + " x " + height);
    }
    
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