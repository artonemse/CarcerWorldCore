package NPCs;

import java.util.List;

public class CarcerNPC {

    private final String id;
    private final int citizensId;
    private final String name;
    private final List<String> dialogue;

    public CarcerNPC(String id, int citizensId, String name, List<String> dialogue) {
        this.id = id;
        this.citizensId = citizensId;
        this.name = name;
        this.dialogue = dialogue;
    }

    public String getId() {
        return id;
    }

    public int getCitizensId() {
        return citizensId;
    }

    public String getName() {
        return name;
    }

    public List<String> getDialogue() {
        return dialogue;
    }
}
