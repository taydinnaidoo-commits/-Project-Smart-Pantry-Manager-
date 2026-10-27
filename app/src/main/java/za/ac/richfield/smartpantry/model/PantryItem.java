package za.ac.richfield.smartpantry.model;

/**
 * One ingredient the user has at home, as stored in the pantry_items table.
 *
 * <p>The unit is always one of the codes in
 * {@link za.ac.richfield.smartpantry.logic.UnitConverter#SUPPORTED_UNITS}, and the
 * expiry date is optional, kept as an ISO "yyyy-MM-dd" string (or null).
 */
public class PantryItem {

    /** Id used for an item that has not been saved to the database yet. */
    public static final long NO_ID = -1;

    private final long id;
    private final String name;
    private final double quantity;
    private final String unit;
    private final String expiryDate;

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    /** ISO date ("yyyy-MM-dd"), or null when the user did not set one. */
    public String getExpiryDate() {
        return expiryDate;
    }

    /** Returns a copy of this item with a different quantity (used when cooking). */
    public PantryItem withQuantity(double newQuantity) {
        return new PantryItem(id, name, newQuantity, unit, expiryDate);
    }
}
