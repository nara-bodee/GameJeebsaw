
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class UI extends JFrame {

    JLayeredPane layeredPane = new JLayeredPane();

    JPanel startLayer;
    JPanel gameLayer;

    ImageIcon bgImage = new ImageIcon("bg.jpg");
    ImageIcon girlImage = new ImageIcon("girl.png");

    private JTextArea dialogueText;

    public UI() {

        setTitle("เกมจีบสาว");
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        layeredPane.setLayout(null);
        layeredPane.setBounds(0,0,1280,820);

        startLayer = createStartScene();
        gameLayer = createGameScene();

        layeredPane.add(startLayer, Integer.valueOf(0));
        layeredPane.add(gameLayer, Integer.valueOf(1));

        gameLayer.setVisible(false);

        add(layeredPane);
        setVisible(true);
    }

    // ================= START =================
    JPanel createStartScene() {

    JPanel p = new JPanel(null){
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            g.drawImage(bgImage.getImage(),0,0,1280,820,null);

            // ใส่ layer มืดทับภาพให้ดูเหมือนในรูป
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(20,30,70,180));
            g2.fillRect(0,0,getWidth(),getHeight());
        }
    };

    p.setBounds(0,0,1280,820);

    JButton start = pinkButton("Start game", 200, 350);
    start.addActionListener(e -> {
        startLayer.setVisible(false);
        gameLayer.setVisible(true);
    });

    JButton exit = pinkButton("Exit", 200, 430);
    exit.addActionListener(e -> System.exit(0));

    p.add(start);
    p.add(exit);

    return p;
}

    // ================= GAME =================
    JPanel createGameScene() {

        JPanel p = new JPanel(null){
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(bgImage.getImage(),0,0,1280,820,null);
            }
        };
        p.setBounds(0,0,1280,820);

        // TOP BAR
        JPanel topBar = new JPanel(null);
        topBar.setBounds(0, 0, 1280, 100);
        topBar.setOpaque(false);

        topBar.add(ovalLabel("📅 1", 40, 25));
        topBar.add(ovalLabel("⏰ 17.00", 200, 25));
        p.add(topBar);

        JLabel girl = new JLabel(girlImage);
        girl.setBounds(450,150,400,500);
        p.add(girl);

        JButton shop = circleButton("🛒", 50, 200);
        shop.addActionListener(e -> JOptionPane.showMessageDialog(UI.this, "ร้านค้ามีไอเทมให้ซื้อ!"));
        JButton menu = circleButton("≡", 1180, 20);

        p.add(shop);
        p.add(menu);

        int y = 150;
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0,0,new Color(255,120,160,220),
                        getWidth(),getHeight(),
                        new Color(120,100,255,220)
                );

                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),40,40);
            }
        };

        panel.setLayout(null);
        panel.setBounds(390,180,500,450);

        JButton save = purpleButton("Save game",120,120);
        save.addActionListener(e -> JOptionPane.showMessageDialog(UI.this, "บันทึกเกมแล้ว!"));
        JButton load = purpleButton("Load save",120,200);
        load.addActionListener(e -> JOptionPane.showMessageDialog(UI.this, "โหลดเซฟแล้ว!"));
        JButton exit = purpleButton("Exit",120,280);
        exit.addActionListener(e -> System.exit(0));

        JButton close = new JButton("X");
        close.setBounds(440,10,50,50);
        close.setBackground(Color.BLACK);
        close.setForeground(Color.WHITE);
        close.setFocusPainted(false);
        close.addActionListener(e-> panel.setVisible(false));

        panel.add(save);
        panel.add(load);
        panel.add(exit);
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
        nameLabel.setFont(new Font("Tahoma",Font.BOLD,18));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JTextArea dialogueText = new JTextArea(text);
        dialogueText.setBounds(40,60,780,80);
        dialogueText.setFont(new Font("Tahoma",Font.PLAIN,18));
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
        label.setFont(new Font("Tahoma",Font.BOLD,18));
        label.setOpaque(false);
        return label;
    }

    public static void main(String[] args) {
        new UI();
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
        setFont(new Font("Tahoma", Font.BOLD, 18));
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