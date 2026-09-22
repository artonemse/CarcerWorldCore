package Cosmetics.Chat;

import org.bukkit.ChatColor;

import java.util.Locale;

public enum ChatCosmetic {

    WHITE("White", ChatColor.WHITE, 0),
    GRAY("Gray", ChatColor.GRAY, 150),
    DARK_GRAY("Dark Gray", ChatColor.DARK_GRAY, 200),
    BLACK("Black", ChatColor.BLACK, 200),
    RED("Red", ChatColor.RED, 300),
    DARK_RED("Dark Red", ChatColor.DARK_RED, 400),
    GOLD("Gold", ChatColor.GOLD, 450),
    YELLOW("Yellow", ChatColor.YELLOW, 250),
    GREEN("Green", ChatColor.GREEN, 300),
    DARK_GREEN("Dark Green", ChatColor.DARK_GREEN, 250),
    AQUA("Aqua", ChatColor.AQUA, 400),
    DARK_AQUA("Dark Aqua", ChatColor.DARK_AQUA, 300),
    BLUE("Blue", ChatColor.BLUE, 300),
    DARK_BLUE("Dark Blue", ChatColor.DARK_BLUE, 250),
    LIGHT_PURPLE("Light Purple", ChatColor.LIGHT_PURPLE, 450),
    DARK_PURPLE("Dark Purple", ChatColor.DARK_PURPLE, 400),

    EMBER("Ember", 350, 0xFF5733, 0xFFD166, "", 0, "Purchase with Gems."),
    TWILIGHT("Twilight", 450, 0x9D4EDD, 0xFF85C0, "", 0, "Purchase with Gems."),
    OCEAN("Ocean", 350, 0x168BFF, 0x77FFE2, "", 0, "Purchase with Gems."),
    AURORA("Aurora", 600, 0x65FFBA, 0xB084FF, "world_explorer", 0, "Complete World Explorer, or purchase with Gems."),
    BLACKTHORN_BLOOM("Blackthorn Bloom", -1, 0x4D9964, 0xD8E887, "clearing_the_road", 0, "Complete Clearing the Road."),
    SOULFIRE("Soulfire", -1, 0x42E8FF, 0xB45CFF, "", 5, "Reach Ascension 5."),

    TAG_WANDERER("Wanderer", ChatColor.GRAY, "world_explorer", 0, 0, "Complete World Explorer."),
    TAG_GRAVEBORN("Graveborn", ChatColor.DARK_GREEN, "slayer_1000", 0, 0, "Complete Slayer I."),
    TAG_RELIC_HUNTER("Relic Hunter", ChatColor.GOLD, "", 0, 0, "Reserved for future relic extraction rewards."),
    TAG_SOULBOUND("Soulbound", ChatColor.AQUA, "", 0, 1, "Reach Ascension 1."),
    TAG_BLACKTHORN("Blackthorn", ChatColor.GREEN, "clearing_the_road", 0, 0, "Complete Clearing the Road."),
    TAG_OATHBREAKER("Oathbreaker", ChatColor.RED, "", 0, 3, "Reach Ascension 3."),
    TAG_NIGHTSTALKER("Nightstalker", ChatColor.DARK_PURPLE, "", 2500, 0, "Reach 2,500 recorded mob kills."),
    TAG_TIDEBORN("Tideborn", ChatColor.BLUE, "", 0, 2, "Reach Ascension 2."),
    TAG_KINGSLAYER("Kingslayer", ChatColor.YELLOW, "", 10000, 0, "Reach 10,000 recorded mob kills."),
    TAG_ASCENDANT("Ascendant", ChatColor.LIGHT_PURPLE, "", 0, 5, "Reach Ascension 5.");

    public enum Kind {
        BASIC, PREMIUM, TAG
    }

    private final String displayName;
    private final Kind kind;
    private final ChatColor color;
    private final long defaultPrice;
    private final int startRGB;
    private final int endRGB;
    private final String defaultQuest;
    private final long defaultKills;
    private final int defaultAscension;
    private final String defaultRequirement;

    ChatCosmetic(String name, ChatColor color, long price) {
        this(name, Kind.BASIC, color, price, 0, 0, "", 0, 0, "Purchase with Scraps.");
    }

    ChatCosmetic(String name, long price, int start, int end, String quest, int ascension, String requirement) {
        this(name, Kind.PREMIUM, null, price, start, end, quest, 0, ascension, requirement);
    }

    ChatCosmetic(String name, ChatColor color, String quest, long kills, int ascension, String requirement) {
        this(name, Kind.TAG, color, -1, 0, 0, quest, kills, ascension, requirement);
    }

    ChatCosmetic(String name, Kind kind, ChatColor color, long price, int start, int end, String quest, long kills, int ascension, String requirement) {
        this.displayName = name;
        this.kind = kind;
        this.color = color;
        this.defaultPrice = price;
        this.startRGB = start;
        this.endRGB = end;
        this.defaultQuest = quest;
        this.defaultKills = kills;
        this.defaultAscension = ascension;
        this.defaultRequirement = requirement;
    }

    public String id() { return name().toLowerCase(Locale.ROOT); }
    public String displayName() { return displayName; }
    public Kind kind() { return kind; }
    public long defaultPrice() { return defaultPrice; }
    public String defaultQuest() { return defaultQuest; }
    public long defaultKills() { return defaultKills; }
    public int defaultAscension() { return defaultAscension; }
    public String defaultRequirement() { return defaultRequirement; }

    public String paint(String text) {
        if (kind != Kind.PREMIUM) return color + text + ChatColor.RESET;

        int[] characters = text.codePoints().toArray();
        StringBuilder result = new StringBuilder(characters.length * 16);

        for (int i = 0; i < characters.length; i++) {
            double progress = characters.length <= 1 ? 0.0 : (double) i / (characters.length - 1);

            int red = interpolate((startRGB >> 16) & 255, (endRGB >> 16) & 255, progress);
            int green = interpolate((startRGB >> 8) & 255, (endRGB >> 8) & 255, progress);
            int blue = interpolate(startRGB & 255, endRGB & 255, progress);

            result.append(hex((red << 16) | (green << 8) | blue));
            result.appendCodePoint(characters[i]);
        }

        return result.append(ChatColor.RESET).toString();
    }

    private static int interpolate(int start, int end, double progress) {
        return (int) Math.round(start + (end - start) * progress);
    }

    private static String hex(int rgb) {
        String digits = String.format(Locale.ROOT, "%06x", rgb);
        StringBuilder result = new StringBuilder("§x");

        for (char digit : digits.toCharArray()) result.append('§').append(digit);

        return result.toString();
    }

    public static ChatCosmetic fromId(String id) {
        if (id == null) return null;

        try {
            return valueOf(id.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
