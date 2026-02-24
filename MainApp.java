import javax.swing.SwingUtilities;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> shop());
    }

    public static void shop() {
        UI_SHOP shopWindow = new UI_SHOP();
        shopWindow.createUI();
    }
}