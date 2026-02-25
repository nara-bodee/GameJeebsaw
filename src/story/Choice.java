package story;

public class Choice {
    private String text;
    private int affectionChange;
    private int teaseChange;
    private String responseText;
    private String outcomeBgPath;
    private boolean openShop;

    public Choice(String text, int affectionChange, int teaseChange, String responseText, String outcomeBgPath) {
        this(text, affectionChange, teaseChange, responseText, outcomeBgPath, false);
    }

    public Choice(String text, int affectionChange, int teaseChange, String responseText, String outcomeBgPath, boolean openShop) {
        this.text = text;
        this.affectionChange = affectionChange;
        this.teaseChange = teaseChange;
        this.responseText = responseText;
        this.outcomeBgPath = outcomeBgPath;
        this.openShop = openShop;
    }

    public String getText() { return text; }
    public int getAffectionChange() { return affectionChange; }
    public int getTeaseChange() { return teaseChange; }
    public String getResponseText() { return responseText; }
    public String getOutcomeBgPath() { return outcomeBgPath; }
    public boolean isOpenShop() { return openShop; }
}
