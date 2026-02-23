import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;

public class UI extends JFrame {

    CardLayout card = new CardLayout();
    JPanel root = new JPanel(card);

    Font italianno;
    Font sarabun;
    Font sarabunBold;

    public UI() {
        loadFonts();

        setTitle("Dating Game UI");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        root.add(startPage(), "start");
        root.add(gamePage(), "game");
        root.add(pauseOverlay(), "pause");
        root.add(savePage(), "save");
        root.add(shopPage(), "shop");

        add(root);
        card.show(root, "start");
        setVisible(true);
    }

    // ================= LOAD FONT =================
    void loadFonts() {
        try {
            italianno = Font.createFont(Font.TRUETYPE_FONT, new File("Italianno-Regular.ttf")).deriveFont(36f);
            sarabun = Font.createFont(Font.TRUETYPE_FONT, new File("Sarabun-Regular.ttf")).deriveFont(20f);
            sarabunBold = Font.createFont(Font.TRUETYPE_FONT, new File("Sarabun-Bold.ttf")).deriveFont(22f);
        } catch (Exception e) {
            italianno = new Font("Serif", Font.PLAIN, 36);
            sarabun = new Font("SansSerif", Font.PLAIN, 20);
            sarabunBold = new Font("SansSerif", Font.BOLD, 22);
        }
    }

