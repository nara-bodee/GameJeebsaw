import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UI_GAME extends JFrame {

    private Image backgroundImage;     // ใส่ BG ในอนาคต
    private Image characterImage;      // ใส่ตัวละครในอนาคต

    private JLabel dialogueText;
    private JLabel dayLabel;
    private JLabel timeLabel;

    public UI_GAME() {
        setTitle("Dating Game - Gameplay");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        add(layeredPane);

        // ===== Background Panel (Placeholder) =====
        JPanel backgroundPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // ===== ใส่ BG ภายหลัง =====
                // if (backgroundImage != null) {
                //     g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                // }

                g.setColor(new Color(240,240,240));
                g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        backgroundPanel.setBounds(0,0,1000,650);
        layeredPane.add(backgroundPanel, Integer.valueOf(0));

        // ===== Top UI =====
        JPanel topPanel = new JPanel(null);
        topPanel.setOpaque(false);
        topPanel.setBounds(0,0,1000,80);

        // Hearts
        for(int i=0;i<10;i++){
            JLabel heart = new JLabel("❤");
            heart.setForeground(Color.RED);
            heart.setFont(new Font("SansSerif", Font.PLAIN, 22));
            heart.setBounds(20 + (i*30), 20, 30, 30);
            topPanel.add(heart);
        }

        // Day Box
        dayLabel = createInfoBox("Day 1");
        dayLabel.setBounds(650, 15, 120, 40);

        // Time Box
        timeLabel = createInfoBox("17:00");
        timeLabel.setBounds(790, 15, 120, 40);

        // Menu Button
        JButton menuBtn = new JButton("≡");
        menuBtn.setBounds(930, 15, 50, 40);
        topPanel.add(menuBtn);

        topPanel.add(dayLabel);
        topPanel.add(timeLabel);
        layeredPane.add(topPanel, Integer.valueOf(2));

        // ===== Choice Buttons =====
        JButton choice1 = createChoiceButton("ตัวเลือกที่ 1");
        JButton choice2 = createChoiceButton("ตัวเลือกที่ 2");

        choice1.setBounds(300, 220, 400, 50);
        choice2.setBounds(300, 290, 400, 50);

        layeredPane.add(choice1, Integer.valueOf(3));
        layeredPane.add(choice2, Integer.valueOf(3));

        // ===== Dialogue Box =====
        JPanel dialoguePanel = new JPanel(null);
        dialoguePanel.setBackground(new Color(255,255,255,200));
        dialoguePanel.setBounds(100, 430, 800, 150);

        dialogueText = new JLabel("เราคือ......");
        dialogueText.setFont(new Font("Tahoma", Font.PLAIN, 20));
        dialogueText.setBounds(30, 40, 700, 50);

        dialoguePanel.add(dialogueText);
        layeredPane.add(dialoguePanel, Integer.valueOf(4));

        setVisible(true);
    }

    // ===== กล่อง Day / Time =====
    private JLabel createInfoBox(String text){
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(255,182,193));
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return label;
    }

    // ===== ปุ่มตัวเลือก =====
    private JButton createChoiceButton(String text){
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setBackground(new Color(100,120,255,180));
        btn.setFocusPainted(false);
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UI_GAME::new);
    }
}