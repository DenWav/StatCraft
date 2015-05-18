package wav.demon.StatCraft.MySQL;

import java.util.Arrays;
import java.util.List;

public enum Table {

    ANIMALS_BRED("(" +
            "id INT UNSIGNED NOT NULL, " +
            "animal VARCHAR(50) NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, animal)" +
            ")",
            Arrays.asList("id", "animal", "amount")),
    ARROWS_SHOT("(" +
            "id INT UNSIGNED NOT NULL, " +
            "type TINYINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, type)" +
            ")",
            Arrays.asList("id", "type", "amount")),
    BLOCK_BREAK("(" +
            "id INT UNSIGNED NOT NULL, " +
            "blockid SMALLINT NOT NULL, " +
            "damage SMALLINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, blockid, damage)" +
            ")",
            Arrays.asList("id", "blockid", "damage", "amount")),
    BLOCK_PLACE("(" +
            "id INT UNSIGNED NOT NULL, " +
            "blockid SMALLINT NOT NULL, " +
            "damage SMALLINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, blockid, damage)" +
            ")",
            Arrays.asList("id", "blockid", "damage", "amount")),
    BUCKET_EMPTY("(" +
            "id INT UNSIGNED NOT NULL, " +
            "type TINYINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, type)" +
            ")",
            Arrays.asList("id", "type", "amount")),
    BUCKET_FILL("(" +
            "id INT UNSIGNED NOT NULL, " +
            "type TINYINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, type)" +
            ")",
            Arrays.asList("id", "type", "amount")),
    DAMAGE_DEALT("(" +
            "id INT UNSIGNED NOT NULL, " +
            "entity VARCHAR(50) NOT NULL, " +
            "type TINYINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, entity, type)" +
            ")",
            Arrays.asList("id", "entity", "type", "amount")),
    DAMAGE_TAKEN("(" +
            "id INT UNSIGNED NOT NULL, " +
            "entity VARCHAR(50) NOT NULL, " +
            "type TINYINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, entity, type)" +
            ")",
            Arrays.asList("id", "entity", "type", "amount")),
    DEATH_BY_CAUSE("(" +
            "id INT UNSIGNED NOT NULL, " +
            "cause VARCHAR(50) NOT NULL, " +
            "type TINYINT NOT NULL, " +
            "world VARCHAR(50) NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, cause, type, world)" +
            ")",
            Arrays.asList("id", "cause", "type", "world", "amount")),
    DEATH("(" +
            "id INT UNSIGNED NOT NULL, " +
            "message VARCHAR(200) NOT NULL, " +
            "world VARCHAR(50) NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, message, world)" +
            ")",
            Arrays.asList("id", "message", "world", "amount")),
    EATING("(" +
            "id INT UNSIGNED NOT NULL, " +
            "food VARCHAR(20) NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, food)" +
            ")",
            Arrays.asList("id", "food", "amount")),
    EGGS_THROWN("(" +
            "id INT UNSIGNED NOT NULL, " +
            "hatched BOOL NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, hatched)" +
            ")",
            Arrays.asList("id", "hatched", "amount")),
    ENCHANTS_DONE("(" +
            "id INT UNSIGNED NOT NULL, " +
            "item SMALLINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, item)" +
            ")",
            Arrays.asList("id", "item", "amount")),
    ENDER_PEARLS("(" +
            "id INT UNSIGNED NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "distance INT UNSIGNED NOT NULL, " +
            "max_throw INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id)" +
            ")",
            Arrays.asList("id", "amount", "distance", "max_throw")),
    ENTER_BED("(" +
            "id INT UNSIGNED NOT NULL, " +
            "time INT NOT NULL, " +
            "UNIQUE INDEX (id)" +
            ")",
            Arrays.asList("id", "time")),
    FALLEN("(" +
            "id INT UNSIGNED NOT NULL, " +
            "distance BIGINT NOT NULL, " +
            "UNIQUE INDEX (id)" +
            ")",
            Arrays.asList("id", "distance")),
    FIRES_STARTED(Table.simple,
            Arrays.asList("id", "amount")),
    FISH_CAUGHT("(" +
            "id INT UNSIGNED NOT NULL, " +
            "item SMALLINT NOT NULL, " +
            "damage SMALLINT NOT NULL, " +
            "type TINYINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, item, damage, type)" +
            ")",
            Arrays.asList("id", "item", "damage", "type", "amount")),
    HIGHEST_LEVEL("(" +
            "id INT UNSIGNED NOT NULL, " +
            "level INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id)" +
            ")",
            Arrays.asList("id", "level")),
    ITEM_DROPS("(" +
            "id INT UNSIGNED NOT NULL, " +
            "item SMALLINT NOT NULL, " +
            "damage SMALLINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, item, damage)" +
            ")",
            Arrays.asList("id", "item", "damage", "amount")),
    ITEM_PICKUPS("(" +
            "id INT UNSIGNED NOT NULL, " +
            "item SMALLINT NOT NULL, " +
            "damage SMALLINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, item, damage)" +
            ")",
            Arrays.asList("id", "item", "damage", "amount")),
    ITEMS_BREWED("(" +
            "id INT UNSIGNED NOT NULL, " +
            "item SMALLINT NOT NULL, " +
            "damage SMALLINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, item, damage)" +
            ")",
            Arrays.asList("id", "item", "damage", "amount")),
    ITEMS_COOKED("(" +
            "id INT UNSIGNED NOT NULL, " +
            "item SMALLINT NOT NULL, " +
            "damage SMALLINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, item, damage)" +
            ")",
            Arrays.asList("id", "item", "damage", "amount")),
    ITEMS_CRAFTED("(" +
            "id INT UNSIGNED NOT NULL, " +
            "item SMALLINT NOT NULL, " +
            "damage SMALLINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, item, damage)" +
            ")",
            Arrays.asList("id", "item", "damage", "amount")),
    JOINS(Table.simple,
            Arrays.asList("id", "amount")),
    JUMPS(Table.simple,
            Arrays.asList("id", "amount")),
    KILLS("(" +
            "id INT UNSIGNED NOT NULL, " +
            "entity VARCHAR(50) NOT NULL, " +
            "type TINYINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, entity, type)" +
            ")",
            Arrays.asList("id", "entity", "type", "amount")),
    LAST_JOIN_TIME("(" +
            "id INT UNSIGNED NOT NULL, " +
            "time INT NOT NULL, " +
            "UNIQUE INDEX (id)" +
            ")",
            Arrays.asList("id", "time")),
    LAST_LEAVE_TIME("(" +
            "id INT UNSIGNED NOT NULL, " +
            "time INT NOT NULL, " +
            "UNIQUE INDEX (id)" +
            ")",
            Arrays.asList("id", "time")),
    LEAVE_BED("(" +
            "id INT UNSIGNED NOT NULL, " +
            "time INT NOT NULL, " +
            "UNIQUE INDEX (id)" +
            ")",
            Arrays.asList("id", "time")),
    MESSAGES_SPOKEN(Table.simple,
            Arrays.asList("id", "amount")),
    MOVE("(" +
            "id INT UNSIGNED NOT NULL, " +
            "vehicle TINYINT NOT NULL, " +
            "distance INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, vehicle)" +
            ")",
            Arrays.asList("id", "vehicle", "distance")),
    ON_FIRE("(" +
            "id INT UNSIGNED NOT NULL, " +
            "time INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id)" +
            ")",
            Arrays.asList("id", "time")),
    PLAYERS("(" +
            "uuid BINARY(16) NOT NULL, " +
            "name VARCHAR(16) NOT NULL, " +
            "id INT UNSIGNED NOT NULL AUTO_INCREMENT, " +
            "PRIMARY KEY (uuid), " +
            "UNIQUE INDEX (id)" +
            ")",
            Arrays.asList("uuid", "name", "id")),
    PLAY_TIME(Table.simple,
            Arrays.asList("id", "amount")),
    SHEARING("(" +
            "id INT UNSIGNED NOT NULL, " +
            "color TINYINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, color)" +
            ")",
            Arrays.asList("id", "color", "amount")),
    TAB_COMPLETE(Table.simple,
            Arrays.asList("id", "amount")),
    TIME_SLEPT(Table.simple,
            Arrays.asList("id", "amount")),
    TNT_DETONATED(Table.simple,
            Arrays.asList("id", "amount")),
    TOOLS_BROKEN("(" +
            "id INT UNSIGNED NOT NULL, " +
            "item SMALLINT NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, item)" +
            ")",
            Arrays.asList("id", "item", "amount")),
    WORD_FREQUENCY("(" +
            "id INT UNSIGNED NOT NULL, " +
            "word VARCHAR(100) NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, word)" +
            ")",
            Arrays.asList("id", "word", "amount")),
    WORLD_CHANGE("(" +
            "id INT UNSIGNED NOT NULL, " +
            "from_world VARCHAR(50) NOT NULL, " +
            "to_world VARCHAR(50) NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id, from_world, to_world)" +
            ")",
            Arrays.asList("id", "from_world", "to_world", "amount")),
    XP_GAINED(Table.simple,
            Arrays.asList("id", "amount"));


    final private static String simple =
            "(" +
            "id INT UNSIGNED NOT NULL, " +
            "amount INT UNSIGNED NOT NULL, " +
            "UNIQUE INDEX (id)" +
            ")";

    private String create;
    private List<String> columnNames;

    Table(String create, List<String> columnNames) {
        this.create = create;
        this.columnNames = columnNames;
    }

    public String getCreate() {
        return create;
    }

    public String getName() {
        return name().toLowerCase();
    }

    public int getColumns() {
        return columnNames.size();
    }

    public List<String> getColumnNames() {
        return columnNames;
    }
}
