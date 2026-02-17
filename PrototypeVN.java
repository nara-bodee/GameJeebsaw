import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Properties;

/**
 * PrototypeVN (Swing)
 * - Dialogue data structure
 * - Choice system + branching
 * - Dialogue renderer (Swing GUI)
 * - Save/Load structure (simple .sav text)
 *
 * Compile:
 *   javac PrototypeVN.java
 * Run:
 *   java PrototypeVN
 */
public class PrototypeVN {

    // -----------------------------
    // 1) DATA STRUCTURE
    // -----------------------------
    static class Choice {
        final String text;
        final String nextNodeId;
        final Condition condition; // optional
        final Effect effect;       // optional

        Choice(String text, String nextNodeId) {
            this(text, nextNodeId, null, null);
        }

        Choice(String text, String nextNodeId, Condition condition, Effect effect) {
            this.text = text;
            this.nextNodeId = nextNodeId;
            this.condition = condition;
            this.effect = effect;
        }
    }

    static class DialogueNode {
        final String id;
        final String speaker;
        final List<String> lines = new ArrayList<>();
        final List<Choice> choices = new ArrayList<>();
        String autoNextNodeId = null;
        boolean isEnd = false;

        DialogueNode(String id, String speaker) {
            this.id = id;
            this.speaker = speaker;
        }

        DialogueNode addLine(String line) {
            lines.add(line);
            return this;
        }

        DialogueNode addChoice(Choice c) {
            choices.add(c);
            return this;
        }

        DialogueNode setAutoNext(String nextId) {
            this.autoNextNodeId = nextId;
            return this;
        }

        DialogueNode setEnd(boolean end) {
            this.isEnd = end;
            return this;
        }
    }

    // -----------------------------
    // 2) GAME STATE + RULES
    // -----------------------------
    static class GameState {
        int affection = 0;
        String playerName = "Player";
        final Map<String, Boolean> flags = new HashMap<>();

        boolean flag(String key) {
            return flags.getOrDefault(key, false);
        }

        void setFlag(String key, boolean value) {
            flags.put(key, value);
        }
    }

    interface Condition {
        boolean test(GameState s);
    }

    interface Effect {
        void apply(GameState s);
    }

    // -----------------------------
    // 3) DIALOGUE MANAGER
    // -----------------------------
    static class DialogueManager {
        private final Map<String, DialogueNode> nodes = new HashMap<>();
        private final GameState state = new GameState();

        private String currentNodeId;
        private boolean running = true;

        // history (debug / future backtracking)
        private final List<String> visited = new ArrayList<>();

        public GameState getState() { return state; }

        public void addNode(DialogueNode node) {
            nodes.put(node.id, node);
        }

        public void start(String startNodeId) {
            if (!nodes.containsKey(startNodeId)) {
                throw new IllegalArgumentException("Start node not found: " + startNodeId);
            }
            currentNodeId = startNodeId;
            running = true;
        }

        public boolean isRunning() { return running; }

        public void stop() { running = false; }

        public DialogueNode getCurrentNode() {
            return nodes.get(currentNodeId);
        }

        public List<Choice> getAvailableChoices(DialogueNode node) {
            List<Choice> out = new ArrayList<>();
            for (Choice c : node.choices) {
                if (c.condition == null || c.condition.test(state)) {
                    out.add(c);
                }
            }
            return out;
        }

        public void choose(int choiceIndex, List<Choice> availableChoices) {
            if (!running) return;
            if (choiceIndex < 0 || choiceIndex >= availableChoices.size()) return;

            Choice picked = availableChoices.get(choiceIndex);
            if (picked.effect != null) picked.effect.apply(state);

            visited.add(currentNodeId);
            goTo(picked.nextNodeId);
        }

        public void nextAuto(DialogueNode node) {
            if (!running) return;

            if (node.isEnd) {
                running = false;
                return;
            }

            if (node.autoNextNodeId != null) {
                visited.add(currentNodeId);
                goTo(node.autoNextNodeId);
            } else {
                running = false;
            }
        }

        private void goTo(String nodeId) {
            if (!nodes.containsKey(nodeId)) {
                System.out.println("[ERROR] Missing node: " + nodeId);
                running = false;
                return;
            }
            currentNodeId = nodeId;
        }

