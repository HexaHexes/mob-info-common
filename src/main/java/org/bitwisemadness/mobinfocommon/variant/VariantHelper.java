package org.bitwisemadness.mobinfocommon.variant;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

/**
 * Turns a stored mob's {@code Variant} tag into a name for a Race line or a
 * look-up title.
 *
 * <p>Every species in scope stores its variant under the same key but not the
 * same type, and no two species agree on how that value becomes words. Ice and
 * Fire keeps dragons, sea serpents and hippogryphs as registered names that its
 * own lang file translates; it keeps trolls as a registered name it never
 * translates at all; and it keeps death worms as a bare int. Twenty more
 * species - across Tensura, Mobs of Mythology and Mysticism - store a bare int
 * ordinal resolved through an enum. This class is the one place that knows all
 * of it.</p>
 *
 * <p>Deliberately imports <b>no</b> foreign type at all. The Ice and Fire
 * branches are pure NBT-and-lang lookups, and the int-ordinal family resolves
 * through {@link VariantBridge}, which gates every call on ModList (for Tensura,
 * on the caller's own supplier). That is what lets this class load identically
 * whatever the pack contains - and what lets a horn captured under one mod list
 * still resolve after that list has changed.</p>
 */
public final class VariantHelper {

    /** The one NBT key every species in scope uses, though the types differ. */
    private static final String VARIANT_KEY = "Variant";

    /**
     * The variant constant that names no particular variant.
     *
     * <p>Seven species use it, and on each one it means "the plain version".
     * Left alone it would reach the tooltip as {@code Default Skeleton},
     * {@code Default Cattledeer} or {@code Default Phantaspore} - text that
     * tells a reader nothing they did not already know from the race. Dropping
     * it lets those mobs read plainly as {@code Skeleton}, {@code Cattledeer}
     * and {@code Phantaspore}.</p>
     */
    private static final String DEFAULT_VARIANT = "DEFAULT";

    /**
     * Mobs of Mythology names no drake skins at all - its enum is bare
     * {@code DRAKE_1..DRAKE_7}, its textures are {@code drake_1.png} through
     * {@code drake_7.png}, and its lang file has no keys for them. Seven drakes
     * that differ only by palette therefore deserve better than numbers, so
     * these names are ours, taken from the textures themselves.
     *
     * <p>Indexed by the <b>stored id, which is zero-based</b>: {@code DRAKE_1}
     * is id 0 and {@code DRAKE_7} is id 6. Read off the enum's constructor
     * rather than assumed from the constant names, since guessing 1-based would
     * have misnamed every single drake.</p>
     */
    private static final String[] DRAKE_NAMES = {
            "Fiery",         // id 0 -> DRAKE_1
            "Plum",          // id 1 -> DRAKE_2
            "Crimson",       // id 2 -> DRAKE_3
            "Violet",        // id 3 -> DRAKE_4
            "Slate Green",   // id 4 -> DRAKE_5
            "Teal",          // id 5 -> DRAKE_6
            "Black",         // id 6 -> DRAKE_7
    };

    private VariantHelper() {
    }

    /**
     * A resolved variant, plus whether its text already names the species.
     *
     * @param text         the display name, never null
     * @param replacesRace true when the name already contains the species word,
     *                     so it stands in for the race instead of preceding it
     */
    public record Variant(String text, boolean replacesRace) {
    }

    /**
     * The variant of the stored mob, or null when it has none we can read.
     *
     * <p>Tensura lookups run when Tensura is installed. Consumers with a switch
     * of their own - Pseudocore's {@code tensuraIntegration} - use the overload
     * that takes the check, so their config cannot become a process-wide switch
     * on another mod's lookups.</p>
     */
    public static Variant variantOf(ResourceLocation typeId, CompoundTag data) {
        return variantOf(typeId, data, () -> ModList.get().isLoaded("tensura"));
    }

