package org.bitwisemadness.mobinfocommon;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Keeps the junit CI check non-vacuous: the suite must always assert something
 * real, so the check cannot pass by running zero tests.
 */
class MobInfoCommonTest {
    @Test
    void modIdIsPlainLowercaseIdentifier() {
        assertTrue(MobInfoCommon.MOD_ID.matches("[a-z][a-z0-9_]*"),
                "MOD_ID must be a plain lowercase identifier, got: " + MobInfoCommon.MOD_ID);
    }
}
