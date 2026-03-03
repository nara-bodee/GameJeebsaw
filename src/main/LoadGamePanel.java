package main;

import save.GameSaveData;
import save.SaveManager;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class LoadGamePanel extends JPanel {
    
    private MainFrame mainFrame;
    private ImageIcon bgImage = new ImageIcon("ui/bg.jpg");
    private static final int MAX_SAVE_SLOTS = 5;
    
    public LoadGamePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));
        
        // ปุ่ม Back
        JButton backBtn = createStyledButton("Back", 200, 600,
            new Color(200, 100, 100), new Color(230, 130, 130));
        backBtn.addActionListener(e -> mainFrame.showMenu());
        add(backBtn);
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
        
        // หัวข้อ
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("TH Sarabun New", Font.BOLD, 48));
        g2.drawString("Load Save File", 300, 100);
        
        // แสดงรายชื่อเซฟ
        GameSaveData[] allSlots = SaveManager.getAllSaveSlotsInfo(MAX_SAVE_SLOTS);
        int startY = 150;
        
        for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
            GameSaveData data = allSlots[i];
            String slotText;
            
            if (data == null) {
                slotText = "Slot " + (i + 1) + " - (Empty)";
                g2.setColor(Color.GRAY);
            } else {
                slotText = "Slot " + (i + 1) + " - " + data.getChapterName() + " (" + data.getSaveDate() + ")";
                g2.setColor(Color.WHITE);
            }
            
            g2.setFont(new Font("TH Sarabun New", Font.PLAIN, 24));
            g2.drawString(slotText, 250, startY + (i * 80));
        }
    }
    
    public void updateSaveList() {
        // เมื่อกลับมาจากเกม ให้อัปเดตรายชื่อเซฟ
        revalidate();
        repaint();
    }
    
    public void loadSlot(int slotNumber) {
        if (SaveManager.hasSlot(slotNumber)) {
            mainFrame.loadGameFromSlot(slotNumber);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Slot " + slotNumber + " ว่างเปล่า", 
                "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        
        // เพิ่ม mouse listener เพื่อให้สามารถคลิกเลือกเซฟได้
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int y = e.getY();
                int startY = 150;
                
                for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
                    int slotY = startY + (i * 80);
                    if (y >= slotY && y < slotY + 60) {
                        loadSlot(i + 1);
                        return;
                    }
                }
            }
        });
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