    /**
     * As {@link #variantOf(ResourceLocation, CompoundTag)}, with the Tensura
     * check supplied by the caller.
     *
     * @param tensuraAllowed whether Tensura variant lookups may resolve at all:
     *                       the installed test AND any consumer switch, in one
     *                       answer
     */
    public static Variant variantOf(ResourceLocation typeId, CompoundTag data,
                                    BooleanSupplier tensuraAllowed) {
        if (typeId == null || data == null || !data.contains(VARIANT_KEY)) return null;
        return switch (typeId.toString()) {
            // Dragon colours carry a lang key, and IaF's wording is not a
            // capitalisation of the stored name - "green" reads "Emerald".
            case "iceandfire:fire_dragon", "iceandfire:ice_dragon", "iceandfire:lightning_dragon" ->
                    localized(data, "dragon.");
            case "iceandfire:sea_serpent" -> localized(data, "sea_serpent.");
            // Hippogryph's key renders a whole phrase ("Black Hippogryph"), so
            // it replaces the race rather than preceding it.
            case "iceandfire:hippogryph" -> hippogryph(data);
            // Trolls have no lang key anywhere in IaF - only their armour and
            // weapon item names reveal that the three types are these words.
            case "iceandfire:troll" -> plainName(data);
            case "iceandfire:deathworm" -> deathWorm(data);

            // The int-ordinal family: twenty species across three mods that
            // all store a bare int under this one key and resolve it through
            // their own byId table. One shared shape, so one shared path.
            case "tensura:direwolf" -> intVariant(data, id -> VariantBridge.direwolf(id, tensuraAllowed));
            case "tensura:slime" -> intVariant(data, id -> VariantBridge.slime(id, tensuraAllowed));
            case "tensura:basilisk" -> intVariant(data, id -> VariantBridge.basilisk(id, tensuraAllowed));
            case "tensura:bone_golem" -> intVariant(data, id -> VariantBridge.boneGolem(id, tensuraAllowed));
            case "tensura:cattledeer" -> intVariant(data, id -> VariantBridge.cattledeer(id, tensuraAllowed));
            case "tensura:horned_rabbit" -> intVariant(data, id -> VariantBridge.hornedRabbit(id, tensuraAllowed));
            case "tensura:hound_dog" -> intVariant(data, id -> VariantBridge.houndDog(id, tensuraAllowed));
            case "tensura:hover_lizard" -> intVariant(data, id -> VariantBridge.hoverLizard(id, tensuraAllowed));
            case "tensura:landfish" -> intVariant(data, id -> VariantBridge.landfish(id, tensuraAllowed));
            case "tensura:leech_lizard" -> intVariant(data, id -> VariantBridge.leechLizard(id, tensuraAllowed));
            case "tensura:lizardman" -> intVariant(data, id -> VariantBridge.lizardman(id, tensuraAllowed));
            case "tensura:one_eyed_owl" -> intVariant(data, id -> VariantBridge.oneEyedOwl(id, tensuraAllowed));
            case "tensura:orc" -> intVariant(data, id -> VariantBridge.orc(id, tensuraAllowed));
            case "tensura:dragon_peacock" -> intVariant(data, id -> VariantBridge.dragonPeacock(id, tensuraAllowed));
            case "tensura:phantaspore" -> intVariant(data, id -> VariantBridge.phantaspore(id, tensuraAllowed));
            case "tensura:skeleton" -> intVariant(data, id -> VariantBridge.skeleton(id, tensuraAllowed));
            case "tensura:zombie" -> intVariant(data, id -> VariantBridge.zombie(id, tensuraAllowed));
            // The drake is the one species that needs names of our own, so it
            // owns a case rather than sharing the family path outright.
            case "mobs_of_mythology:drake" -> drake(data);
            case "mobs_of_mythology:sporeling" -> intVariant(data, VariantBridge::sporeling);
            case "mysticism:okami" -> intVariant(data, VariantBridge::okami);

            default -> plainName(data);
        };
    }

    /**
     * Whether the variant's own words already name the species it belongs to.
     *
     * <p>Delegates to {@link VariantText}, the MC-free home of the comparison -
     * kept here so a consumer that already holds this class finds the check
     * without hunting for a second one.</p>
     */
    public static boolean alreadyNamesMob(String variantText, String raceText) {
        return VariantText.alreadyNamesMob(variantText, raceText);
    }

    /**
     * The variant-qualified display name: the shared "Red Fang Direwolf" /
     * "Emerald Fire Dragon" composition behind the horn's Race line and Jade's
     * variant-qualified title.
     *
     * <p>Every consumer qualifies the same way, on purpose: a variant that
     * already names or replaces the species stands alone, and one that merely
     * decorates it is prepended. A null variant - or null text - is the base
     * name unchanged.</p>
     */
    public static String qualifiedName(String baseName, Variant variant) {
        if (variant == null || variant.text() == null) return baseName;
        if (baseName == null) return variant.text();
        return replacesOrNames(baseName, variant)
                ? variant.text()
                : variant.text() + " " + baseName;
    }

    /**
     * As {@link #qualifiedName(String, Variant)}, for a component base name:
     * the base keeps whatever styling it carries when prepended to, and the
     * replacing form stands alone as plain text. The decision itself is the
     * shared one, so a Jade title and a horn's Race line can never disagree
     * about which shape a variant takes.
     */
    public static Component qualifiedName(Component baseName, Variant variant) {
        if (variant == null || variant.text() == null) return baseName;
        if (baseName == null) return Component.literal(variant.text());
        return replacesOrNames(baseName.getString(), variant)
                ? Component.literal(variant.text())
                : Component.literal(variant.text() + " ").append(baseName);
    }

