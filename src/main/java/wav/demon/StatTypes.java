package wav.demon;

public enum StatTypes {
    //    <- to the right of an item indicates it is in the process of being implemented
    ///   <- to the right of an item indicates it has been implemented
    //x   <- to the right of an item indicates it has been implemented, but not in the desired fashion
    ///x  <- to the right of an item indicates a successful implementation has not yet been found

    DEATH            (1 , "death"),           ///
    BLOCK_BREAK      (2 , "block_break"),     ///
    BLOCK_PLACE      (3 , "block_place"),     ///
    PLAY_TIME        (4 , "play_time"),       ///
    ITEMS_CRAFTED    (5 , "items_crafted"),   ///
    ON_FIRE          (6 , "on_fire"),         ///
    WORLD_CHANGE     (7 , "world_change"),    ///
    TOOLS_BROKEN     (8 , "tools_broken"),    ///
    ARROWS_SHOT      (9 , "arrows_shot"),     ///
    LAST_JOIN_TIME   (10, "last_join_time"),  ///
    LAST_LEAVE_TIME  (11, "last_leave_time"), ///
    FILL_BUCKET      (12, "bucket_fill"),     ///
    EMPTY_BUCKET     (13, "bucket_empty"),    ///
    ITEM_DROPS       (14, "item_drops"),      ///
    ITEM_PICKUPS     (15, "item_pickups"),    ///
    ENTER_BED        (16, "enter_bed"),       ///
    LEAVE_BED        (17, "leave_bed"),       ///
    TIME_SLEPT       (18, "time_slept"),      ///
    WORDS_SPOKEN     (19, "words_spoken"),    ///
    MESSAGES_SPOKEN  (20, "messages_spoken"), ///
    DAMAGE_TAKEN     (21, "damage_taken"),    ///
    FISH_CAUGHT      (22, "fish_caught"),     ///
    JOINS            (23, "joins"),           ///
    XP_GAINED        (24, "xp_gained"),       ///
    MOVE             (25, "move"),
    KILLS            (26, "kills"),           ///
    JUMPS            (27, "jumps"),
    FALLEN           (28, "fallen"),
    DEATH_LOCATIONS  (29, "death_locations"), ///
    EGGS_THROWN      (30, "eggs_thrown"),
    CHICKEN_HATCHES  (31, "chicken_hatches"),
    ENDER_PEARLS     (32, "ender_pearls"),
    ANIMALS_BRED     (33, "animals_bred"),
    TNT_DETONATED    (34, "tnt_detonated"),
    ENCHANTS_DONE    (35, "enchants_done"),
    HIGHEST_LEVEL    (36, "highest_level"),   ///
    DAMAGE_DEALT     (37, "damage_dealt"),    ///
    ITEMS_BREWED     (38, "items_brewed"),
    ITEMS_COOKED     (39, "items_cooked"),
    FIRES_STARTED    (40, "fires_started"),
    MINED            (41, "mined"),           //
    TAB_COMPLETE     (42, "tab_complete"),    ///
    EATING           (43, "eating"),
    SHEARING         (44, "shearing");

    public final int id;
    public final String title;

    StatTypes(int id, String title) {

        this.id = id;
        this.title = title;
    }

}
