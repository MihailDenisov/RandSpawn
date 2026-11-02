package ru.randspawn.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import ru.randspawn.data.PlayerSpawnData;
import ru.randspawn.data.RandSpawnSavedData;
import ru.randspawn.config.RandSpawnConfig;
import ru.randspawn.spawn.SpawnManager;
import ru.randspawn.spawn.SpawnValidator;

public final class PlayerEventHandler {
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        RandSpawnSavedData data = RandSpawnSavedData.get(player.getServer());
        PlayerSpawnData existing = data.get(player.getUUID()).orElse(null);
        if (existing != null) {
            String currentName = player.getGameProfile().getName();
            if (!existing.nickname().equals(currentName)) {
                data.put(existing.withNickname(currentName));
            }
            return;
        }
        if (RandSpawnConfig.AUTOMATIC_ASSIGNMENT.get()) {
            SpawnManager.create(player.getServer(), player.getUUID(), player.getGameProfile().getName())
                    .ifPresent(spawn -> SpawnManager.teleport(player, spawn));
            return;
        }
        if (data.claimWindow(player.getUUID()).isEmpty()) {
            long durationMillis = RandSpawnConfig.CLAIM_WINDOW_MINUTES.get() * 60_000L;
            data.ensureClaimWindow(player.getUUID(), System.currentTimeMillis(), durationMillis);
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "command.randspawn.claim.opened", RandSpawnConfig.CLAIM_WINDOW_MINUTES.get()));
        }
    }

    public static void onRespawnPosition(PlayerRespawnPositionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        DimensionTransition vanilla = event.getOriginalDimensionTransition();
        // A null vanilla respawn position means the player has no bed/anchor or
        // /spawnpoint at all, so RandSpawn must also handle that normal case.
        if (!RespawnFallbackPolicy.shouldUseRandSpawn(event.isFromEndFight(), player.getRespawnPosition() != null,
                vanilla.missingRespawnBlock())) {
            return;
        }
        PlayerSpawnData spawn = RandSpawnSavedData.get(player.getServer()).get(player.getUUID()).orElse(null);
        if (spawn == null) {
            return;
        }
        ServerLevel level = player.getServer().getLevel(spawn.dimension());
        if (level == null) {
            return;
        }
        BlockPos position = spawn.position();
        if (!SpawnValidator.isExistingSpawnSafe(level, position)) {
            spawn = SpawnManager.repairNearAnchor(player.getServer(), spawn).orElse(null);
            if (spawn == null) {
                return;
            }
            level = player.getServer().getLevel(spawn.dimension());
            position = spawn.position();
        }
        event.setDimensionTransition(new DimensionTransition(level, Vec3.atBottomCenterOf(position), Vec3.ZERO,
                player.getYRot(), 0.0F, false, DimensionTransition.DO_NOTHING));
    }

    private PlayerEventHandler() {
    }
}
