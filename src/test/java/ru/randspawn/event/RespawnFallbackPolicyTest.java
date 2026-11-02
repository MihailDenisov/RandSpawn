package ru.randspawn.event;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RespawnFallbackPolicyTest {
    @Test
    void validConfiguredVanillaSpawnKeepsPriority() {
        assertFalse(RespawnFallbackPolicy.shouldUseRandSpawn(false, true, false));
    }

    @Test
    void absentVanillaSpawnUsesRandSpawn() {
        assertTrue(RespawnFallbackPolicy.shouldUseRandSpawn(false, false, false));
    }

    @Test
    void destroyedRespawnBlockUsesRandSpawn() {
        assertTrue(RespawnFallbackPolicy.shouldUseRandSpawn(false, true, true));
    }

    @Test
    void endFightReturnIsNeverOverridden() {
        assertFalse(RespawnFallbackPolicy.shouldUseRandSpawn(true, false, false));
        assertFalse(RespawnFallbackPolicy.shouldUseRandSpawn(true, true, true));
    }
}
