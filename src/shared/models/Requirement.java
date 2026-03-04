package shared.models;

public class Requirement {
    public Integer charmMin;    // nullable - minimum charm required
    public Integer moneyMin;    // nullable - minimum money required

    public Requirement() {}

    public Requirement(Integer charmMin, Integer moneyMin) {
        this.charmMin = charmMin;
        this.moneyMin = moneyMin;
    }

    public boolean isMet(int currentCharm, int currentMoney) {
        if (charmMin != null && currentCharm < charmMin) return false;
        if (moneyMin != null && currentMoney < moneyMin) return false;
        return true;
    }
}
