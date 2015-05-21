package wav.demon.StatCraft.Magic;

import org.bukkit.Statistic;

public enum MoveCode {
    WALKING((byte)0, Statistic.WALK_ONE_CM),
    CROUCHING((byte)1, Statistic.CROUCH_ONE_CM),
    SPRINTING((byte)2, Statistic.SPRINT_ONE_CM),
    SWIMMING((byte)3, Statistic.SWIM_ONE_CM),
    FALLING((byte)4, Statistic.FALL_ONE_CM),
    CLIMBING((byte)5, Statistic.CLIMB_ONE_CM),
    FLYING((byte)6, Statistic.FLY_ONE_CM),
    DIVING((byte)7, Statistic.DIVE_ONE_CM),
    MINECART((byte)8, Statistic.MINECART_ONE_CM),
    BOAT((byte)9, Statistic.BOAT_ONE_CM),
    PIG((byte)10, Statistic.PIG_ONE_CM),
    HORSE((byte)11, Statistic.HORSE_ONE_CM);

    private byte code;
    private Statistic statistic;

    MoveCode(byte code, Statistic statistic) {
        this.code = code;
        this.statistic = statistic;
    }

    public byte getCode() {
        return code;
    }

    public static MoveCode fromCode(short code) {
        for (MoveCode moveCode : values()) {
            if (code == moveCode.getCode())
                return moveCode;
        }
        return null;
    }

    public Statistic getStat() {
        return statistic;
    }
}
