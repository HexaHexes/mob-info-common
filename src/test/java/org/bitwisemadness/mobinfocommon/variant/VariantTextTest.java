package org.bitwisemadness.mobinfocommon.variant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The word-shaping every consumer shares, pinned without a game on the
 * classpath - {@link VariantText} carries no Minecraft import for exactly this
 * reason.
 */
class VariantTextTest {

    // ---------------------------------------------------------------------
    // titleCase
    // ---------------------------------------------------------------------

    @Test
    void snakeCaseBecomesWords() {
        assertEquals("Dark Brown", VariantText.titleCase("dark_brown"));
        assertEquals("Red Fang", VariantText.titleCase("red_fang"));
    }

    @Test
    void shoutyTokensAreNormalised() {
        assertEquals("Red", VariantText.titleCase("RED"));
        assertEquals("Red", VariantText.titleCase("red"));
    }

    @Test
    void camelCasingSurvives() {
        // Not shouty, so only the first letter is touched.
        assertEquals("HiHi", VariantText.titleCase("hiHi"));
    }

    @Test
    void aNamespaceIsStripped() {
        assertEquals("Value", VariantText.titleCase("someid:value"));
        assertEquals("Dark Brown", VariantText.titleCase("someid:dark_brown"));
    }

    @Test
    void noiseDeclinesRatherThanRendering() {
        assertNull(VariantText.titleCase(null), "null must decline");
        assertNull(VariantText.titleCase(""), "blank must decline");
        assertNull(VariantText.titleCase("   "), "whitespace must decline");
        assertNull(VariantText.titleCase("12345"), "a bare number has no letters");
        assertNull(VariantText.titleCase("textures/mob/skin.png"), "a path must decline");
        assertNull(VariantText.titleCase("x".repeat(49)), "over 48 characters must decline");
    }

    @Test
    void longButLegitimateNamesStillRead() {
        assertEquals("X" + "x".repeat(47), VariantText.titleCase("x".repeat(48)),
                "exactly 48 characters is the accepted maximum, and only the first letter is touched");
    }

    // ---------------------------------------------------------------------
    // alreadyNamesMob - the #69 cases, pinned against the shared home
    // ---------------------------------------------------------------------

    @Test
    void limeDoesNotRetireSlime() {
        // The #69 report: "lime" is four letters and fell inside "slime" at
        // index 1 under the old substring test, so a Lime Slime's race word
        // was dropped and the name line read "Lime".
        assertFalse(VariantText.alreadyNamesMob("lime", "slime"));
    }

    @Test
    void aCompoundSpeciesStillRetiresOnItsInnerWord() {
        // "wolf" matches at the END of "direwolf", not the start - the
        // compound check is what keeps that a match.
        assertTrue(VariantText.alreadyNamesMob("Mystic Wind Wolf", "direwolf"));
        assertTrue(VariantText.alreadyNamesMob("Direwolf", "direwolf"),
                "a variant that IS the species name retires it too");
    }

    @Test
    void aDecoratingVariantKeepsTheRace() {
        assertFalse(VariantText.alreadyNamesMob("Red Fang", "direwolf"),
                "'Red Fang' says nothing about wolves; 'Red Fang Direwolf' keeps both words");
    }

    @Test
    void aSpeciesPrefixAloneDoesNotRetire() {
        // "slime" begins "king slime" but is not a word of it; the compound
        // guard ("s" is not a half of "slime") keeps the race.
        assertFalse(VariantText.alreadyNamesMob("king slime", "lime"));
    }

    @Test
    void matchingIgnoresCase() {
        assertTrue(VariantText.alreadyNamesMob("Mystic Wind Wolf", "DIREWOLF"));
        assertTrue(VariantText.alreadyNamesMob("mystic wind wolf", "Direwolf"));
    }

    @Test
    void nullAndBlankInputsNeverMatch() {
        assertFalse(VariantText.alreadyNamesMob(null, "slime"));
        assertFalse(VariantText.alreadyNamesMob("lime", null));
        assertFalse(VariantText.alreadyNamesMob(null, null));
        assertFalse(VariantText.alreadyNamesMob("   ", "slime"),
                "a variant that is only whitespace names nothing");
    }
}
