package org.bitwisemadness.mobinfocommon.analysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tensura's gating rules, pinned in pure form: both attributes must be
 * positive, the mode decides what may be analysed, and the EP reach stretches
 * by half the analysis level per level.
 */
class GatingRulesTest {

    @Test
    void bothAttributesMustBePositive() {
        assertFalse(GatingRules.entitled(0, 10, 0, GatingRules.Scope.ENTITY),
                "analysis level 0 is Tensura's own statement of no analysis");
        assertFalse(GatingRules.entitled(2, 0, 0, GatingRules.Scope.ENTITY),
                "analysis distance 0 is the same statement on the other axis");
        assertFalse(GatingRules.entitled(-1, 10, 0, GatingRules.Scope.BLOCK));
        assertTrue(GatingRules.entitled(1, 1, 0, GatingRules.Scope.ENTITY));
    }

    @Test
    void theModeDecidesWhatMayBeAnalysed() {
        assertTrue(GatingRules.entitled(2, 10, 0, GatingRules.Scope.ENTITY), "mode 0: whatever is aimed at");
        assertTrue(GatingRules.entitled(2, 10, 0, GatingRules.Scope.BLOCK), "mode 0 covers blocks too");
        assertTrue(GatingRules.entitled(2, 10, 1, GatingRules.Scope.ENTITY), "mode 1: mobs only");
        assertFalse(GatingRules.entitled(2, 10, 1, GatingRules.Scope.BLOCK), "mode 1 never analyses blocks");
        assertFalse(GatingRules.entitled(2, 10, 2, GatingRules.Scope.ENTITY), "mode 2 never analyses mobs");
        assertTrue(GatingRules.entitled(2, 10, 2, GatingRules.Scope.BLOCK), "mode 2: blocks only");
    }

    @Test
    void anUnknownModeAnalysesNothing() {
        // This is also the branch a viewer with no Tensura player data takes:
        // the missing storage reports -1.
        assertFalse(GatingRules.entitled(2, 10, -1, GatingRules.Scope.ENTITY));
        assertFalse(GatingRules.entitled(2, 10, 3, GatingRules.Scope.BLOCK));
    }

    @Test
    void reachStretchesByHalfTheLevelPerLevel() {
        // The numbers from tensura-jade's own ep_blur: a 1050-EP viewer at
        // level 2 may read exactly up to 2100.
        assertEquals(2100.0D, GatingRules.reach(1050.0D, 2), 1.0E-9);
        assertEquals(1050.0D, GatingRules.reach(1050.0D, 0), 1.0E-9, "level 0 reads their own maximum");
    }

    @Test
    void beyondReachBlurs() {
        assertTrue(GatingRules.blurred(100049.0D, 2100.0D));
        assertFalse(GatingRules.blurred(1550.0D, 2100.0D), "a value inside reach survives exactly");
        assertFalse(GatingRules.blurred(2100.0D, 2100.0D), "exactly at reach is inside reach");
    }
}
