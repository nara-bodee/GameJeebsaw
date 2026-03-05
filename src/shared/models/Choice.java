package shared.models;

public class Choice {
    public String choiceId;
    public String text;
    public Requirement require = new Requirement();
    public Effect effect = new Effect();
    public String resultText;

    public Choice() {}

    public Choice(String choiceId, String text) {
        this.choiceId = choiceId;
        this.text = text;
    }
}
