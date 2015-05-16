package wav.demon.StatCraft.Magic;

public enum ArrowCode {

    NORMAL((byte)0),
    FLAMING((byte)1);

    private byte code;

    ArrowCode(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static ArrowCode fromCode(byte code) {
        for (ArrowCode arrowCode : values()) {
            if (code == arrowCode.getCode())
                return arrowCode;
        }
        return null;
    }
}
