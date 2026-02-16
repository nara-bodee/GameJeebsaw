import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main_game {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			GameContext context = new GameContext();
			SceneManager sceneManager = new SceneManager(context);

			sceneManager.register("boot", new BootScene());
			sceneManager.register("placeholder", new PlaceholderScene());
			sceneManager.start("boot");
		});
	}
}

final class GameContext {
	private final JFrame frame;

	GameContext() {
		frame = new JFrame("GameJeebsaw Runner");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(960, 540);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());
		frame.setVisible(true);
	}

	JFrame getFrame() {
		return frame;
	}
}

interface Scene {
	String id();
	JPanel build(GameContext context, SceneManager manager);
	default void onShow(GameContext context, SceneManager manager) {}
	default void onHide(GameContext context, SceneManager manager) {}
}

final class SceneManager {
	private final GameContext context;
	private final Map<String, Scene> scenes;
	private Scene currentScene;
	private JPanel currentPanel;

	SceneManager(GameContext context) {
		this.context = context;
		this.scenes = new LinkedHashMap<>();
	}

	void register(String id, Scene scene) {
		scenes.put(id, scene);
	}

	void start(String id) {
		switchTo(id);
	}

	void switchTo(String id) {
		Scene next = scenes.get(id);
		if (next == null) {
			throw new IllegalArgumentException("Scene not found: " + id);
		}

		if (currentScene != null) {
			currentScene.onHide(context, this);
		}

		JFrame frame = context.getFrame();
		if (currentPanel != null) {
			frame.remove(currentPanel);
		}

		JPanel panel = next.build(context, this);
		frame.add(panel, BorderLayout.CENTER);
		frame.revalidate();
		frame.repaint();

		currentScene = next;
		currentPanel = panel;
		next.onShow(context, this);
	}
}

final class BootScene implements Scene {
	@Override
	public String id() {
		return "boot";
	}

	@Override
	public JPanel build(GameContext context, SceneManager manager) {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Booting...", SwingConstants.CENTER);
		panel.add(label, BorderLayout.CENTER);
		return panel;
	}

	@Override
	public void onShow(GameContext context, SceneManager manager) {
		Timer timer = new Timer(400, e -> {
			((Timer) e.getSource()).stop();
			manager.switchTo("placeholder");
		});
		timer.setRepeats(false);
		timer.start();
	}
}

final class PlaceholderScene implements Scene {
	@Override
	public String id() {
		return "placeholder";
	}

	@Override
	public JPanel build(GameContext context, SceneManager manager) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JLabel title = new JLabel("Game Runner");
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel hint = new JLabel("Add story, UI, and character assets here.");
		hint.setAlignmentX(Component.CENTER_ALIGNMENT);

		JButton startButton = new JButton("Start");
		startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		startButton.addActionListener(e -> JOptionPane.showMessageDialog(
				context.getFrame(),
				"Hook your game logic to this action.",
				"Placeholder",
				JOptionPane.INFORMATION_MESSAGE
		));

		panel.add(Box.createVerticalStrut(40));
		panel.add(title);
		panel.add(Box.createVerticalStrut(12));
		panel.add(hint);
		panel.add(Box.createVerticalStrut(24));
		panel.add(startButton);

		return panel;
	}
}
