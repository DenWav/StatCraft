package wav.demon.StatCraft.Magic;

public enum FishCode {

    FISH((byte)0),
    TREASURE((byte)1),
    JUNK((byte)2);

    private byte code;

    FishCode(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static FishCode fromCode(byte code) {
        for (FishCode fishCode : values()) {
            if (code == fishCode.getCode())
                return fishCode;
        }
        return null;
    }
}
