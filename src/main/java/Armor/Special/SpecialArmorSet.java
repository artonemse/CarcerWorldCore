package Armor.Special;

public enum SpecialArmorSet {

    BLACKTHORN(
            "blackthorn",
            "&2&lBlackthorn",
            15.0,
            10.0,
            8.0,
            20.0,
            40.0,
            20.0
    );

    private final String id;
    private final String displayName;
    private final double pieceHealth;
    private final double pieceDamage;
    private final double pieceDamageReduction;
    private final double twoPieceDamage;
    private final double fourPieceDamage;
    private final double fourPieceDamageReduction;

    SpecialArmorSet(String id, String displayName, double pieceHealth, double pieceDamage, double pieceDamageReduction, double twoPieceDamage, double fourPieceDamage, double fourPieceDamageReduction) {
        this.id = id;
        this.displayName = displayName;
        this.pieceHealth = pieceHealth;
        this.pieceDamage = pieceDamage;
        this.pieceDamageReduction = pieceDamageReduction;
        this.twoPieceDamage = twoPieceDamage;
        this.fourPieceDamage = fourPieceDamage;
        this.fourPieceDamageReduction = fourPieceDamageReduction;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPieceHealth() {
        return pieceHealth;
    }

    public double getPieceDamage() {
        return pieceDamage;
    }

    public double getPieceDamageReduction() {
        return pieceDamageReduction;
    }

    public double getTwoPieceDamage() {
        return twoPieceDamage;
    }

    public double getFourPieceDamage() {
        return fourPieceDamage;
    }

    public double getFourPieceDamageReduction() {
        return fourPieceDamageReduction;
    }

    public static SpecialArmorSet fromId(String id) {
        if (id == null) return null;

        for (SpecialArmorSet set : values()) {
            if (set.id.equalsIgnoreCase(id)) return set;
        }

        return null;
    }
}
