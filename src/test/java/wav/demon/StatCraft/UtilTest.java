package wav.demon.StatCraft;

import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;

public class UtilTest {

    final static UUID uuid = UUID.fromString("c43d2930-22aa-40f4-aca6-82e6540044cc");
    final static byte[] array = new byte[]{-60, 61, 41, 48, 34, -86, 64, -12, -84, -90, -126, -26, 84, 0, 68, -52};

    @Test
    public void testUUIDToByte() {
        assertTrue(Arrays.equals(array, Util.UUIDToByte(uuid)));
    }

    @Test
    public void testByteToUUID() {
        assertTrue(uuid.equals(Util.byteToUUID(array)));
    }

    @Test
    public void testTransformTime() {
        assertEquals("3 minutes, 39 seconds", Util.transformTime(219));
    }

    @Test
    public void testBiggerTransformTime() {
        assertEquals("12 hours, 9 minutes, 49 seconds", Util.transformTime(43789));
    }

    @Test
    public void testBiggestTransformTime() {
        assertEquals("452 weeks, 5 days, 11 hours, 19 minutes, 54 seconds", Util.transformTime(273842394));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeTransformTime() {
        Util.transformTime(-1);
    }

    @Test
    public void testDistanceUnits0m() {
        assertEquals("0.00 m", Util.distanceUnits(0));
    }

    @Test
    public void testDistanceUnits100point65m() {
        assertEquals("100.65 m", Util.distanceUnits(10065));
    }

    @Test
    public void testDistanceUnits547point23km() {
        assertEquals("547.23 km", Util.distanceUnits(54723000));
    }
}
