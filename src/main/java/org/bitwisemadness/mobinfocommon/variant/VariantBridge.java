package org.bitwisemadness.mobinfocommon.variant;

import net.neoforged.fml.ModList;
import org.bitwisemadness.mobinfocommon.MobInfoCommon;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Facade for every int-ordinal variant the pack's mods use.
 *
 * <p>This class deliberately contains <b>no</b> reference to any Tensura, Mobs
 * of Mythology or Mysticism type. Each method checks its owning mod - the
 * Tensura check arriving as the caller's {@code tensuraAllowed} supplier, the
 * others taken straight from {@link ModList} - and only then delegates to an
 * access class - {@link TensuraAccess}, {@link MythologyAccess} or
 * {@link MysticismAccess} - that does import the mod's types. Because the JVM
 * resolves a class's references lazily, those access classes are never loaded,
 * and so never try to resolve missing types, when their mod is absent.</p>
 *
 * <p>That gate is load-bearing rather than defensive. The entity-id switch in
 * {@link VariantHelper} can only be reached by a mob of that exact type, which
 * requires the mod - except that a horn is a <b>portable, storable item</b>. A
 * horn captured while Mobs of Mythology was installed and hovered after it was
 * removed reaches the branch anyway, and without the gate that is a
 * {@link NoClassDefFoundError} on a tooltip. Capture, transport and trade
 * across mod-list changes is precisely what these lookups are for.</p>
 *
 * <p>The Tensura check is a <b>parameter</b>, not a static a consumer once
 * could replace: in a pack that ships both Pseudocore and Tensura Jade, a
 * process-wide switch would let one mod's config silently suppress the other
 * mod's variant names. Each lookup carries its own answer instead.</p>
 */
public final class VariantBridge {

    private VariantBridge() {
    }

    /**
     * Whether any mod whose variants this library can name is installed - the
     * cheap pre-check for callers that would otherwise serialise every entity
     * they meet just to find a {@code Variant} key that cannot exist.
     *
     * <p>Deliberately the plain ModList answer with no per-consumer switch: it
     * is a cost filter for "can this lookup possibly resolve", and a consumer
     * that wants its own suppression applies that at its own call site.</p>
     */
    public static boolean anyVariantSourceLoaded() {
        ModList mods = ModList.get();
        return mods.isLoaded("tensura") || mods.isLoaded("mobs_of_mythology") || mods.isLoaded("mysticism");
    }

    /** Tensura's direwolf. The species word is in the name itself. */
    static Optional<String> direwolf(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.direwolfVariant(id));
    }

    /** The slime's colour. Its {@code Type} key is a separate axis, unused. */
    static Optional<String> slime(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.slimeColor(id));
    }

    static Optional<String> basilisk(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.basiliskVariant(id));
    }

    static Optional<String> boneGolem(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.boneGolemVariant(id));
    }

    static Optional<String> cattledeer(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.cattledeerVariant(id));
    }

    static Optional<String> hornedRabbit(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.hornedRabbitVariant(id));
    }

    static Optional<String> houndDog(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.houndDogVariant(id));
    }

    static Optional<String> hoverLizard(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.hoverLizardVariant(id));
    }

    static Optional<String> landfish(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.landfishVariant(id));
    }

    static Optional<String> leechLizard(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.leechLizardVariant(id));
    }

    static Optional<String> lizardman(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.lizardmanVariant(id));
    }

    static Optional<String> oneEyedOwl(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.oneEyedOwlVariant(id));
    }

    static Optional<String> orc(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.orcVariant(id));
    }

    static Optional<String> dragonPeacock(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.dragonPeacockVariant(id));
    }

    static Optional<String> phantaspore(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.phantasporeVariant(id));
    }

    static Optional<String> skeleton(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.skeletonVariant(id));
    }

    static Optional<String> zombie(int id, BooleanSupplier tensuraAllowed) {
        return guarded(tensuraAllowed, () -> TensuraAccess.zombieVariant(id));
    }

    /**
     * The drake's skin. MoM never names these, so {@link VariantHelper} owns
     * the readable table and calls this only as the fallback for an id that
     * table does not cover.
     */
    static Optional<String> drake(int id) {
        return guarded(() -> ModList.get().isLoaded("mobs_of_mythology"),
                () -> MythologyAccess.drakeVariant(id));
    }

    static Optional<String> sporeling(int id) {
        return guarded(() -> ModList.get().isLoaded("mobs_of_mythology"),
                () -> MythologyAccess.sporelingVariant(id));
    }

    static Optional<String> okami(int id) {
        return guarded(() -> ModList.get().isLoaded("mysticism"),
                () -> MysticismAccess.okamiVariant(id));
    }

    private static Optional<String> guarded(BooleanSupplier modLoaded, Supplier<Optional<String>> work) {
        if (!modLoaded.getAsBoolean()) return Optional.empty();
        try {
            return work.get();
        } catch (Throwable t) {
            // A tag from another mod build, or a resolver that has moved.
            MobInfoCommon.LOGGER.debug("Variant lookup failed", t);
            return Optional.empty();
        }
    }
}
