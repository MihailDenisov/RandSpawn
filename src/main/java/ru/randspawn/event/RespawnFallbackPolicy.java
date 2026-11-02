package ru.randspawn.event;

public final class RespawnFallbackPolicy {
    public static boolean shouldUseRandSpawn(boolean fromEndFight, boolean hasConfiguredVanillaSpawn,
            boolean missingRespawnBlock) {
        return !fromEndFight && (!hasConfiguredVanillaSpawn || missingRespawnBlock);
    }

    private RespawnFallbackPolicy() {
    }
}
