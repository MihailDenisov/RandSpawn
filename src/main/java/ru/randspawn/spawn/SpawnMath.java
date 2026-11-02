package ru.randspawn.spawn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.random.RandomGenerator;

public final class SpawnMath {
    public record Offset(int x, int z) {
    }

    public record Offset3d(int x, int y, int z) {
        long distanceSquared() {
            return (long) x * x + (long) y * y + (long) z * z;
        }
    }

    public static Offset sampleAnnulus(RandomGenerator random, int minRadius, int maxRadius) {
        long minSquared = (long) minRadius * minRadius;
        long maxSquared = (long) maxRadius * maxRadius;
        double radius = Math.sqrt(minSquared + random.nextDouble() * (maxSquared - minSquared));
        double angle = random.nextDouble() * Math.PI * 2.0;
        return new Offset((int) Math.round(Math.cos(angle) * radius), (int) Math.round(Math.sin(angle) * radius));
    }

    public static boolean farEnough(int firstX, int firstZ, int secondX, int secondZ, int minimumDistance) {
        long dx = (long) firstX - secondX;
        long dz = (long) firstZ - secondZ;
        long minimumSquared = (long) minimumDistance * minimumDistance;
        return dx * dx + dz * dz >= minimumSquared;
    }

    public static List<Offset3d> orderedLocalOffsets(int firstX, int lastX, int firstY, int lastY,
            int firstZ, int lastZ) {
        int minX = Math.min(firstX, lastX);
        int maxX = Math.max(firstX, lastX);
        int minY = Math.min(firstY, lastY);
        int maxY = Math.max(firstY, lastY);
        int minZ = Math.min(firstZ, lastZ);
        int maxZ = Math.max(firstZ, lastZ);
        List<Offset3d> offsets = new ArrayList<>((maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1));
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    offsets.add(new Offset3d(x, y, z));
                }
            }
        }
        offsets.sort(Comparator.comparingLong(Offset3d::distanceSquared)
                .thenComparingInt(Offset3d::y)
                .thenComparingInt(Offset3d::x)
                .thenComparingInt(Offset3d::z));
        return offsets;
    }

    private SpawnMath() {
    }
}
