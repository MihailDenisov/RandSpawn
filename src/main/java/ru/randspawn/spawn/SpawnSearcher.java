package ru.randspawn.spawn;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import ru.randspawn.config.RandSpawnConfig;
import ru.randspawn.data.PlayerSpawnData;
import ru.randspawn.data.RandSpawnSavedData;

public final class SpawnSearcher {
    public static Optional<BlockPos> find(ServerLevel level, RandSpawnSavedData data, UUID excludedUuid) {
        int minRadius = Math.min(RandSpawnConfig.MIN_RADIUS.get(), RandSpawnConfig.MAX_RADIUS.get());
        int maxRadius = Math.max(RandSpawnConfig.MIN_RADIUS.get(), RandSpawnConfig.MAX_RADIUS.get());
        for (int attempt = 0; attempt < RandSpawnConfig.MAX_SEARCH_ATTEMPTS.get(); attempt++) {
            SpawnMath.Offset offset = SpawnMath.sampleAnnulus(ThreadLocalRandom.current(), minRadius, maxRadius);
            int x = RandSpawnConfig.CENTER_X.get() + offset.x();
            int z = RandSpawnConfig.CENTER_Z.get() + offset.z();
            if (!hasEnoughDistance(x, z, data, excludedUuid)) {
                continue;
            }
            level.getChunk(x >> 4, z >> 4);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos feet = new BlockPos(x, y, z);
            if (SpawnValidator.isCandidateSafe(level, feet)) {
                return Optional.of(feet);
            }
        }
        return Optional.empty();
    }

    public static Optional<BlockPos> findNearAnchor(ServerLevel level, BlockPos anchor,
            RandSpawnSavedData data, UUID excludedUuid) {
        for (SpawnMath.Offset3d offset : SpawnMath.orderedLocalOffsets(
                RandSpawnConfig.LOCAL_MIN_X.get(), RandSpawnConfig.LOCAL_MAX_X.get(),
                RandSpawnConfig.LOCAL_MIN_Y.get(), RandSpawnConfig.LOCAL_MAX_Y.get(),
                RandSpawnConfig.LOCAL_MIN_Z.get(), RandSpawnConfig.LOCAL_MAX_Z.get())) {
            BlockPos candidate = anchor.offset(offset.x(), offset.y(), offset.z());
            if (!hasEnoughDistance(candidate.getX(), candidate.getZ(), data, excludedUuid)) {
                continue;
            }
            level.getChunk(candidate.getX() >> 4, candidate.getZ() >> 4);
            if (SpawnValidator.isLocalRepairSafe(level, candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private static boolean hasEnoughDistance(int x, int z, RandSpawnSavedData data, UUID excludedUuid) {
        int minimum = RandSpawnConfig.MIN_DISTANCE.get();
        for (PlayerSpawnData spawn : data.all()) {
            if (spawn.uuid().equals(excludedUuid)) {
                continue;
            }
            if (!SpawnMath.farEnough(x, z, spawn.position().getX(), spawn.position().getZ(), minimum)) {
                return false;
            }
        }
        return true;
    }

    private SpawnSearcher() {
    }
}
