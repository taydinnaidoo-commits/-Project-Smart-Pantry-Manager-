package za.ac.richfield.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

import za.ac.richfield.smartpantry.logic.UnitConverter;
import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.util.DateUtils;
import za.ac.richfield.smartpantry.util.QuantityFormat;

/**
 * Add/Edit Ingredient screen. Started with no extras it adds a new item;
 * started with {@link #EXTRA_ITEM_ID} it loads that item for editing and also
 * offers Delete. All input is validated before anything is saved.
 */
public class AddEditItemActivity extends BaseActivity {

    /** Intent extra: id of the pantry item to edit. Absent when adding. */
    public static final String EXTRA_ITEM_ID = "za.ac.richfield.smartpantry.EXTRA_ITEM_ID";

    private static final String STATE_EXPIRY = "expiry_date";
    private static final int MAX_NAME_LENGTH = 40;
    private static final double MAX_QUANTITY = 100_000;

    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputEditText nameInput;
    private TextInputEditText quantityInput;
    private Spinner unitSpinner;
    private TextView expiryValue;
    private MaterialButton clearExpiryButton;

    private long itemId = PantryItem.NO_ID;
    /** Chosen expiry date in "yyyy-MM-dd" form, or null for none. */
    private String expiryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);
        setUpScreen(true);

        nameLayout = findViewById(R.id.name_layout);
        quantityLayout = findViewById(R.id.quantity_layout);
        nameInput = findViewById(R.id.name_input);
        quantityInput = findViewById(R.id.quantity_input);
        unitSpinner = findViewById(R.id.unit_spinner);
        expiryValue = findViewById(R.id.expiry_value);
        clearExpiryButton = findViewById(R.id.clear_expiry_button);
        MaterialButton pickExpiryButton = findViewById(R.id.pick_expiry_button);
        MaterialButton saveButton = findViewById(R.id.save_button);
        MaterialButton deleteButton = findViewById(R.id.delete_button);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.unit_labels, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);

        // Clear an error as soon as the user starts fixing that field.
        nameInput.addTextChangedListener(new ClearErrorWatcher(nameLayout));
        quantityInput.addTextChangedListener(new ClearErrorWatcher(quantityLayout));

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, PantryItem.NO_ID);
        boolean editing = itemId != PantryItem.NO_ID;
        setTitle(editing ? R.string.title_edit_item : R.string.title_add_item);
        deleteButton.setVisibility(editing ? View.VISIBLE : View.GONE);

        if (savedInstanceState != null) {
            // Rotated: the text fields and spinner restore themselves.
            expiryDate = savedInstanceState.getString(STATE_EXPIRY);
        } else if (editing) {
            PantryItem item = db.getPantryItem(itemId);
            if (item == null) {
                Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            fillForm(item);
        }
        showExpiry();

        pickExpiryButton.setOnClickListener(v -> pickExpiryDate());
        clearExpiryButton.setOnClickListener(v -> {
            expiryDate = null;
            showExpiry();
        });
        saveButton.setOnClickListener(v -> save());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPIRY, expiryDate);
    }

    private void fillForm(PantryItem item) {
        nameInput.setText(item.getName());
        quantityInput.setText(QuantityFormat.number(item.getQuantity()).replace(',', '.'));
        int unitIndex = UnitConverter.SUPPORTED_UNITS.indexOf(item.getUnit());
        unitSpinner.setSelection(Math.max(unitIndex, 0));
        expiryDate = item.getExpiryDate();
    }

    private void showExpiry() {
        if (expiryDate == null) {
            expiryValue.setText(R.string.expiry_not_set);
            clearExpiryButton.setVisibility(View.GONE);
        } else {
            expiryValue.setText(DateUtils.toDisplay(expiryDate));
            clearExpiryButton.setVisibility(View.VISIBLE);
        }
    }

    private void pickExpiryDate() {
        Calendar initial = DateUtils.parse(expiryDate);
        if (initial == null) {
            initial = Calendar.getInstance();
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            expiryDate = DateUtils.toDbString(year, month, day);
            showExpiry();
        }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Validates every field, shows errors next to bad ones, then saves. */
    private void save() {
        String name = textOf(nameInput);
        Double quantity = QuantityFormat.parse(textOf(quantityInput));
        boolean valid = true;

        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_required));
            valid = false;
        } else if (name.length() > MAX_NAME_LENGTH) {
            nameLayout.setError(getString(R.string.error_name_too_long, MAX_NAME_LENGTH));
            valid = false;
        } else if (!name.matches(".*\\p{L}.*")) {
            nameLayout.setError(getString(R.string.error_name_letters));
            valid = false;
        } else {
            PantryItem duplicate = db.findPantryItemByName(name, itemId);
            if (duplicate != null) {
                nameLayout.setError(getString(R.string.error_name_duplicate, duplicate.getName()));
                valid = false;
            }
        }

        if (textOf(quantityInput).isEmpty()) {
            quantityLayout.setError(getString(R.string.error_quantity_required));
            valid = false;
        } else if (quantity == null) {
            quantityLayout.setError(getString(R.string.error_quantity_number));
            valid = false;
        } else if (quantity <= 0) {
            quantityLayout.setError(getString(R.string.error_quantity_positive));
            valid = false;
        } else if (quantity > MAX_QUANTITY) {
            quantityLayout.setError(getString(R.string.error_quantity_too_large));
            valid = false;
        }

        if (!valid) {
            return;
        }

        String unit = UnitConverter.SUPPORTED_UNITS.get(unitSpinner.getSelectedItemPosition());
        PantryItem item = new PantryItem(itemId, name, quantity, unit, expiryDate);
        if (itemId == PantryItem.NO_ID) {
            db.insertPantryItem(item);
            Toast.makeText(this, getString(R.string.item_added, name), Toast.LENGTH_SHORT).show();
        } else {
            db.updatePantryItem(item);
            Toast.makeText(this, getString(R.string.item_updated, name), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        String name = textOf(nameInput);
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.delete_confirm_title, name))
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deletePantryItem(itemId);
                    Toast.makeText(this, getString(R.string.item_deleted, name),
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    private static String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    /** Removes a field's error message once the user edits that field. */
    private static class ClearErrorWatcher implements TextWatcher {
        private final TextInputLayout layout;

        ClearErrorWatcher(TextInputLayout layout) {
            this.layout = layout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            layout.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
