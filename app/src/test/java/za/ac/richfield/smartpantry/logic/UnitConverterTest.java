package za.ac.richfield.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests for converting quantities between units. */
public class UnitConverterTest {

    private static final double DELTA = 1e-9;

    @Test
    public void convertsWithinTheSameDimension() {
        assertEquals(2000, UnitConverter.convert(2, "kg", "g", null), DELTA);
        assertEquals(0.25, UnitConverter.convert(250, "ml", "l", null), DELTA);
        assertEquals(3, UnitConverter.convert(1, "tbsp", "tsp", null), DELTA);
        assertEquals(250, UnitConverter.convert(1, "cup", "ml", null), DELTA);
    }

    @Test
    public void bridgesCountAndMassForKnownIngredients() {
        assertEquals(300, UnitConverter.convert(2, "piece", "g", "onion"), DELTA);
        assertEquals(2, UnitConverter.convert(100, "g", "piece", "egg"), DELTA);
    }

    @Test
    public void bridgesVolumeAndMassForKnownStaples() {
        assertEquals(6, UnitConverter.convert(1, "tsp", "g", "salt"), DELTA);
    }

    @Test
    public void refusesConversionsItCannotJustify() {
        assertTrue(Double.isNaN(UnitConverter.convert(100, "ml", "g", "cream")));
        assertTrue(Double.isNaN(UnitConverter.convert(1, "piece", "g", null)));
        assertTrue(Double.isNaN(UnitConverter.convert(1, "slice", "piece", "bread")));
        assertTrue(Double.isNaN(UnitConverter.convert(1, "bucket", "g", "salt")));
    }

    @Test
    public void understandsCommonSpellingsOfUnits() {
        assertEquals("g", UnitConverter.canonicalUnit("Grams"));
        assertEquals("tbsp", UnitConverter.canonicalUnit("tablespoons"));
        assertEquals("piece", UnitConverter.canonicalUnit("pcs"));
        assertEquals("l", UnitConverter.canonicalUnit("Litres"));
        assertNull(UnitConverter.canonicalUnit("bucket"));
    }
}
