package ru.randspawn.spawn;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class SpawnMathTest {
    @Test
    void sampledPointsStayInsideAnnulusWithinRoundingTolerance() {
        Random random = new Random(42L);
        for (int index = 0; index < 10_000; index++) {
            SpawnMath.Offset offset = SpawnMath.sampleAnnulus(random, 100, 1_000);
            double radius = Math.hypot(offset.x(), offset.z());
            assertTrue(radius >= 99.0 && radius <= 1_001.0);
        }
    }

    @Test
    void horizontalDistanceUsesInclusiveBoundary() {
        assertFalse(SpawnMath.farEnough(0, 0, 3, 4, 6));
        assertTrue(SpawnMath.farEnough(0, 0, 3, 4, 5));
    }

    @Test
    void localOffsetsIncludeEveryDefaultPositionAndUseDeterministicDistanceOrder() {
        var offsets = SpawnMath.orderedLocalOffsets(-16, 16, -16, 16, -16, 16);

        assertEquals(33 * 33 * 33, offsets.size());
        assertEquals(new SpawnMath.Offset3d(0, 0, 0), offsets.getFirst());
        assertEquals(new SpawnMath.Offset3d(0, -1, 0), offsets.get(1));
        assertEquals(new SpawnMath.Offset3d(-1, 0, 0), offsets.get(2));
        assertEquals(new SpawnMath.Offset3d(0, 0, -1), offsets.get(3));
        assertTrue(offsets.contains(new SpawnMath.Offset3d(0, 16, 0)));
        assertTrue(offsets.contains(new SpawnMath.Offset3d(0, -16, 0)));
    }

    @Test
    void reversedLocalBoundsAreNormalizedAndInclusive() {
        var offsets = SpawnMath.orderedLocalOffsets(1, -1, 2, 0, 0, 0);

        assertEquals(9, offsets.size());
        assertTrue(offsets.contains(new SpawnMath.Offset3d(-1, 0, 0)));
        assertTrue(offsets.contains(new SpawnMath.Offset3d(1, 2, 0)));
    }
}
