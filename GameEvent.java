import java.util.ArrayList;
import java.util.List;

public class GameEvent {
    private String eventId;
    private String eventName;
    private String introText;       
    private String introBgPath;     
    private String questionText;    
    private String questionBgPath;  
    private List<Choice> choices;   

    public GameEvent(String eventId, String eventName, 
                     String introText, String introBgPath, 
                     String questionText, String questionBgPath) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.introText = introText;
        this.introBgPath = introBgPath;
        this.questionText = questionText;
        this.questionBgPath = questionBgPath;
        this.choices = new ArrayList<>();
    }

    public void addChoice(Choice choice) {
        this.choices.add(choice);
    }

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getIntroText() { return introText; }
    public String getIntroBgPath() { return introBgPath; }
    public String getQuestionText() { return questionText; }
    public String getQuestionBgPath() { return questionBgPath; }
    public List<Choice> getChoices() { return choices; }
}