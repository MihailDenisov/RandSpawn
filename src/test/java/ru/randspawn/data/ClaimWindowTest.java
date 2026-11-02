package ru.randspawn.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClaimWindowTest {
    @Test
    void deadlineIsInclusiveAndUsesWallClockValues() {
        ClaimWindow claim = new ClaimWindow(10_000L, false);

        assertTrue(claim.isAvailableAt(9_999L));
        assertTrue(claim.isAvailableAt(10_000L));
        assertFalse(claim.isAvailableAt(10_001L));
    }

    @Test
    void consumedClaimCannotBeUsedAgain() {
        assertFalse(new ClaimWindow(10_000L, false).consume().isAvailableAt(1L));
    }
}
