package shared.models;

import java.util.ArrayList;
import java.util.List;

public class Event {
    public int day;
    public String eventId;
    public String background;      // simplified - image path
    public String heroineSprite;   // simplified - image path
    public List<String> dialogue = new ArrayList<>(); // simplified text
    public List<Choice> choices = new ArrayList<>();

    public Event() {}

    public Event(int day, String eventId) {
        this.day = day;
        this.eventId = eventId;
    }
}
