package wav.demon.StatCraft;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public class Util {

    public static byte[] UUIDToByte(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static UUID byteToUUID(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    /**
     * Returns the damage value to be placed in the database for a specific id. This returns the given damage
     * value for any id where the damage value represents a different distinct block or item. In all other cases,
     * 0 is returned. Blocks or items whose damage value represents orientation, or lava/water level return 0. For
     * blocks whose damage value determines both the block type <i>and</i> orientation are normalized to a standard
     * value for representing that block type in the database. So far only the block type <i>log</i> and <i>log2</i>
     * fall under this category (id's 17 and 162 respectively).
     *
     * @param id The id of the block or item to be normalized
     * @param damage The given damage value for this block or item
     * @return The normalized value to be placed into the database
     */
    public static short damageValue(short id, short damage) {
        switch (id) {
            case 1:     // Stone
            case 3:     // Dirt
            case 5:     // Wooden Planks
            case 6:     // Saplings
            case 12:    // Sand
            case 18:    // Leaves
            case 19:    // Sponges
            case 24:    // Sandstone
            case 31:    // Tall Grass
            case 35:    // Wool
            case 38:    // Flowers
            case 43:    // Double Slabs
            case 44:    // Slabs
            case 95:    // Stained Glass
            case 97:    // Monster Egg Blocks
            case 98:    // Stone Bricks
            case 125:   // Double New Wooden Slabs
            case 126:   // New Wooden Slabs
            case 139:   // Cobblestone Walls
            case 144:   // Various Head Blocks
            case 155:   // Quartz Blocks
            case 159:   // Stained Clay
            case 160:   // Stained Glass Panes
            case 161:   // New LEaves
            case 168:   // Prismarine Blocks
            case 171:   // Carpet
            case 175:   // Various Plants
            case 179:   // Red Sandstone
            case 263:   // Coal and Charcoal
            case 349:   // Various Raw Fish
            case 350:   // Various Cooked Fish
            case 351:   // Dyes
            case 373:   // All Potions
            case 383:   // Mob Spawn Eggs
            case 397:   // Various Heads
            case 425:   // Colored Banners
                return damage;
            case 17:    // Wood
            case 162:   // New Wood
                return (short) ((damage >= 8) ? (damage - 8) : (damage >= 4) ? (damage - 4) : damage);
            default:
                return 0;
        }
    }

    /**
     * Transform time held in seconds to human readable time
     *
     * @param seconds Length of time interval in seconds
     * @return String of time in a more human readable format, for example 219 seconds would read as "3 minutes, 39 seconds"
     */
    public static String transformTime(int seconds) {
        // Figure out the playtime in a human readable format
        final int secondsInMinute = 60;
        final int secondsInHour = 60 * secondsInMinute;
        final int secondsInDay = 24 * secondsInHour;
        final int secondsInWeek = 7 * secondsInDay;

        final int weeks = seconds / secondsInWeek;

        final int daySeconds = seconds % secondsInWeek;
        final int days = daySeconds / secondsInDay;

        final int hourSeconds = daySeconds % secondsInDay;
        final int hours = hourSeconds / secondsInHour;

        final int minuteSeconds = hourSeconds % secondsInHour;
        final int minutes = minuteSeconds / secondsInMinute;

        final int remainingSeconds = minuteSeconds % secondsInMinute;

        // Make some strings
        String weekString;
        String dayString;
        String hourString;
        String minuteString;
        String secondString;

        // Use correct grammar, and don't use it if it's zero
        if (weeks == 1)
            weekString = weeks + " week";
        else if (weeks == 0)
            weekString = "";
        else
            weekString = weeks + " weeks";

        if (days == 1)
            dayString = days + " day";
        else if (days == 0)
            dayString = "";
        else
            dayString = days + " days";

        if (hours == 1)
            hourString = hours + " hour";
        else if (hours == 0)
            hourString = "";
        else
            hourString = hours + " hours";

        if (minutes == 1)
            minuteString = minutes + " minute";
        else if (minutes == 0)
            minuteString = "";
        else
            minuteString = minutes + " minutes";

        if (remainingSeconds == 1)
            secondString = remainingSeconds + " second";
        else if (remainingSeconds == 0)
            secondString = "";
        else
            secondString = remainingSeconds + " seconds";

        ArrayList<String> results = new ArrayList<>();
        results.add(weekString);
        results.add(dayString);
        results.add(hourString);
        results.add(minuteString);
        results.add(secondString);

        for (int x = results.size() - 1; x >= 0; x--) {
            if (results.get(x).equals("")) {
                results.remove(x);
            }
        }

        if (results.size() == 0)
            return "0 seconds";

        String finalResult = "";
        for (int x = 0; x < results.size(); x++) {
            if (x == results.size() - 1) {
                if (x == 0)
                    finalResult = results.get(x);
                else
                    finalResult = finalResult + ", " + results.get(x);
            } else {
                if (x == 0)
                    finalResult = results.get(x);
                else
                    finalResult = finalResult + ", " + results.get(x);
            }
        }

        return finalResult;
    }
}
