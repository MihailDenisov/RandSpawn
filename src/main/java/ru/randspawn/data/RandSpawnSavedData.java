package ru.randspawn.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public final class RandSpawnSavedData extends SavedData {
    private static final String FILE_NAME = "randspawn_spawns";
    private static final int DATA_VERSION = 2;
    private final Map<UUID, PlayerSpawnData> spawns = new HashMap<>();
    private final Map<UUID, ClaimWindow> claims = new HashMap<>();

    public static RandSpawnSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(RandSpawnSavedData::new, RandSpawnSavedData::load, DataFixTypes.LEVEL), FILE_NAME);
    }

    public static RandSpawnSavedData load(CompoundTag root, HolderLookup.Provider registries) {
        RandSpawnSavedData data = new RandSpawnSavedData();
        ListTag players = root.getList("players", Tag.TAG_COMPOUND);
        for (Tag tag : players) {
            CompoundTag player = (CompoundTag) tag;
            try {
                UUID uuid = player.getUUID("uuid");
                String nickname = player.getString("nickname");
                ResourceLocation dimensionId = ResourceLocation.parse(player.getString("dimension"));
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
                BlockPos position = new BlockPos(player.getInt("x"), player.getInt("y"), player.getInt("z"));
                BlockPos anchor = player.contains("anchorX", Tag.TAG_INT)
                        && player.contains("anchorY", Tag.TAG_INT)
                        && player.contains("anchorZ", Tag.TAG_INT)
                        ? new BlockPos(player.getInt("anchorX"), player.getInt("anchorY"), player.getInt("anchorZ"))
                        : position;
                data.spawns.put(uuid, new PlayerSpawnData(uuid, nickname, dimension, position, anchor));
            } catch (RuntimeException ignored) {
                // Skip a malformed record without losing the remaining players.
            }
        }
        ListTag claims = root.getList("claims", Tag.TAG_COMPOUND);
        for (Tag tag : claims) {
            CompoundTag claim = (CompoundTag) tag;
            try {
                UUID uuid = claim.getUUID("uuid");
                data.claims.put(uuid, new ClaimWindow(claim.getLong("deadlineEpochMillis"), claim.getBoolean("consumed")));
            } catch (RuntimeException ignored) {
                // Skip a malformed claim without losing the remaining players.
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag root, HolderLookup.Provider registries) {
        ListTag players = new ListTag();
        for (PlayerSpawnData spawn : spawns.values()) {
            CompoundTag player = new CompoundTag();
            player.putUUID("uuid", spawn.uuid());
            player.putString("nickname", spawn.nickname());
            player.putString("dimension", spawn.dimension().location().toString());
            player.putInt("x", spawn.position().getX());
            player.putInt("y", spawn.position().getY());
            player.putInt("z", spawn.position().getZ());
            player.putInt("anchorX", spawn.anchor().getX());
            player.putInt("anchorY", spawn.anchor().getY());
            player.putInt("anchorZ", spawn.anchor().getZ());
            players.add(player);
        }
        ListTag claimList = new ListTag();
        for (Map.Entry<UUID, ClaimWindow> entry : claims.entrySet()) {
            CompoundTag claim = new CompoundTag();
            claim.putUUID("uuid", entry.getKey());
            claim.putLong("deadlineEpochMillis", entry.getValue().deadlineEpochMillis());
            claim.putBoolean("consumed", entry.getValue().consumed());
            claimList.add(claim);
        }
        root.putInt("dataVersion", DATA_VERSION);
        root.put("players", players);
        root.put("claims", claimList);
        return root;
    }

    public Optional<PlayerSpawnData> get(UUID uuid) {
        return Optional.ofNullable(spawns.get(uuid));
    }

    public Optional<PlayerSpawnData> findByNickname(String nickname) {
        return spawns.values().stream().filter(value -> value.nickname().equalsIgnoreCase(nickname)).findFirst();
    }

    public Collection<PlayerSpawnData> all() {
        return Collections.unmodifiableCollection(spawns.values());
    }

    public Optional<ClaimWindow> claimWindow(UUID uuid) {
        return Optional.ofNullable(claims.get(uuid));
    }

    public ClaimWindow ensureClaimWindow(UUID uuid, long nowEpochMillis, long durationMillis) {
        ClaimWindow existing = claims.get(uuid);
        if (existing != null) {
            return existing;
        }
        ClaimWindow created = new ClaimWindow(nowEpochMillis + durationMillis, false);
        claims.put(uuid, created);
        setDirty();
        return created;
    }

    public boolean completeClaim(PlayerSpawnData spawn, long claimTimeEpochMillis) {
        ClaimWindow claim = claims.get(spawn.uuid());
        if (claim == null || !claim.isAvailableAt(claimTimeEpochMillis) || spawns.containsKey(spawn.uuid())) {
            return false;
        }
        spawns.put(spawn.uuid(), spawn);
        claims.put(spawn.uuid(), claim.consume());
        setDirty();
        return true;
    }

    public void put(PlayerSpawnData spawn) {
        spawns.put(spawn.uuid(), spawn);
        setDirty();
    }

    public boolean remove(UUID uuid) {
        if (spawns.remove(uuid) == null) {
            return false;
        }
        setDirty();
        return true;
    }
}
