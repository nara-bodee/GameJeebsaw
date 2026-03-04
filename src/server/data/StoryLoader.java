package server.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import shared.models.Event;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * StoryLoader - loads event data from story.json
 * DEV BIRD responsibility: Provide event data with choices and effects
 */
public class StoryLoader {
    private final Gson gson = new Gson();

    /**
     * Load all story events from resources
     */
    public List<Event> loadEvents() {
        try {
            InputStreamReader reader = new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream("story.json")
            );
            
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> rawData = gson.fromJson(reader, mapType);
            
            Object days = rawData.get("days");
            if (days != null) {
                String daysJson = gson.toJson(days);
                Type listType = new TypeToken<List<Event>>(){}.getType();
                return gson.fromJson(daysJson, listType);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error loading story.json: " + e.getMessage());
            return generateDefaultStory();
        }
    }

    /**
     * Generate default story if JSON loading fails
     * This provides fallback story content
     */
    private List<Event> generateDefaultStory() {
        List<Event> events = new ArrayList<>();

        // Day 1 Event
        Event day1 = new Event(1, "D1_E1");
        day1.background = "images_Story/image_StoryDay1/bg.png";
        day1.heroineSprite = "images_Story/image_StoryDay1/heroine.png";
        day1.dialogue.add("ตอนเย็น: สื่อาคารมาเจอเลม่อนที่ร้านหน้าปากซอย...");
        day1.dialogue.add("เลม่อน: 'ขอโทษด้วยนะ พอดีลืมกระเป๋าตังค์ไว้บ้าน'");

        shared.models.Choice choice1 = new shared.models.Choice("C1_1", "รวมเงินมาจ่าย");
        choice1.effect.love = 20;
        choice1.effect.charm = 5;
        choice1.resultText = "เลม่อน: 'หูยย ฮีโร่! ขอบคุณมากๆนะ!' [ความชอบ +20]";

        shared.models.Choice choice2 = new shared.models.Choice("C1_2", "ให้ยืมไปก่อน");
        choice2.effect.love = 10;
        choice2.resultText = "เลม่อน: 'คืนแน่นอนจ้า ขอบคุณค่า' [ความชอบ +10]";

        shared.models.Choice choice3 = new shared.models.Choice("C1_3", "เดินหนีไปเงียบๆ");
        choice3.effect.love = -20;
        choice3.resultText = "(เลม่อนมองตามด้วยสายตาเศร้า) [ความชอบ -20]";

        day1.choices.add(choice1);
        day1.choices.add(choice2);
        day1.choices.add(choice3);
        events.add(day1);

        // Day 2 Event
        Event day2 = new Event(2, "D2_E1");
        day2.background = "images_Story/image_StoryDay2/bg.png";
        day2.heroineSprite = "images_Story/image_StoryDay2/heroine.png";
        day2.dialogue.add("ตอนเที่ยง: เลม่อนกวักมือเรียกให้นั่งกินข้าวด้วย");
        day2.dialogue.add("เลม่อน: 'นนท์! ทางนี้ๆ! มานั่งกินข้าวเที่ยงด้วยกันสิ'");

        shared.models.Choice choice2_1 = new shared.models.Choice("C2_1", "ยิ้มและนั่งข้างเธอ");
        choice2_1.effect.love = 15;
        choice2_1.effect.charm = 10;
        choice2_1.resultText = "เลม่อน: 'ดีใจด้วยนะ!' [ความชอบ +15]";

        shared.models.Choice choice2_2 = new shared.models.Choice("C2_2", "บอกว่ามีธุระ");
        choice2_2.effect.love = -10;
        choice2_2.resultText = "เลม่อน: 'อ๋อ... งั้นไหน' (ดูเศร้า) [ความชอบ -10]";

        day2.choices.add(choice2_1);
        day2.choices.add(choice2_2);
        events.add(day2);

        // Add more days as needed (Days 3-7)
        for (int i = 3; i <= 7; i++) {
            Event dayEvent = new Event(i, "D" + i + "_E1");
            dayEvent.background = "images_Story/image_StoryDay" + i + "/bg.png";
            dayEvent.heroineSprite = "images_Story/image_StoryDay" + i + "/heroine.png";
            dayEvent.dialogue.add("วันที่ " + i + " - เหตุการณ์ของวันนี้");

            shared.models.Choice simpleChoice = new shared.models.Choice("C" + i + "_1", "ตัวเลือก");
            simpleChoice.effect.love = 10;
            dayEvent.choices.add(simpleChoice);
            events.add(dayEvent);
        }

        return events;
    }
}