    // ================= START PAGE =================
    JPanel startPage() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(35,45,95));

        panel.add(menuButton("Start game", 420, 350, e->card.show(root,"game")));
        panel.add(menuButton("Load save", 420, 420, e->card.show(root,"save")));
        panel.add(menuButton("Exit", 420, 490, e->System.exit(0)));

        return panel;
    }

    // ================= GAME PAGE =================
    JPanel gamePage() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(60,70,120));

        // Top Bar
        RoundedPanel top = new RoundedPanel(50,new Color(255,120,160,230));
        top.setBounds(40,20,1120,70);
        top.setLayout(null);

        JLabel hearts = new JLabel("❤ ❤ ❤ ❤ ❤ ❤ ❤ ❤ ❤ ❤");
        hearts.setFont(sarabunBold);
        hearts.setForeground(Color.RED);
        hearts.setBounds(30,15,300,40);

        JLabel day = new JLabel("1");
        day.setFont(sarabunBold);
        day.setForeground(Color.WHITE);
        day.setBounds(550,15,100,40);

        JLabel time = new JLabel("17.00");
        time.setFont(sarabunBold);
        time.setForeground(Color.WHITE);
        time.setBounds(1000,15,100,40);

        GradientButton menu = smallMenuButton();
        menu.setBounds(1060,15,50,40);
        menu.addActionListener(e->card.show(root,"pause"));

        top.add(hearts); top.add(day); top.add(time); top.add(menu);

        // Choices
        panel.add(choiceButton("ตัวเลือกที่ 1",450,300));
        panel.add(choiceButton("ตัวเลือกที่ 2",450,370));

        // Dialogue Box
        RoundedPanel dialogue = new RoundedPanel(60,new Color(255,255,255,220));
        dialogue.setBounds(200,560,800,170);
        dialogue.setLayout(null);

        JLabel text = new JLabel("เราดีใจ ......");
        text.setFont(sarabun);
        text.setBounds(40,60,700,40);

        dialogue.add(text);

        panel.add(top);
        panel.add(dialogue);

        return panel;
    }

    // ================= PAUSE =================
    JPanel pauseOverlay() {
    JPanel overlay = new JPanel(null) {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0, 0, 0, 170));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    };

    int boxWidth = 420;
    int boxHeight = 460;

    RoundedPanel box = new RoundedPanel(80, new Color(230, 90, 130, 240));
    box.setBounds(
            (1200 - boxWidth) / 2,
            (800 - boxHeight) / 2,
            boxWidth,
            boxHeight
    );
    box.setLayout(null);

    int btnWidth = 280;
    int btnHeight = 65;
    int startY = 140;
    int gap = 90;

    GradientButton save = new GradientButton("Save game");
    save.setFont(italianno.deriveFont(40f));
    save.setBounds((boxWidth - btnWidth) / 2, startY, btnWidth, btnHeight);

    GradientButton load = new GradientButton("Load save");
    load.setFont(italianno.deriveFont(40f));
    load.setBounds((boxWidth - btnWidth) / 2, startY + gap, btnWidth, btnHeight);

    GradientButton exit = new GradientButton("Exit");
    exit.setFont(italianno.deriveFont(40f));
    exit.setBounds((boxWidth - btnWidth) / 2, startY + gap * 2, btnWidth, btnHeight);

    JButton close = new JButton("✕");
    close.setFont(new Font("Arial", Font.BOLD, 18));
    close.setForeground(Color.WHITE);
    close.setBackground(new Color(40, 40, 40));
    close.setFocusPainted(false);
    close.setBounds(boxWidth - 55, 20, 35, 35);
    close.setBorder(BorderFactory.createEmptyBorder());
    close.addActionListener(e -> card.show(root, "game"));

    box.add(save);
    box.add(load);
    box.add(exit);
    box.add(close);

    overlay.add(box);
    return overlay;
}

    // ================= SAVE =================
    JPanel savePage(){
        JPanel panel = blurPanel();
        RoundedPanel box = popupBox(panel);

        box.add(choiceButton("17.00 / 22 / 2 / 2565",100,120));
        box.add(choiceButton("18.00 / 21 / 1 / 2565",100,190));
        box.add(choiceButton("20.00 / 25 / 31 / 2564",100,260));

        return panel;
    }

    // ================= SHOP =================
    JPanel shopPage(){
        JPanel panel = blurPanel();
        RoundedPanel box = popupBox(panel);

        int x=100,y=120;
        for(int i=0;i<6;i++){
            GradientButton item = new GradientButton("150  buy");
            item.setBounds(x,y,180,100);
            box.add(item);
            x+=220;
            if(i==2){ x=100; y+=140; }
        }
        return panel;
    }

    // ================= HELPERS =================

    GradientButton menuButton(String text,int x,int y,ActionListener act){
        GradientButton b = new GradientButton(text);
        b.setFont(italianno);
        b.setBounds(x,y,350,60);
        b.addActionListener(act);
        return b;
    }

    GradientButton choiceButton(String text,int x,int y){
        GradientButton b = new GradientButton(text);
        b.setFont(sarabunBold);
        b.setBounds(x,y,300,55);
        return b;
    }

    GradientButton smallMenuButton(){
        GradientButton b = new GradientButton("≡");
        b.setFont(sarabunBold);
        return b;
    }

    JPanel blurPanel(){
        JPanel panel = new JPanel(null){
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                g.setColor(new Color(0,0,0,120));
                g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        return panel;
    }

    RoundedPanel popupBox(JPanel panel){
        RoundedPanel box = new RoundedPanel(60,new Color(255,100,150,220));
        box.setBounds(300,150,600,500);
        box.setLayout(null);
        panel.add(box);

        JButton close = new JButton("X");
        close.setBounds(520,20,50,40);
        close.addActionListener(e->card.show(root,"game"));
        box.add(close);

        return box;
    }

    // ================= CUSTOM COMPONENTS =================

    class RoundedPanel extends JPanel{
        int radius; Color bg;
        RoundedPanel(int r,Color c){radius=r;bg=c;setOpaque(false);}
        protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),radius,radius);
        }
    }

    class GradientButton extends JButton{
        boolean hover=false;
        GradientButton(String text){
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hover=true;repaint();}
                public void mouseExited(MouseEvent e){hover=false;repaint();}
            });
        }
        protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = hover ?
                    new GradientPaint(0,0,new Color(170,120,255),
                            0,getHeight(),new Color(255,160,220)) :
                    new GradientPaint(0,0,new Color(120,90,255),
                            0,getHeight(),new Color(255,120,200));
            g2.setPaint(gp);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),50,50);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args){
        new UI();
    }
}