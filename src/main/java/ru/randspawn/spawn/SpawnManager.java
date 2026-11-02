package ru.randspawn.spawn;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import ru.randspawn.RandSpawn;
import ru.randspawn.config.RandSpawnConfig;
import ru.randspawn.data.PlayerSpawnData;
import ru.randspawn.data.RandSpawnSavedData;

public final class SpawnManager {
    public enum ClaimResult {
        SUCCESS,
        NOT_AVAILABLE,
        SEARCH_FAILED
    }

    public static Optional<PlayerSpawnData> getOrCreate(ServerPlayer player) {
        RandSpawnSavedData data = RandSpawnSavedData.get(player.getServer());
        Optional<PlayerSpawnData> existing = data.get(player.getUUID());
        if (existing.isPresent()) {
            PlayerSpawnData spawn = existing.get();
            String currentName = player.getGameProfile().getName();
            if (!spawn.nickname().equals(currentName)) {
                spawn = spawn.withNickname(currentName);
                data.put(spawn);
            }
            return Optional.of(spawn);
        }
        return create(player.getServer(), player.getUUID(), player.getGameProfile().getName());
    }

    public static Optional<PlayerSpawnData> create(MinecraftServer server, UUID uuid, String nickname) {
        RandSpawnSavedData data = RandSpawnSavedData.get(server);
        Optional<PlayerSpawnData> spawn = find(server, data, uuid, nickname, null);
        spawn.ifPresent(data::put);
        return spawn;
    }

    private static Optional<PlayerSpawnData> find(MinecraftServer server, RandSpawnSavedData data, UUID uuid,
            String nickname, UUID excludedUuid) {
        ResourceLocation id = ResourceLocation.tryParse(RandSpawnConfig.DIMENSION.get());
        if (id == null) {
            RandSpawn.LOGGER.error("Invalid RandSpawn dimension: {}", RandSpawnConfig.DIMENSION.get());
            return Optional.empty();
        }
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, id);
        ServerLevel level = server.getLevel(key);
        if (level == null) {
            RandSpawn.LOGGER.error("RandSpawn dimension is not loaded: {}", id);
            return Optional.empty();
        }
        Optional<BlockPos> position = SpawnSearcher.find(level, data, excludedUuid);
        if (position.isEmpty()) {
            RandSpawn.LOGGER.warn("No safe RandSpawn found for {} after {} attempts", nickname,
                    RandSpawnConfig.MAX_SEARCH_ATTEMPTS.get());
            return Optional.empty();
        }
        return Optional.of(new PlayerSpawnData(uuid, nickname, key, position.get()));
    }

    public static Optional<PlayerSpawnData> regenerate(MinecraftServer server, UUID uuid, String nickname) {
        RandSpawnSavedData data = RandSpawnSavedData.get(server);
        Optional<PlayerSpawnData> replacement = find(server, data, uuid, nickname, uuid);
        replacement.ifPresent(data::put);
        return replacement;
    }

    public static Optional<PlayerSpawnData> repairNearAnchor(MinecraftServer server, PlayerSpawnData original) {
        RandSpawnSavedData data = RandSpawnSavedData.get(server);
        ServerLevel level = server.getLevel(original.dimension());
        if (level == null) {
            return Optional.empty();
        }
        Optional<BlockPos> position = SpawnSearcher.findNearAnchor(level, original.anchor(), data, original.uuid());
        if (position.isEmpty()) {
            return Optional.empty();
        }
        PlayerSpawnData repaired = original.withPosition(position.get());
        data.put(repaired);
        return Optional.of(repaired);
    }

    public static ClaimResult claim(ServerPlayer player, long claimTimeEpochMillis) {
        RandSpawnSavedData data = RandSpawnSavedData.get(player.getServer());
        if (data.claimWindow(player.getUUID()).filter(claim -> claim.isAvailableAt(claimTimeEpochMillis)).isEmpty()
                || data.get(player.getUUID()).isPresent()) {
            return ClaimResult.NOT_AVAILABLE;
        }
        Optional<PlayerSpawnData> spawn = find(player.getServer(), data, player.getUUID(),
                player.getGameProfile().getName(), null);
        if (spawn.isEmpty()) {
            return ClaimResult.SEARCH_FAILED;
        }
        if (!data.completeClaim(spawn.get(), claimTimeEpochMillis)) {
            return ClaimResult.NOT_AVAILABLE;
        }
        teleport(player, spawn.get());
        return ClaimResult.SUCCESS;
    }

    public static void teleport(ServerPlayer player, PlayerSpawnData spawn) {
        ServerLevel level = player.getServer().getLevel(spawn.dimension());
        if (level != null) {
            BlockPos pos = spawn.position();
            player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    Set.of(), player.getYRot(), player.getXRot());
        }
    }

    private SpawnManager() {
    }
}
