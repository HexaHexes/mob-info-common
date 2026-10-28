package org.bitwisemadness.mobinfocommon.gametest;

import io.github.manasmods.tensura.entity.variant.DirewolfVariant;
import io.github.manasmods.tensura.entity.variant.SkeletonVariant;
import io.github.manasmods.tensura.registry.attribute.TensuraAttributes;
import io.github.manasmods.tensura.storage.TensuraStorages;
import io.github.manasmods.tensura.storage.player.ITensuraPlayer;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.mobinfocommon.MobInfoCommon;
import org.bitwisemadness.mobinfocommon.analysis.AnalysisGating;
import org.bitwisemadness.mobinfocommon.variant.VariantHelper;
import org.bitwisemadness.mobinfocommon.variant.VariantText;

/**
 * Exercises the library where a real Tensura is present: the variant pipeline
 * against Tensura's own enums, the per-consumer Tensura seam, and the gating
 * rules against live player attributes.
 *
 * <p>The expected variant names are read off the <b>enum itself</b> rather than
 * hardcoded, so the tests pin the pipeline (stored int -&gt; enum constant
 * -&gt; title-cased words) without pinning Tensura's declaration order, which
 * is Tensura's business and not this library's.</p>
 */
@GameTestHolder(MobInfoCommon.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MobInfoCommonGameTests {

    private MobInfoCommonGameTests() {
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static ResourceLocation type(String id) {
        return ResourceLocation.parse(id);
    }

    private static CompoundTag variantTag(int storedId) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Variant", storedId);
        return tag;
    }

    /** The mod is loaded and the empty structure template resolves. */
    @GameTest(template = "empty")
    public static void mod_loads(GameTestHelper helper) {
        helper.succeed();
    }

    /**
     * The full pipeline on a real Tensura species: a stored int resolves
     * through Tensura's own enum to title-cased words, a {@code DEFAULT}
     * constant is dropped so the mob reads plainly, and an out-of-range id
     * declines instead of throwing.
     */
    @GameTest(template = "empty")
    public static void tensura_variant_pipeline(GameTestHelper helper) {
        // An ordinary variant: whatever Tensura's second constant is today.
        DirewolfVariant[] direwolves = DirewolfVariant.values();
        check(direwolves.length > 1, "Tensura's direwolf must ship more than one variant");
        int ordinaryId = 1;
        VariantHelper.Variant ordinary =
                VariantHelper.variantOf(type("tensura:direwolf"), variantTag(ordinaryId));
        String expected = VariantText.titleCase(direwolves[ordinaryId].name());
        check(ordinary != null, "an in-range direwolf variant must resolve, id " + ordinaryId);
        check(expected.equals(ordinary.text()),
                "id " + ordinaryId + " must read <" + expected + "> but read <" + ordinary.text() + ">");
        check(!ordinary.replacesRace(),
                "a direwolf variant decorates the race rather than replacing it");

        // The shared composition, against the real names: a decorating variant
        // prepends, one that already names the wolf stands alone.
        check((expected + " Direwolf").equals(VariantHelper.qualifiedName("Direwolf", ordinary)),
                "a decorating variant must prepend to the species");
        int namingId = indexOf(direwolves, "MYSTIC_WOLF");
        check(namingId >= 0, "Tensura must still ship MYSTIC_WOLF; the retire rule is pinned on it");
        VariantHelper.Variant naming =
                VariantHelper.variantOf(type("tensura:direwolf"), variantTag(namingId));
        check(naming != null, "MYSTIC_WOLF must resolve");
        check("Mystic Wolf".equals(VariantHelper.qualifiedName("Direwolf", naming)),
                "a variant that already names the species must retire the race, not follow it");

        // DEFAULT means "the plain version" and is dropped rather than shown.
        SkeletonVariant[] skeletons = SkeletonVariant.values();
        int defaultId = indexOf(skeletons, "DEFAULT");
        check(defaultId >= 0, "Tensura's skeleton variants must still carry DEFAULT");
        check(VariantHelper.variantOf(type("tensura:skeleton"), variantTag(defaultId)) == null,
                "the DEFAULT constant must read as no variant, so the mob reads plainly");

        // A tag from a newer mod build must decline, not throw.
        check(VariantHelper.variantOf(type("tensura:direwolf"), variantTag(99)) == null,
                "an out-of-range stored id must read as no variant");

        helper.succeed();
    }

    /**
     * The per-consumer Tensura seam: a caller-supplied "no" suppresses Tensura
     * variant names while the installed-mod default still resolves - the
     * isolation that keeps Pseudocore's integration switch from becoming a
     * process-wide switch on Tensura Jade's lookups.
     */
    @GameTest(template = "empty")
    public static void variant_tensura_gate_suppresses(GameTestHelper helper) {
        CompoundTag tag = variantTag(1);
        check(VariantHelper.variantOf(type("tensura:direwolf"), tag, () -> false) == null,
                "a consumer switch that says no must suppress the Tensura variant");
        check(VariantHelper.variantOf(type("tensura:direwolf"), tag, () -> true) != null,
                "the same tag must resolve when the consumer switch permits");

        // The gate touches Tensura only: a species outside its reach ignores it.
        CompoundTag stringVariant = new CompoundTag();
        stringVariant.putString("Variant", "some_name");
        VariantHelper.Variant generic =
                VariantHelper.variantOf(type("some_other_mod:mob"), stringVariant, () -> false);
        check(generic != null && "Some Name".equals(generic.text()),
                "a non-Tensura species must keep resolving under a closed Tensura gate");

        helper.succeed();
    }

    /**
     * Tensura's gating matrix, read live: both attributes must be positive,
     * then only what the analysis mode allows - and the override turns all of
     * it off. Mirrors the same-named test Tensura Jade runs against its panel,
     * through the shared source of the rules.
     */
    @GameTest(template = "empty")
    public static void gating_matrix(GameTestHelper helper) {
        Player viewer = helper.makeMockPlayer(GameType.CREATIVE);

        // Override off: nothing else may be consulted, so this must hold for a
        // viewer that has never been given an analysis attribute at all.
        check(AnalysisGating.allowedForEntity(viewer, false),
                "with the override off, mob analysis must not be gated");
        check(AnalysisGating.allowedForBlock(viewer, false),
                "with the override off, block analysis must not be gated");

        setAttribute(viewer, TensuraAttributes.ANALYSIS_LEVEL, 0);
        setAttribute(viewer, TensuraAttributes.ANALYSIS_DISTANCE, 10);
        check(!AnalysisGating.allowedForEntity(viewer, true), "analysis level 0 must deny mob analysis");
        check(!AnalysisGating.allowedForBlock(viewer, true), "analysis level 0 must deny block analysis");

        setAttribute(viewer, TensuraAttributes.ANALYSIS_LEVEL, 2);
        setAttribute(viewer, TensuraAttributes.ANALYSIS_DISTANCE, 0);
        check(!AnalysisGating.allowedForEntity(viewer, true),
                "an analysis distance of 0 must deny mob analysis");
        check(!AnalysisGating.allowedForBlock(viewer, true),
                "an analysis distance of 0 must deny block analysis");

        setAttribute(viewer, TensuraAttributes.ANALYSIS_DISTANCE, 10);
        ITensuraPlayer data = TensuraStorages.getPlayerDataFrom(viewer);
        check(data != null,
                "a viewer must carry Tensura player data; without it the mode reads -1");

        data.setAnalysisMode(0);
        check(AnalysisGating.allowedForEntity(viewer, true), "mode 0 analyses whatever is aimed at");
        check(AnalysisGating.allowedForBlock(viewer, true), "mode 0 covers blocks too");

        data.setAnalysisMode(1);
        check(AnalysisGating.allowedForEntity(viewer, true), "mode 1 analyses mobs");
        check(!AnalysisGating.allowedForBlock(viewer, true), "mode 1 never analyses blocks");

        data.setAnalysisMode(2);
        check(!AnalysisGating.allowedForEntity(viewer, true), "mode 2 never analyses mobs");
        check(AnalysisGating.allowedForBlock(viewer, true), "mode 2 analyses blocks");

        data.setAnalysisMode(3);
        check(!AnalysisGating.allowedForEntity(viewer, true),
                "an unknown analysis mode must deny mob analysis");
        check(!AnalysisGating.allowedForBlock(viewer, true),
                "an unknown analysis mode must deny block analysis");

        helper.succeed();
    }

    /**
     * The blurred EP readout: beyond the viewer's stretched reach the figure
     * reads {@code "<reach>+"}; inside it, or without gating, the exact value
     * survives. All figures stay under a million, so the assertions are exact
     * strings with no language file in the way.
     */
    @GameTest(template = "empty")
    public static void ep_blur(GameTestHelper helper) {
        Player viewer = helper.makeMockPlayer(GameType.CREATIVE);
        setAttribute(viewer, TensuraAttributes.ANALYSIS_LEVEL, 2);
        setAttribute(viewer, TensuraAttributes.ANALYSIS_DISTANCE, 64);
        // A cap above the attribute's registered minimum of 10.0 - asking for 0
        // is read back as 10, which would silently throw every figure off.
        setAttribute(viewer, TensuraAttributes.MAX_AURA, 50.0D);
        setAttribute(viewer, TensuraAttributes.MAX_MAGICULE, 1000.0D);

        // Reach = the viewer's max EP (1000 magicule + 50 aura = 1050)
        //       * (1 + level 2 * 0.5) = 2100.
        check("2100+".equals(AnalysisGating.epReadout(viewer, 100049.0D, true).getString()),
                "a target beyond reach must blur into the reach figure plus a plus");
        check("1550".equals(AnalysisGating.epReadout(viewer, 1550.0D, true).getString()),
                "a target inside reach must survive exactly");
        check("100049".equals(AnalysisGating.epReadout(viewer, 100049.0D, false).getString()),
                "with gating off the true figure must show");
        check("100049".equals(AnalysisGating.epReadout(null, 100049.0D, true).getString()),
                "with no viewer to gate by, the exact figure must show");

        helper.succeed();
    }

    private static void setAttribute(Player player,
                                     net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                     double value) {
        var instance = player.getAttribute(attribute);
        check(instance != null, "a player must carry " + attribute.getRegisteredName()
                + "; the gating premise needs revisiting");
        instance.setBaseValue(value);
    }

    private static int indexOf(Enum<?>[] values, String constantName) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].name().equals(constantName)) return i;
        }
        return -1;
    }
}
