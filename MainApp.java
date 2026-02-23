import javax.swing.SwingUtilities;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> shop());
    }

    public static void shop() {
        Shop shopWindow = new Shop();
        shopWindow.createUI();
    }
}