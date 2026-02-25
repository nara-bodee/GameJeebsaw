package core;

import java.util.HashSet;
import java.util.Set;

public class Player {
    private int affectionScore = 0;
    private int teaseCount = 0;
    private Set<String> inventory = new HashSet<>();

    public void addAffection(int amount) { this.affectionScore += amount; }
    public void addTease() { this.teaseCount++; }

    public int getAffectionScore() { return affectionScore; }
    public int getTeaseCount() { return teaseCount; }

    public void addItem(String itemName) {
        this.inventory.add(itemName);
    }

    public boolean hasItem(String itemName) {
        return inventory.contains(itemName);
    }
}