        // -----------------------------
        // 4) SAVE / LOAD (simple)
        // -----------------------------
        public void saveToFile(File file) throws IOException {
            Properties p = new Properties();
            p.setProperty("currentNodeId", currentNodeId == null ? "" : currentNodeId);
            p.setProperty("playerName", state.playerName);
            p.setProperty("affection", String.valueOf(state.affection));

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Boolean> e : state.flags.entrySet()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(e.getKey()).append(":").append(e.getValue());
            }
            p.setProperty("flags", sb.toString());

            try (OutputStream os = new FileOutputStream(file)) {
                p.store(new OutputStreamWriter(os, StandardCharsets.UTF_8), "PrototypeVN Save");
            }
        }

        public void loadFromFile(File file) throws IOException {
            Properties p = new Properties();
            try (InputStream is = new FileInputStream(file)) {
                p.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }

            String loadedNode = p.getProperty("currentNodeId", "");
            state.playerName = p.getProperty("playerName", "Player");
            state.affection = Integer.parseInt(p.getProperty("affection", "0"));

            state.flags.clear();
            String flagsStr = p.getProperty("flags", "");
            if (!flagsStr.trim().isEmpty()) {
                String[] parts = flagsStr.split(",");
                for (String part : parts) {
                    String[] kv = part.split(":");
                    if (kv.length == 2) {
                        state.flags.put(kv[0], Boolean.parseBoolean(kv[1]));
                    }
                }
            }

            if (nodes.containsKey(loadedNode)) {
                currentNodeId = loadedNode;
                running = true;
            } else {
                throw new IOException("Save references missing node: " + loadedNode);
            }
        }
    }

    // -----------------------------
    // 5) RENDERER (Swing GUI)
    // -----------------------------
    static class SwingRenderer {
        private final DialogueManager dm;

        private JFrame frame;
        private JTextArea dialogueArea;
        private JPanel choicesPanel;
        private JButton nextButton;

        private final JFileChooser fileChooser = new JFileChooser();

        SwingRenderer(DialogueManager dm) {
            this.dm = dm;
        }

        public void show() {
            SwingUtilities.invokeLater(() -> {
                buildUI();
                render();
                frame.setVisible(true);
            });
        }

        private void buildUI() {
            frame = new JFrame("PrototypeVN (Swing)");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(820, 520);
            frame.setLocationRelativeTo(null);

            JPanel root = new JPanel(new BorderLayout(12, 12));
            root.setBorder(new EmptyBorder(12, 12, 12, 12));
            frame.setContentPane(root);

            // Top toolbar
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);

            JButton btnSetName = new JButton("Set Name");
            JButton btnState = new JButton("Show State");
            JButton btnSave = new JButton("Save...");
            JButton btnLoad = new JButton("Load...");
            JButton btnQuit = new JButton("Quit");

            btnSetName.addActionListener(e -> onSetName());
            btnState.addActionListener(e -> onShowState());
            btnSave.addActionListener(e -> onSave());
            btnLoad.addActionListener(e -> onLoad());
            btnQuit.addActionListener(e -> System.exit(0));

            toolbar.add(btnSetName);
            toolbar.add(btnState);
            toolbar.addSeparator();
            toolbar.add(btnSave);
            toolbar.add(btnLoad);
            toolbar.addSeparator();
            toolbar.add(btnQuit);

            root.add(toolbar, BorderLayout.NORTH);

            // Center dialogue box
            dialogueArea = new JTextArea();
            dialogueArea.setEditable(false);
            dialogueArea.setLineWrap(true);
            dialogueArea.setWrapStyleWord(true);
            dialogueArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            dialogueArea.setMargin(new Insets(10, 10, 10, 10));

            JScrollPane scroll = new JScrollPane(dialogueArea);
            root.add(scroll, BorderLayout.CENTER);

            // Bottom: choices + next
            JPanel bottom = new JPanel(new BorderLayout(8, 8));

            choicesPanel = new JPanel();
            choicesPanel.setLayout(new BoxLayout(choicesPanel, BoxLayout.Y_AXIS));

            JScrollPane choicesScroll = new JScrollPane(choicesPanel);
            choicesScroll.setPreferredSize(new Dimension(100, 160));
            bottom.add(choicesScroll, BorderLayout.CENTER);

            nextButton = new JButton("Next");
            nextButton.addActionListener(e -> {
                DialogueNode node = dm.getCurrentNode();
                dm.nextAuto(node);
                render();
            });

            JPanel nextWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            nextWrap.add(nextButton);
            bottom.add(nextWrap, BorderLayout.SOUTH);

            root.add(bottom, BorderLayout.SOUTH);
        }

        private void render() {
            if (!dm.isRunning()) {
                dialogueArea.setText("The story has ended.\n\nYou may load a save, or quit.");
                clearChoices();
                nextButton.setEnabled(false);
                return;
            }

            DialogueNode node = dm.getCurrentNode();
            if (node == null) {
                dialogueArea.setText("[ERROR] Missing current node.");
                clearChoices();
                nextButton.setEnabled(false);
                return;
            }

            // Render lines
            StringBuilder sb = new StringBuilder();
            String speaker = node.speaker == null ? "" : node.speaker;

            for (String line : node.lines) {
                if (!speaker.isEmpty()) sb.append(speaker).append(": ");
                sb.append(applyVars(line, dm.getState())).append("\n");
            }

            if (node.isEnd) {
                sb.append("\n--- END ---");
            }

            dialogueArea.setText(sb.toString());
            dialogueArea.setCaretPosition(0);

            // Render choices
            List<Choice> choices = dm.getAvailableChoices(node);
            clearChoices();

            if (!choices.isEmpty()) {
                nextButton.setEnabled(false);

                JLabel hint = new JLabel("Choose an option:");
                hint.setBorder(new EmptyBorder(4, 4, 8, 4));
                choicesPanel.add(hint);

                for (int i = 0; i < choices.size(); i++) {
                    int idx = i;
                    Choice c = choices.get(i);

                    JButton btn = new JButton(applyVars(c.text, dm.getState()));
                    btn.setAlignmentX(Component.LEFT_ALIGNMENT);
                    btn.addActionListener(e -> {
                        dm.choose(idx, choices);
                        render();
                    });

                    JPanel row = new JPanel(new BorderLayout());
                    row.setBorder(new EmptyBorder(4, 4, 4, 4));
                    row.add(btn, BorderLayout.CENTER);
                    row.setAlignmentX(Component.LEFT_ALIGNMENT);

                    choicesPanel.add(row);
                }
            } else {
                // No choices: allow Next if autoNext exists and not end
                boolean canNext = (!node.isEnd) && (node.autoNextNodeId != null);
                nextButton.setEnabled(canNext);

                if (node.isEnd) {
                    JLabel hint = new JLabel("This is the end. You can Load a save or Quit.");
                    hint.setBorder(new EmptyBorder(8, 4, 8, 4));
                    choicesPanel.add(hint);
                    dm.stop(); // stop after showing end node once
                } else if (!canNext) {
                    JLabel hint = new JLabel("No more nodes to go to. (Load a save or Quit.)");
                    hint.setBorder(new EmptyBorder(8, 4, 8, 4));
                    choicesPanel.add(hint);
                    dm.stop();
                } else {
                    JLabel hint = new JLabel("Click Next to continue.");
                    hint.setBorder(new EmptyBorder(8, 4, 8, 4));
                    choicesPanel.add(hint);
                }
            }

            choicesPanel.revalidate();
            choicesPanel.repaint();
        }

        private void clearChoices() {
            choicesPanel.removeAll();
        }

        private void onSetName() {
            String current = dm.getState().playerName;
            String input = JOptionPane.showInputDialog(frame, "Enter player name:", current);
            if (input != null) {
                input = input.trim();
                if (!input.isEmpty()) {
                    dm.getState().playerName = input;
                    render();
                }
            }
        }

        private void onShowState() {
            GameState s = dm.getState();
            String msg = "playerName = " + s.playerName +
                    "\naffection  = " + s.affection +
                    "\nflags      = " + s.flags;
            JOptionPane.showMessageDialog(frame, msg, "Game State", JOptionPane.INFORMATION_MESSAGE);
        }

        private void onSave() {
            fileChooser.setDialogTitle("Save Game");
            fileChooser.setSelectedFile(new File("save.sav"));

            int res = fileChooser.showSaveDialog(frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();
                try {
                    dm.saveToFile(f);
                    JOptionPane.showMessageDialog(frame, "Saved to:\n" + f.getAbsolutePath(),
                            "Save", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Save failed:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void onLoad() {
            fileChooser.setDialogTitle("Load Game");

            int res = fileChooser.showOpenDialog(frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();
                try {
                    dm.loadFromFile(f);
                    JOptionPane.showMessageDialog(frame, "Loaded from:\n" + f.getAbsolutePath(),
                            "Load", JOptionPane.INFORMATION_MESSAGE);
                    render();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Load failed:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private String applyVars(String text, GameState s) {
            return text
                    .replace("{name}", s.playerName)
                    .replace("{affection}", String.valueOf(s.affection));
        }
    }

    // -----------------------------
    // 6) BUILD SAMPLE STORY GRAPH (ENGLISH ONLY)
    // -----------------------------
    static DialogueManager buildPrototype() {
        DialogueManager dm = new DialogueManager();

        DialogueNode start = new DialogueNode("start", "Narrator")
                .addLine("Welcome, {name}, to GameJeebsaw (Prototype).")
                .addLine("You walk into the club room in the evening...")
                .addChoice(new Choice(
                        "Greet politely",
                        "greet",
                        null,
                        s -> { s.affection += 1; s.setFlag("greeted", true); }
                ))
                .addChoice(new Choice(
                        "Watch quietly from the corner",
                        "sneak",
                        null,
                        s -> { s.setFlag("sneaky", true); }
                ));

        DialogueNode greet = new DialogueNode("greet", "Aoi")
                .addLine("Oh—hi! What's your name?")
                .addLine("(You can use the 'Set Name' button anytime.)")
                .addChoice(new Choice(
                        "Tell your name",
                        "ask_goal",
                        null,
                        s -> s.affection += 1
                ))
                .addChoice(new Choice(
                        "Stay silent",
                        "ask_goal",
                        null,
                        s -> s.affection -= 1
                ));

        DialogueNode sneak = new DialogueNode("sneak", "Narrator")
                .addLine("You hide behind a cabinet and listen to the voices inside...")
                .addLine("Aoi: Who's there? Come out!")
                .setAutoNext("ask_goal");

        DialogueNode askGoal = new DialogueNode("ask_goal", "Aoi")
                .addLine("So, why are you here at the club, {name}?")
                .addChoice(new Choice(
                        "I'm here to make friends",
                        "branch_friend",
                        null,
                        s -> s.affection += 1
                ))
                .addChoice(new Choice(
                        "I want to join the club",
                        "branch_join",
                        null,
                        s -> s.affection += 2
                ))
                .addChoice(new Choice(
                        "Just hiding from the rain",
                        "branch_rain",
                        null,
                        s -> { /* no change */ }
                ));

        DialogueNode branchFriend = new DialogueNode("branch_friend", "Aoi")
                .addLine("Looking for friends, huh... Then let's talk.")
                .addLine("Current affection: {affection}")
                .addChoice(new Choice(
                        "Keep talking",
                        "ending_good",
                        s -> s.affection >= 2,
                        s -> s.setFlag("good_end", true)
                ))
                .addChoice(new Choice(
                        "Head home for now",
                        "ending_neutral"
                ));

        DialogueNode branchJoin = new DialogueNode("branch_join", "Aoi")
                .addLine("Join the club? Perfect—we actually need help right now.")
                .addLine("Current affection: {affection}")
                .addChoice(new Choice(
                        "Accept the responsibility",
                        "ending_good",
                        null,
                        s -> { s.affection += 1; s.setFlag("joined", true); }
                ))
                .addChoice(new Choice(
                        "Let me think about it",
                        "ending_neutral"
                ));

        DialogueNode branchRain = new DialogueNode("branch_rain", "Aoi")
                .addLine("Hiding from the rain? Fair enough. You can wait here until it stops.")
                .addLine("Current affection: {affection}")
                .addChoice(new Choice(
                        "Thanks",
                        "ending_neutral",
                        null,
                        s -> { /* no change */ }
                ));

        DialogueNode endingGood = new DialogueNode("ending_good", "Narrator")
                .addLine("It feels like today started well...")
                .addLine("Aoi smiles at you. Something between you seems to move forward.")
                .addLine("(Tip: Use 'Show State' to see flags and affection.)")
                .setEnd(true);

        DialogueNode endingNeutral = new DialogueNode("ending_neutral", "Narrator")
                .addLine("You head home quietly.")
                .addLine("But maybe... there will be another chance next time.")
                .setEnd(true);

        dm.addNode(start);
        dm.addNode(greet);
        dm.addNode(sneak);
        dm.addNode(askGoal);
        dm.addNode(branchFriend);
        dm.addNode(branchJoin);
        dm.addNode(branchRain);
        dm.addNode(endingGood);
        dm.addNode(endingNeutral);

        dm.start("start");
        return dm;
    }

    // -----------------------------
    // MAIN
    // -----------------------------
    public static void main(String[] args) {
        DialogueManager dm = buildPrototype();
        SwingRenderer ui = new SwingRenderer(dm);
        ui.show();
    }
}