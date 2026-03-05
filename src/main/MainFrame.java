package main;

import core.GameSettings;
import java.awt.*;
import javax.swing.*;
import ui.UI;

public class MainFrame extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    // ชื่อการ์ดต่างๆ
    public static final String MENU_PANEL = "MENU";
    public static final String LOAD_GAME_PANEL = "LOAD_GAME";
    public static final String GAME_PANEL = "GAME";
    public static final String SHOP_PANEL = "SHOP";
    public static final String SETTINGS_PANEL = "SETTINGS";
    
    private MenuPanel menuPanel;
    private LoadGamePanel loadGamePanel;
    private GamePanel gamePanel;
    private ShopPanel shopPanel;
    private SettingsPanel settingsPanel;
    
    public MainFrame() {
        setTitle("เกมจีบสาว 7 Days");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GameSettings settings = GameSettings.getInstance();
        int width = settings.getScreenWidth();
        int height = settings.getScreenHeight();
        
        // สร้าง CardLayout และ container
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        // สร้าง panels ต่างๆ
        menuPanel = new MenuPanel(this);
        loadGamePanel = new LoadGamePanel(this);
        gamePanel = new GamePanel(this);
        shopPanel = new ShopPanel(this);
        settingsPanel = new SettingsPanel(this);
        
        // เพิ่ม panels เข้า CardLayout
        mainContainer.add(menuPanel, MENU_PANEL);
        mainContainer.add(loadGamePanel, LOAD_GAME_PANEL);
        mainContainer.add(gamePanel, GAME_PANEL);
        mainContainer.add(shopPanel, SHOP_PANEL);
        mainContainer.add(settingsPanel, SETTINGS_PANEL);
        
        add(mainContainer);
        
        setSize(width, height);
        setLocationRelativeTo(null);
        setVisible(true);
        
        // แสดงหน้า menu ก่อน
        showMenu();
    }
    
    public void showMenu() {
        cardLayout.show(mainContainer, MENU_PANEL);
    }
    
    public void showLoadGame() {
        loadGamePanel.updateSaveList();
        cardLayout.show(mainContainer, LOAD_GAME_PANEL);
    }
    
    public void loadGameFromSlot(int slotNumber) {
        gamePanel.loadExistingSaveFromSlot(slotNumber);
        cardLayout.show(mainContainer, GAME_PANEL);
    }
    
    public void startNewGame() {
        gamePanel.startNewGame();
        cardLayout.show(mainContainer, GAME_PANEL);
    }
    
    public void showGame() {
        gamePanel.initializeGame();
        cardLayout.show(mainContainer, GAME_PANEL);
    }
    
    public void showShop(String eventId) {
        shopPanel.refreshShop(gamePanel.getPlayer(), eventId);
        cardLayout.show(mainContainer, SHOP_PANEL);
    }
    
    public void returnToGame() {
        cardLayout.show(mainContainer, GAME_PANEL);
    }

    public void openMultiplayer() {
        // ซ่อน MainFrame เพื่อให้เห็นเฉพาะ Multiplayer window
        //this.setVisible(false);
        
        // แสดง UI window เต็มหน้าจอ
        // Constructor จะดำเนินการใส่ชื่อผู้เล่นและแสดง Host/Join menu โดยอัตโนมัติ
        UI onlineUI = new UI(null, false, true, this);
    }

    public void showSettings() {
        cardLayout.show(mainContainer, SETTINGS_PANEL);
    }
    
    public void updateWindowSize() {
        GameSettings settings = GameSettings.getInstance();
        setSize(settings.getScreenWidth(), settings.getScreenHeight());
        setLocationRelativeTo(null);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