    /** The one decision behind both overloads: does the variant stand alone? */
    private static boolean replacesOrNames(String baseName, Variant variant) {
        return variant.replacesRace() || alreadyNamesMob(variant.text(), baseName);
    }

    /**
     * A species whose variant is a registered name with a short lang namespace.
     *
     * <p>Falls back to a title-cased name so an unlocalised variant degrades to
     * readable text rather than leaking a translation key into a tooltip.</p>
     */
    private static Variant localized(CompoundTag data, String keyPrefix) {
        String raw = stringVariant(data);
        String title = VariantText.titleCase(raw);
        if (title == null) return null;
        return new Variant(Component.translatableWithFallback(keyPrefix + raw, title).getString(), false);
    }

    /**
     * Hippogryph, whose lang value is a finished name rather than a modifier.
     *
     * <p>Prefixing it with the race would read "Black Hippogryph Hippogryph",
     * so this is the one species that substitutes instead of prepends. The
     * fallback appends the species word for the same reason - without IaF's
     * translation the line still has to name the mob.</p>
     */
    private static Variant hippogryph(CompoundTag data) {
        String raw = stringVariant(data);
        String title = VariantText.titleCase(raw);
        if (title == null) return null;
        String text = Component.translatableWithFallback(
                "entity.iceandfire.hippogryph." + raw, title + " Hippogryph").getString();
        return new Variant(text, true);
    }

    /** A variant stored as a name we simply read and title-case. */
    private static Variant plainName(CompoundTag data) {
        String title = VariantText.titleCase(stringVariant(data));
        return title == null ? null : new Variant(title, false);
    }

    /**
     * The death worm's int variant, mapped exactly as IaF's renderer does.
     *
     * <p>The three chitin colours are tan, red and white, but the order is
     * <b>not</b> the order the renderer declares its texture constants in
     * ({@code RED, WHITE, YELLOW}). {@code DeathWormEntityRenderer#getTexture}
     * tests 2 for white, 1 for red, and falls through to yellow for anything
     * else, which makes 0 the tan variant. Taken from that branch rather than
     * from declaration order, which would have named every worm wrongly.</p>
     */
    private static Variant deathWorm(CompoundTag data) {
        if (!data.contains(VARIANT_KEY, Tag.TAG_INT)) return null;
        return new Variant(switch (data.getInt(VARIANT_KEY)) {
            case 1 -> "Red";
            case 2 -> "White";
            default -> "Tan";
        }, false);
    }

    /**
     * A variant stored as a bare int ordinal and resolved through its owning
     * mod's enum - the shape shared by twenty species across three mods.
     *
     * <p>The resolver is gated behind {@link VariantBridge}, so a mod that is
     * no longer installed yields no variant instead of a crash. That matters
     * because a horn is portable: one captured under Mobs of Mythology and
     * hovered after its removal still carries that mob's NBT.</p>
     *
     * <p>The {@link #DEFAULT_VARIANT} constant is dropped here rather than
     * per-species, which is what lets seven different mobs read plainly as
     * {@code Skeleton}, {@code Cattledeer} and {@code Phantaspore} instead of
     * {@code Default Skeleton} and friends.</p>
     */
    private static Variant intVariant(CompoundTag data, IntFunction<Optional<String>> resolver) {
        if (data == null || !data.contains(VARIANT_KEY, Tag.TAG_INT)) return null;
        String raw = resolver.apply(data.getInt(VARIANT_KEY)).orElse(null);
        if (raw == null || DEFAULT_VARIANT.equals(raw)) return null;
        String title = VariantText.titleCase(raw);
        return title == null ? null : new Variant(title, false);
    }

    /**
     * The drake, whose seven skins MoM never names.
     *
     * <p>Our own table covers the ids the mod ships today; anything outside it
     * falls through to the enum's own constant, so a future build adding an
     * eighth drake degrades to {@code Drake 8} rather than showing nothing or
     * throwing.</p>
     */
    private static Variant drake(CompoundTag data) {
        if (data == null || !data.contains(VARIANT_KEY, Tag.TAG_INT)) return null;
        int id = data.getInt(VARIANT_KEY);
        if (id >= 0 && id < DRAKE_NAMES.length) return new Variant(DRAKE_NAMES[id], false);
        return intVariant(data, VariantBridge::drake);
    }

    /**
     * The stored variant as a raw string, or null if it is not one.
     *
     * <p>Public because a consumer's dragon-tint path needs the <b>raw</b>
     * stored value - the un-localized {@code DragonColor} name - which the
     * {@link Variant} deliberately translates away. Keeping the read here
     * means the {@code Variant} key and its type guard stay in one place.</p>
     */
    public static String stringVariant(CompoundTag data) {
        if (data == null || !data.contains(VARIANT_KEY, Tag.TAG_STRING)) return null;
        return data.getString(VARIANT_KEY);
    }
}
