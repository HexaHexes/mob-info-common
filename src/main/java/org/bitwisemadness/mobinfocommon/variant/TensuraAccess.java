package org.bitwisemadness.mobinfocommon.variant;

import io.github.manasmods.tensura.entity.variant.BasiliskVariant;
import io.github.manasmods.tensura.entity.variant.BoneGolemVariant;
import io.github.manasmods.tensura.entity.variant.CattledeerVariant;
import io.github.manasmods.tensura.entity.variant.DirewolfVariant;
import io.github.manasmods.tensura.entity.variant.HornedRabbitVariant;
import io.github.manasmods.tensura.entity.variant.HoundDogVariant;
import io.github.manasmods.tensura.entity.variant.HoverLizardVariant;
import io.github.manasmods.tensura.entity.variant.LandfishVariant;
import io.github.manasmods.tensura.entity.variant.LeechLizardVariant;
import io.github.manasmods.tensura.entity.variant.LizardmanVariant;
import io.github.manasmods.tensura.entity.variant.OneEyedOwlVariant;
import io.github.manasmods.tensura.entity.variant.OrcVariant;
import io.github.manasmods.tensura.entity.variant.PeacockVariant;
import io.github.manasmods.tensura.entity.variant.PhantasporeVariant;
import io.github.manasmods.tensura.entity.variant.SkeletonVariant;
import io.github.manasmods.tensura.entity.variant.SlimeColor;
import io.github.manasmods.tensura.entity.variant.ZombieVariant;

import java.util.Optional;

/**
 * The only class in this library that imports a Tensura type for variant
 * resolution.
 *
 * <p>Never reference this class directly - always go through
 * {@link VariantBridge}, which gates every call on the caller's Tensura check
 * so this class is only loaded when Tensura is actually installed and
 * permitted. Loading it without Tensura would throw
 * {@link NoClassDefFoundError}.</p>
 */
final class TensuraAccess {

    private TensuraAccess() {
    }

    /**
     * Variant names for the seventeen Tensura species that store an int
     * ordinal under the {@code Variant} key.
     *
     * <p>All of these go through {@link Enum#name()} rather than the
     * {@code getName()} some of them also expose. For most species the two are
     * the same word in different cases, but Phantaspore's {@code getName()}
     * returns a <b>texture path</b> ({@code phantaspore_red}), which would
     * reach a tooltip as "Phantaspore Red Phantaspore". The constant name is
     * the display name everywhere here.</p>
     *
     * <p>Bounds are handled by {@link VariantNames} because {@code byId}
     * indexes its array directly and would throw on a tag from another mod
     * build.</p>
     */
    static Optional<String> direwolfVariant(int id) {
        return VariantNames.lookup(DirewolfVariant.values(), id, DirewolfVariant::byId);
    }

    static Optional<String> slimeColor(int id) {
        return VariantNames.lookup(SlimeColor.values(), id, SlimeColor::byId);
    }

    static Optional<String> basiliskVariant(int id) {
        return VariantNames.lookup(BasiliskVariant.values(), id, BasiliskVariant::byId);
    }

    static Optional<String> boneGolemVariant(int id) {
        return VariantNames.lookup(BoneGolemVariant.values(), id, BoneGolemVariant::byId);
    }

    static Optional<String> cattledeerVariant(int id) {
        return VariantNames.lookup(CattledeerVariant.values(), id, CattledeerVariant::byId);
    }

    static Optional<String> hornedRabbitVariant(int id) {
        return VariantNames.lookup(HornedRabbitVariant.values(), id, HornedRabbitVariant::byId);
    }

    static Optional<String> houndDogVariant(int id) {
        return VariantNames.lookup(HoundDogVariant.values(), id, HoundDogVariant::byId);
    }

    static Optional<String> hoverLizardVariant(int id) {
        return VariantNames.lookup(HoverLizardVariant.values(), id, HoverLizardVariant::byId);
    }

    static Optional<String> landfishVariant(int id) {
        return VariantNames.lookup(LandfishVariant.values(), id, LandfishVariant::byId);
    }

    static Optional<String> leechLizardVariant(int id) {
        return VariantNames.lookup(LeechLizardVariant.values(), id, LeechLizardVariant::byId);
    }

    static Optional<String> lizardmanVariant(int id) {
        return VariantNames.lookup(LizardmanVariant.values(), id, LizardmanVariant::byId);
    }

    static Optional<String> oneEyedOwlVariant(int id) {
        return VariantNames.lookup(OneEyedOwlVariant.values(), id, OneEyedOwlVariant::byId);
    }

    static Optional<String> orcVariant(int id) {
        return VariantNames.lookup(OrcVariant.values(), id, OrcVariant::byId);
    }

    static Optional<String> dragonPeacockVariant(int id) {
        return VariantNames.lookup(PeacockVariant.values(), id, PeacockVariant::byId);
    }

    static Optional<String> phantasporeVariant(int id) {
        return VariantNames.lookup(PhantasporeVariant.values(), id, PhantasporeVariant::byId);
    }

    static Optional<String> skeletonVariant(int id) {
        return VariantNames.lookup(SkeletonVariant.values(), id, SkeletonVariant::byId);
    }

    static Optional<String> zombieVariant(int id) {
        return VariantNames.lookup(ZombieVariant.values(), id, ZombieVariant::byId);
    }
}
