package org.bitwisemadness.mobinfocommon.analysis;

import java.text.DecimalFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import org.bitwisemadness.mobinfocommon.MobInfoCommon;

/**
 * Tensura's analysis gating, shared by every addon that shows information the
 * overlay would have gated.
 *
 * <p>Tensura only lets a player analyse when their {@code analysis_level} and
 * {@code analysis_distance} attributes are both positive, then only what their
 * analysis mode allows, and it blurs EP beyond the viewer's reach into
 * {@code "12k+"} so analysis cannot be used to measure the unmeasurable. This
 * class reproduces those rules for tooltip-shaped consumers: Tensura Jade
 * feeds them its plugin-config toggle, Pseudocore its own config switch, and
 * both get the same answers from the same source.</p>
 *
 * <p>Every method takes the {@code respectGating} decision as a plain boolean
 * rather than a config interface: the toggles live in each consumer's own
 * config screen, and this library renders none of its own.</p>
 *
 * <p>Every method here uses only vanilla types; the Tensura reads sit in
 * {@link TensuraAnalysisAccess}, loaded only past the {@code ModList} gate.
 * That keeps the class loadable - and its {@code "tensura."} translation keys
 * harmless - on an installation without Tensura.</p>
 */
public final class AnalysisGating {

    private AnalysisGating() {
    }

    /**
     * Whether the viewer is entitled to entity analysis at all.
     *
     * <p><strong>The reach limit is deliberately not mirrored.</strong> Tensura
     * picks its target with {@code getTargetingEntity(player, analysisDistance,
     * ...)} so its own HUD stops dead at {@code analysis_distance} - 5 blocks
     * by default, with the attribute topping out at 1024. Tooltip consumers do
     * not stop there: Jade validates entity data out to roughly 24 blocks, and
     * a horn can be hovered from anywhere its stack is held. Comparing
     * distances would leave a band in which the always-visible rows stay on
     * screen while the gated ones vanish, and a tooltip that half-disappears
     * is worse than analysis reaching a little further than the HUD would. The
     * attribute is still consulted: {@code analysis_distance <= 0} is
     * Tensura's own statement that this viewer has no analysis to spend, and
     * that is kept.</p>
     *
     * <p>A null viewer - a tooltip rendered where no client player exists -
     * returns true: there is nobody to gate by, and degrading to "show" keeps
     * a server-side render honest rather than blank.</p>
     */
    public static boolean allowedForEntity(Player viewer, boolean respectGating) {
        return allowedFor(viewer, respectGating, GatingRules.Scope.ENTITY);
    }

    /**
     * As {@link #allowedForEntity(Player, boolean)}, for blocks: analysis mode
     * {@code 0} or {@code 2}, never {@code 1}. The reach limit is dropped here
     * for the same reason, and it is just as visible - Jade keeps reporting a
     * block long past the raycast Tensura would have cast at it.
     */
    public static boolean allowedForBlock(Player viewer, boolean respectGating) {
        return allowedFor(viewer, respectGating, GatingRules.Scope.BLOCK);
    }

    private static boolean allowedFor(Player viewer, boolean respectGating, GatingRules.Scope scope) {
        if (!respectGating) return true;
        if (viewer == null || !tensuraPresent()) return true;
        try {
            return GatingRules.entitled(
                    TensuraAnalysisAccess.analysisLevel(viewer),
                    TensuraAnalysisAccess.analysisDistance(viewer),
                    TensuraAnalysisAccess.analysisMode(viewer),
                    scope);
        } catch (Throwable t) {
            MobInfoCommon.LOGGER.debug("Analysis gating check failed; showing the information", t);
            return true;
        }
    }

    /**
     * How {@code targetEp} should read for this viewer: exactly when they may
     * see it and it is within reach, the reach figure plus {@code "+"} when it
     * is not (Tensura's own blur), and exactly when the viewer has no analysis
     * to spend and the consumer chose not to honour that for values - the
     * consumer hides the line instead, so the exact figure never leaks through
     * this path.
     *
     * <p>The blurred form is deliberately a <b>component</b>, not a string: the
     * shortening is Tensura's own recipe and carries its translation keys, so
     * "12k" reads from Tensura's language file rather than from English text
     * baked in here.</p>
     */
    public static Component epReadout(Player viewer, double targetEp, boolean respectGating) {
        double shown = targetEp;
        boolean blur = false;
        if (respectGating && viewer != null && tensuraPresent()) {
            try {
                double reach = GatingRules.reach(
                        TensuraAnalysisAccess.maxEp(viewer),
                        TensuraAnalysisAccess.analysisLevel(viewer));
                if (GatingRules.blurred(targetEp, reach)) {
                    shown = reach;
                    blur = true;
                }
            } catch (Throwable t) {
                MobInfoCommon.LOGGER.debug("EP reach check failed; showing the exact value", t);
            }
        }
        return blur
                ? shortenedNumber(shown).append(Component.literal("+"))
                : shortenedNumber(shown);
    }

    /**
     * The number shortening Tensura's overlay prints, reproduced rather than
     * reused.
     *
     * <p>Tensura keeps this as {@code RenderHelper.getShortenedNumber}, a
     * static method inside a client-rendering utility whose neighbours take
     * {@code GuiGraphics} and {@code MultiBufferSource}. Loading <em>that</em>
     * class on a dedicated server aborts with {@code invalid dist
     * DEDICATED_SERVER}, because verification resolves those parameter types
     * even for a pure method. This method is therefore transcribed here
     * byte-for-byte from Tensura's implementation - the same two thresholds,
     * the same {@code DecimalFormat} patterns, and Tensura's own
     * {@code million_index} / {@code billion_index} keys - so values read
     * identically while staying loadable on both sides.</p>
     *
     * <p>Anything below a million prints as a bare integer, as {@code "14000"};
     * a million or more becomes {@code "1.4M"}-style through Tensura's
     * translation keys, so the wording still comes from Tensura's language file
     * rather than from here.</p>
     */
    public static MutableComponent shortenedNumber(double value) {
        if (value < 1_000_000.0D) {
            return Component.literal(new DecimalFormat("#").format(value));
        }
        DecimalFormat scaled = new DecimalFormat("#.##");
        if (value < 1_000_000_000.0D) {
            return Component.translatable("tensura.main_menu.million_index",
                    scaled.format(value / 1_000_000.0D));
        }
        return Component.translatable("tensura.main_menu.billion_index",
                scaled.format(value / 1_000_000_000.0D));
    }

    private static boolean tensuraPresent() {
        return ModList.get().isLoaded("tensura");
    }
}
