package wav.demon.StatCraft.Magic;

public enum ProjectilesCode {

    NORMAL_ARROW((short)0),
    FLAMING_ARROW((short)1),
    ENDER_PEARL((short)2),
    UNHATCHED_EGG((short)3),
    HATCHED_EGG((short)4),
    SNOWBALL((short)5);

    private short code;

    ProjectilesCode(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }

    public static ProjectilesCode fromCode(short code) {
        for (ProjectilesCode projectilesCode : values()) {
            if (code == projectilesCode.getCode())
                return projectilesCode;
        }
        return null;
    }
}
