package wav.demon;

public enum StatTypes {
    //    <- to the right of an item indicates it is being implemented
    ///   <- to the right of an item indicates it has been implemented
    //x   <- to the right of an item indicates it has been implemented, but not in the desired fashion
    ///x  <- to the right of an item indicates a successful implementation has not yet been found

    DEATH            (1 , "death"),           ///
    BLOCK_BREAK      (2 , "block_break"),     ///
    BLOCK_PLACE      (3 , "block_place"),     ///
    PLAY_TIME        (4 , "play_time"),       ///
    ITEMS_CRAFTED    (5 , "items_crafted"),   ///
    ON_FIRE          (6 , "on_fire"),         //x  can only count the number of times a player has been on fire, not how long
    WORLD_CHANGE     (7 , "world_change"),
    TOOLS_BROKEN     (8 , "tools_broken"),    ///
    ARROWS_SHOT      (9 , "arrows_shot"),     ///
    LAST_JOIN_TIME   (10, "last_join_time"),  ///
    LAST_LEAVE_TIME  (11, "last_leave_time"), ///
    FILL_BUCKET      (12, "bucket_fill"),     ///
    EMPTY_BUCKET     (13, "bucket_empty"),    ///
    ITEM_DROPS       (14, "item_drops"),      ///
    ITEM_PICKUPS     (15, "item_pickups"),    ///
    ENTER_BED        (16, "enter_bed"),
    TIME_SLEPT       (17, "time_slept"),
    WORDS_SPOKEN     (18, "words_spoken"),
    DAMAGE_TAKEN     (19, "damage_taken"),
    FISH_CAUGHT      (20, "fish_caught"),
    JOINS            (21, "joins"),
    XP_GAINED        (22, "xp_gained"),
    MOVE             (23, "move"),
    KILLS            (24, "kills"),
    JUMPS            (25, "jumps"),
    FALLEN           (26, "fallen"),
    DEATH_LOCATIONS  (27, "death_locations"),
    EGGS_THROWN      (28, "eggs_thrown"),
    CHICKEN_HATCHES  (29, "chicken_hatches"),
    ENDER_PEARLS     (30, "ender_pearls"),
    ANIMALS_BRED     (31, "animals_bred"),
    TNT_DETONATED    (32, "tnt_detonated"),
    ENCHANTS_DONE    (33, "enchants_done"),
    HIGHEST_LEVEL    (34, "highest_level"),
    DAMAGE_DEALT     (35, "damage_dealt"),
    ITEMS_BREWED     (36, "items_brewed"),
    ITEMS_COOKED     (37, "items_cooked"),
    FIRES_STARTED    (38, "fires_started");

    public final int id;
    public final String title;

    StatTypes(int id, String title) {

        this.id = id;
        this.title = title;
    }

}
