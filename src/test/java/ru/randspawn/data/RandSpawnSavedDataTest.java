package ru.randspawn.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class RandSpawnSavedDataTest {
    private static final HolderLookup.Provider EMPTY_REGISTRIES = HolderLookup.Provider.create(Stream.empty());

    @Test
    void nicknameUpdateKeepsOneUuidRecordAndRoundTripsCoordinates() {
        UUID uuid = UUID.fromString("8bb978a7-a7ee-4acf-9b68-42910b3b4f98");
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.parse("minecraft:overworld"));
        BlockPos position = new BlockPos(123, 70, -456);
        PlayerSpawnData original = new PlayerSpawnData(uuid, "OldName", dimension, position);
        RandSpawnSavedData data = new RandSpawnSavedData();

        data.put(original);
        data.put(original.withNickname("NewName"));

        assertEquals(1, data.all().size());
        assertEquals(position, data.get(uuid).orElseThrow().position());

        CompoundTag serialized = data.save(new CompoundTag(), EMPTY_REGISTRIES);
        PlayerSpawnData restored = RandSpawnSavedData.load(serialized, EMPTY_REGISTRIES).get(uuid).orElseThrow();
        assertEquals("NewName", restored.nickname());
        assertEquals(dimension, restored.dimension());
        assertEquals(position, restored.position());
        assertEquals(position, restored.anchor());
    }

    @Test
    void anchorAndClaimStateRoundTripWithoutMovingTheAnchor() {
        UUID uuid = UUID.fromString("17140380-bb80-4d18-8179-23b289dd5762");
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.parse("minecraft:overworld"));
        BlockPos anchor = new BlockPos(100, 70, 100);
        PlayerSpawnData original = new PlayerSpawnData(uuid, "Player", dimension, anchor, anchor);
        PlayerSpawnData repaired = original.withPosition(new BlockPos(101, 72, 100));
        assertEquals(anchor, repaired.anchor());
        RandSpawnSavedData data = new RandSpawnSavedData();
        data.ensureClaimWindow(uuid, 1_000L, 15 * 60_000L);
        assertTrue(data.completeClaim(repaired, 2_000L));

        RandSpawnSavedData restored = RandSpawnSavedData.load(
                data.save(new CompoundTag(), EMPTY_REGISTRIES), EMPTY_REGISTRIES);

        assertEquals(anchor, restored.get(uuid).orElseThrow().anchor());
        assertEquals(repaired.position(), restored.get(uuid).orElseThrow().position());
        assertTrue(restored.claimWindow(uuid).orElseThrow().consumed());
    }

    @Test
    void failedOrExpiredClaimDoesNotConsumeWindowOrCreateSpawn() {
        UUID uuid = UUID.fromString("fc099535-e5a7-41a4-b52f-c1741ec33891");
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.parse("minecraft:overworld"));
        PlayerSpawnData candidate = new PlayerSpawnData(uuid, "Player", dimension, new BlockPos(0, 64, 0));
        RandSpawnSavedData data = new RandSpawnSavedData();
        data.ensureClaimWindow(uuid, 1_000L, 1_000L);

        assertFalse(data.completeClaim(candidate, 2_001L));
        assertTrue(data.claimWindow(uuid).orElseThrow().isAvailableAt(1_500L));
        assertTrue(data.get(uuid).isEmpty());
        assertTrue(data.completeClaim(candidate, 1_500L));
    }

    @Test
    void legacySpawnWithoutAnchorMigratesAnchorFromPosition() {
        UUID uuid = UUID.fromString("6c942e0d-f99c-46bb-b6c0-f2d982ecdb91");
        CompoundTag player = new CompoundTag();
        player.putUUID("uuid", uuid);
        player.putString("nickname", "Legacy");
        player.putString("dimension", "minecraft:overworld");
        player.putInt("x", 7);
        player.putInt("y", 80);
        player.putInt("z", -9);
        net.minecraft.nbt.ListTag players = new net.minecraft.nbt.ListTag();
        players.add(player);
        CompoundTag root = new CompoundTag();
        root.put("players", players);

        PlayerSpawnData migrated = RandSpawnSavedData.load(root, EMPTY_REGISTRIES).get(uuid).orElseThrow();

        assertEquals(migrated.position(), migrated.anchor());
    }
}
