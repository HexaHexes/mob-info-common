package org.bitwisemadness.mobinfocommon.analysis;

import io.github.manasmods.tensura.registry.attribute.TensuraAttributes;
import io.github.manasmods.tensura.storage.TensuraStorages;
import io.github.manasmods.tensura.storage.player.ITensuraPlayer;
import io.github.manasmods.tensura.util.EnergyHelper;
import net.minecraft.world.entity.player.Player;

/**
 * The only class in this library that imports a Tensura type for analysis.
 *
 * <p>Never reference this directly - always go through {@link AnalysisGating},
 * which gates every call on ModList so this class is only loaded when Tensura
 * is actually installed. Loading it without Tensura would throw
 * {@link NoClassDefFoundError}.</p>
 */
final class TensuraAnalysisAccess {

    private TensuraAnalysisAccess() {
    }

    static int analysisLevel(Player viewer) {
        return (int) viewer.getAttributeValue(TensuraAttributes.ANALYSIS_LEVEL);
    }

    static int analysisDistance(Player viewer) {
        return (int) viewer.getAttributeValue(TensuraAttributes.ANALYSIS_DISTANCE);
    }

    /** Returns {@code -1} when the viewer has no Tensura player data, which never analyses. */
    static int analysisMode(Player viewer) {
        ITensuraPlayer data = TensuraStorages.getPlayerDataFrom(viewer);
        return data == null ? -1 : data.getAnalysisMode();
    }

    /** A live entity's maximum EP, the base Tensura's own reach maths starts from. */
    static double maxEp(Player entity) {
        return EnergyHelper.getMaxEP(entity);
    }
}
