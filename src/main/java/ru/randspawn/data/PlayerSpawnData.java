package ru.randspawn.data;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record PlayerSpawnData(UUID uuid, String nickname, ResourceKey<Level> dimension, BlockPos position,
        BlockPos anchor) {
    public PlayerSpawnData(UUID uuid, String nickname, ResourceKey<Level> dimension, BlockPos position) {
        this(uuid, nickname, dimension, position, position);
    }

    public PlayerSpawnData withNickname(String newNickname) {
        return new PlayerSpawnData(uuid, newNickname, dimension, position, anchor);
    }

    public PlayerSpawnData withPosition(BlockPos newPosition) {
        return new PlayerSpawnData(uuid, nickname, dimension, newPosition, anchor);
    }
}
