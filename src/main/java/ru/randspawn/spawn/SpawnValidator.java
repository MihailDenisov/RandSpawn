package ru.randspawn.spawn;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import ru.randspawn.config.RandSpawnConfig;

public final class SpawnValidator {
    public static boolean isCandidateSafe(ServerLevel level, BlockPos feet) {
        if (!isInsideAllowedBounds(level, feet)) {
            return false;
        }
        return isExistingSpawnSafe(level, feet)
                && (!RandSpawnConfig.REQUIRE_OPEN_SKY.get() || level.canSeeSky(feet));
    }

    public static boolean isLocalRepairSafe(ServerLevel level, BlockPos feet) {
        return isInsideAllowedBounds(level, feet) && isExistingSpawnSafe(level, feet);
    }

    public static boolean isExistingSpawnSafe(ServerLevel level, BlockPos feet) {
        BlockPos ground = feet.below();
        if (level.isOutsideBuildHeight(ground) || level.isOutsideBuildHeight(feet)
                || level.isOutsideBuildHeight(feet.above()) || !level.getWorldBorder().isWithinBounds(feet)) {
            return false;
        }
        BlockState groundState = level.getBlockState(ground);
        VoxelShape groundShape = groundState.getCollisionShape(level, ground);
        if (groundShape.isEmpty() || !Block.isFaceFull(groundShape, Direction.UP)) {
            return false;
        }
        if (!level.getFluidState(ground).isEmpty() || isBlocked(groundState)) {
            return false;
        }
        return isFree(level, feet) && isFree(level, feet.above());
    }

    private static boolean isInsideAllowedBounds(ServerLevel level, BlockPos feet) {
        return feet.getY() >= RandSpawnConfig.MIN_Y.get()
                && feet.getY() <= RandSpawnConfig.MAX_Y.get()
                && !level.isOutsideBuildHeight(feet)
                && !level.isOutsideBuildHeight(feet.above())
                && !level.isOutsideBuildHeight(feet.below())
                && level.getWorldBorder().isWithinBounds(feet);
    }

    private static boolean isFree(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !isBlocked(state)
                && state.getCollisionShape(level, pos).isEmpty()
                && level.getFluidState(pos).isEmpty();
    }

    private static boolean isBlocked(BlockState state) {
        Set<Block> blocked = new HashSet<>();
        for (String id : RandSpawnConfig.BLOCKED_BLOCKS.get()) {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location != null) {
                BuiltInRegistries.BLOCK.getOptional(location).ifPresent(blocked::add);
            }
        }
        return blocked.contains(state.getBlock());
    }

    private SpawnValidator() {
    }
}
