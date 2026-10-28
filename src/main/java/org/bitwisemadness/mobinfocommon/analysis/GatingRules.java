package org.bitwisemadness.mobinfocommon.analysis;

/**
 * The decision math behind Tensura's analysis gating, in pure form.
 *
 * <p>{@link AnalysisGating} reads the live player attributes and hands them to
 * this class; nothing here touches Minecraft at all, which is what lets the
 * JUnit suite pin every branch without a game on the classpath.</p>
 *
 * <p>The rules are Tensura's own {@code pre_renderAnalysis} conditions, minus
 * its reach limit - see {@link AnalysisGating#allowedForEntity} for why the
 * distance the target sits at plays no part.</p>
 */
public final class GatingRules {

    private GatingRules() {
    }

    /** What the viewer is trying to analyse, which their mode can forbid. */
    public enum Scope {
        ENTITY,
        BLOCK
    }

    /**
     * Whether a viewer with these analysis attributes may see a panel of
     * {@code scope}.
     *
     * <p>Tensura gates its overlay on the {@code analysis_level} and
     * {@code analysis_distance} attributes both being positive, then on the
     * analysis mode: {@code 0} analyses whatever is aimed at (mobs win over
     * blocks), {@code 1} mobs only, {@code 2} blocks only. Any other mode -
     * including the {@code -1} a missing player storage reports - analyses
     * nothing.</p>
     */
    public static boolean entitled(int level, int distance, int mode, Scope scope) {
        if (level <= 0 || distance <= 0) return false;
        return switch (scope) {
            case ENTITY -> mode == 0 || mode == 1;
            case BLOCK -> mode == 0 || mode == 2;
        };
    }

    /**
     * The EP figure a viewer with {@code analysisLevel} may read exactly:
     * their own maximum EP stretched by half the level per level, the same
     * reach Tensura's own analysis uses before a value goes blurry.
     */
    public static double reach(double viewerMaxEp, int analysisLevel) {
        return viewerMaxEp * (1.0D + (double) analysisLevel * 0.5D);
    }

    /** Whether {@code targetEp} is beyond {@code reach} and must be blurred. */
    public static boolean blurred(double targetEp, double reach) {
        return targetEp > reach;
    }
}
