package org.bitwisemadness.mobinfocommon.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;

/**
 * The bounds handling that lives in exactly one place, because each enum's
 * {@code byId} indexes its array directly and a stored id from another mod
 * build can be any int at all.
 */
class VariantNamesTest {

    /** A stand-in for the species enums: three constants, a byId like theirs. */
    private enum FakeVariant {
        ALPHA,
        BETA,
        GAMMA;

        static FakeVariant byId(int id) {
            return values()[id];
        }
    }

    @Test
    void anInRangeIdResolvesToItsConstantName() {
        assertEquals(Optional.of("BETA"), lookup(1));
        assertEquals(Optional.of("ALPHA"), lookup(0));
        assertEquals(Optional.of("GAMMA"), lookup(2));
    }

    @Test
    void anOutOfRangeIdDeclinesInsteadOfThrowing() {
        // The byId above would throw ArrayIndexOutOfBoundsException for both;
        // the bounds check is what keeps a portable horn's stale tag safe.
        assertEquals(Optional.empty(), lookup(-1));
        assertEquals(Optional.empty(), lookup(FakeVariant.values().length));
        assertEquals(Optional.empty(), lookup(Integer.MAX_VALUE));
    }

    @Test
    void aResolverReturningNullDeclines() {
        IntFunction<FakeVariant> nullResolver = id -> null;
        assertEquals(Optional.empty(), VariantNames.lookup(FakeVariant.values(), 1, nullResolver),
                "a byId that answers null must read as 'no variant', not as a crash");
    }

    private static Optional<String> lookup(int id) {
        return VariantNames.lookup(FakeVariant.values(), id, FakeVariant::byId);
    }
}
