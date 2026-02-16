import javax.swing.*;
import java.awt.*;

public class gui {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ตัวอย่าง GUI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(320, 140);
            frame.setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            JButton button = new JButton("กดฉัน");
            JLabel label = new JLabel("ยังไม่ได้กด");

            button.addActionListener(e -> label.setText("ปุ่มถูกกดแล้ว!"));

            panel.add(button);
            panel.add(label);

            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}
