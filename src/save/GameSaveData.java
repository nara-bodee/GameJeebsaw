package save;

import core.Player;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import story.GameEvent;

public class GameSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String saveDate;
    private final String chapterName;

    private final String playerName;
    private final int currentSceneId;
    private final int affectionGirlA;
    private final int affectionGirlB;

    private final int currentDay;
    private final int eventStep;
    private final int introIndex;
    private final Player player;
    private final GameEvent activeEvent;

    public GameSaveData(String chapterName, int currentDay, int eventStep, int introIndex, Player player, GameEvent activeEvent) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        this.saveDate = formatter.format(new Date());

        this.chapterName = chapterName;
        this.playerName = "Player";
        this.currentSceneId = currentDay;
        this.affectionGirlA = player != null ? player.getAffectionScore() : 0;
        this.affectionGirlB = 0;

        this.currentDay = currentDay;
        this.eventStep = eventStep;
        this.introIndex = introIndex;
        this.player = player;
        this.activeEvent = activeEvent;
    }

    public String getSaveDate() { return saveDate; }
    public String getChapterName() { return chapterName; }

    public String getPlayerName() { return playerName; }
    public int getCurrentSceneId() { return currentSceneId; }
    public int getAffectionGirlA() { return affectionGirlA; }
    public int getAffectionGirlB() { return affectionGirlB; }

    public int getCurrentDay() { return currentDay; }
    public int getEventStep() { return eventStep; }
    public int getIntroIndex() { return introIndex; }
    public Player getPlayer() { return player; }
    public GameEvent getActiveEvent() { return activeEvent; }
}
