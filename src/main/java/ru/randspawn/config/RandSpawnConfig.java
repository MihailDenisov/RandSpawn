package ru.randspawn.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class RandSpawnConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CENTER_X;
    public static final ModConfigSpec.IntValue CENTER_Z;
    public static final ModConfigSpec.IntValue MIN_RADIUS;
    public static final ModConfigSpec.IntValue MAX_RADIUS;
    public static final ModConfigSpec.IntValue MIN_DISTANCE;
    public static final ModConfigSpec.IntValue MIN_Y;
    public static final ModConfigSpec.IntValue MAX_Y;
    public static final ModConfigSpec.BooleanValue REQUIRE_OPEN_SKY;
    public static final ModConfigSpec.IntValue MAX_SEARCH_ATTEMPTS;
    public static final ModConfigSpec.ConfigValue<String> DIMENSION;
    public static final ModConfigSpec.BooleanValue AUTOMATIC_ASSIGNMENT;
    public static final ModConfigSpec.IntValue CLAIM_WINDOW_MINUTES;
    public static final ModConfigSpec.IntValue LOCAL_MIN_X;
    public static final ModConfigSpec.IntValue LOCAL_MAX_X;
    public static final ModConfigSpec.IntValue LOCAL_MIN_Y;
    public static final ModConfigSpec.IntValue LOCAL_MAX_Y;
    public static final ModConfigSpec.IntValue LOCAL_MIN_Z;
    public static final ModConfigSpec.IntValue LOCAL_MAX_Z;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_BLOCKS;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("spawn");
        CENTER_X = BUILDER.comment("Search center X.").defineInRange("centerX", 0, -30_000_000, 30_000_000);
        CENTER_Z = BUILDER.comment("Search center Z.").defineInRange("centerZ", 0, -30_000_000, 30_000_000);
        MIN_RADIUS = BUILDER.comment("Minimum radius from the center.").defineInRange("minRadius", 100, 0, 30_000_000);
        MAX_RADIUS = BUILDER.comment("Maximum radius from the center.").defineInRange("maxRadius", 5_000, 1, 30_000_000);
        MIN_DISTANCE = BUILDER.comment("Minimum horizontal distance between personal spawns.").defineInRange("minDistanceBetweenSpawns", 250, 0, 30_000_000);
        MIN_Y = BUILDER.comment("Minimum accepted surface Y.").defineInRange("minY", -64, -2048, 2048);
        MAX_Y = BUILDER.comment("Maximum accepted surface Y.").defineInRange("maxY", 320, -2048, 2048);
        REQUIRE_OPEN_SKY = BUILDER.comment("Require sky access while choosing a new spawn.").define("requireOpenSky", true);
        MAX_SEARCH_ATTEMPTS = BUILDER.comment("Maximum generated candidates per search.").defineInRange("maxSearchAttempts", 500, 1, 100_000);
        DIMENSION = BUILDER.comment("Dimension for personal spawns.").define("dimension", "minecraft:overworld");
        BUILDER.pop();

        BUILDER.push("assignment");
        AUTOMATIC_ASSIGNMENT = BUILDER.comment(
                "When true, first login assigns and teleports to a personal spawn.",
                "When false, first login opens a persistent one-time /randspawn claim window.")
                .define("automatic", true);
        CLAIM_WINDOW_MINUTES = BUILDER.comment(
                "Wall-clock minutes in which a player can claim after first login in claim mode.")
                .defineInRange("claimWindowMinutes", 15, 1, 10_080);
        BUILDER.pop();

        BUILDER.push("localRepair");
        LOCAL_MIN_X = localOffset("minOffsetX", -16);
        LOCAL_MAX_X = localOffset("maxOffsetX", 16);
        LOCAL_MIN_Y = localOffset("minOffsetY", -16);
        LOCAL_MAX_Y = localOffset("maxOffsetY", 16);
        LOCAL_MIN_Z = localOffset("minOffsetZ", -16);
        LOCAL_MAX_Z = localOffset("maxOffsetZ", 16);
        BUILDER.pop();

        BUILDER.push("safety");
        BLOCKED_BLOCKS = BUILDER.comment("Blocks that cannot be used as spawn ground or occupy player space.")
                .defineListAllowEmpty("blockedBlocks", List.of(
                        "minecraft:magma_block", "minecraft:cactus", "minecraft:campfire",
                        "minecraft:soul_campfire", "minecraft:fire", "minecraft:soul_fire",
                        "minecraft:powder_snow"), () -> "minecraft:air", value -> value instanceof String);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private static ModConfigSpec.IntValue localOffset(String name, int defaultValue) {
        return BUILDER.comment("Inclusive offset from the immutable personal-spawn anchor.")
                .defineInRange(name, defaultValue, -64, 64);
    }

    private RandSpawnConfig() {
    }
}
