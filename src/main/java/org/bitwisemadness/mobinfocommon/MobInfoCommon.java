package org.bitwisemadness.mobinfocommon;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Shared mob-information logic for the Bitwise Madness Tensura addons.
 *
 * <p>The library ships no gameplay and renders nothing of its own: Pseudocore
 * reads its variant names for the horn's Race line, and Tensura Jade reads the
 * same names for Jade's entity title plus the analysis gating that decides who
 * may see EP. Every foreign mod it touches - Tensura, Mobs of Mythology,
 * Mysticism - sits behind a {@code ModList} gate, so the library loads (and
 * simply resolves nothing) on an installation that has none of them.</p>
 */
@Mod(MobInfoCommon.MOD_ID)
public final class MobInfoCommon {

    public static final String MOD_ID = "mob_info_common";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MobInfoCommon(IEventBus modBus) {
    }
}
