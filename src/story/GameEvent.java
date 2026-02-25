package story;

import java.util.ArrayList;
import java.util.List;

public class GameEvent {
    private String eventId;
    private String eventName;
    private List<String> introTexts;
    private List<String> introBgPaths;
    private String questionText;
    private String questionBgPath;
    private List<Choice> choices;

    public GameEvent(String eventId, String eventName, String firstIntroText, String firstIntroBgPath, String questionText, String questionBgPath) {
        this.eventId = eventId;
        this.eventName = eventName;

        this.introTexts = new ArrayList<>();
        this.introBgPaths = new ArrayList<>();
        this.introTexts.add(firstIntroText);
        this.introBgPaths.add(firstIntroBgPath);

        this.questionText = questionText;
        this.questionBgPath = questionBgPath;
        this.choices = new ArrayList<>();
    }

    public void addIntroSequence(String text, String bgPath) {
        this.introTexts.add(text);
        this.introBgPaths.add(bgPath);
    }

    public void addChoice(Choice choice) {
        this.choices.add(choice);
    }

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public List<String> getIntroTexts() { return introTexts; }
    public List<String> getIntroBgPaths() { return introBgPaths; }
    public String getQuestionText() { return questionText; }
    public String getQuestionBgPath() { return questionBgPath; }
    public List<Choice> getChoices() { return choices; }
}
