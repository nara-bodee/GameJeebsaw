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
    
    private MenuPanel menuPanel;
    private LoadGamePanel loadGamePanel;
    private GamePanel gamePanel;
    private ShopPanel shopPanel;
    private UI multiplayerUi;
    
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
        
        // เพิ่ม panels เข้า CardLayout
        mainContainer.add(menuPanel, MENU_PANEL);
        mainContainer.add(loadGamePanel, LOAD_GAME_PANEL);
        mainContainer.add(gamePanel, GAME_PANEL);
        mainContainer.add(shopPanel, SHOP_PANEL);
        
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
        if (multiplayerUi == null || !multiplayerUi.isDisplayable()) {
            multiplayerUi = new UI(null, false, true);
        }
        multiplayerUi.openOnlineMenu();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
