package wav.demon.StatCraft.Magic;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;

public enum EntityCode {

    SKELETON((byte)0),
    WITHER_SKELETON((byte)1);

    private byte code;

    EntityCode(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static EntityCode fromCode(byte code) {
        for (EntityCode entityCode : values()) {
            if (code == entityCode.getCode())
                return entityCode;
        }
        return null;
    }

    public static EntityCode fromEntity(Entity entity) {
        if (entity instanceof Skeleton) {
            Skeleton skeleton = (Skeleton) entity;
            if (skeleton.getSkeletonType() == Skeleton.SkeletonType.NORMAL)
                return EntityCode.SKELETON;
            else if (skeleton.getSkeletonType() == Skeleton.SkeletonType.WITHER)
                return EntityCode.WITHER_SKELETON;
        }

        return EntityCode.SKELETON;
    }
}
