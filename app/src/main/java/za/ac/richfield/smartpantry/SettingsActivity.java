package za.ac.richfield.smartpantry;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

import za.ac.richfield.smartpantry.data.AppPreferences;
import za.ac.richfield.smartpantry.data.SampleData;
import za.ac.richfield.smartpantry.model.PantryItem;

/**
 * Settings screen: expiry alert options, whether to show the "Almost there"
 * list, and pantry data tools (load a sample pantry, clear the pantry).
 */
public class SettingsActivity extends BaseActivity {

    private AppPreferences prefs;
    private TextView databaseInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setUpScreen(false);
        setUpBottomNavigation(R.id.nav_settings);
        prefs = new AppPreferences(this);

        MaterialSwitch expirySwitch = findViewById(R.id.expiry_alerts_switch);
        Spinner daysSpinner = findViewById(R.id.expiry_days_spinner);
        MaterialSwitch almostThereSwitch = findViewById(R.id.almost_there_switch);
        databaseInfo = findViewById(R.id.database_info);

        expirySwitch.setChecked(prefs.isExpiryAlertsEnabled());
        daysSpinner.setEnabled(prefs.isExpiryAlertsEnabled());
        expirySwitch.setOnCheckedChangeListener((button, checked) -> {
            prefs.setExpiryAlertsEnabled(checked);
            daysSpinner.setEnabled(checked);
        });

        setUpDaysSpinner(daysSpinner);

        almostThereSwitch.setChecked(prefs.isShowAlmostThere());
        almostThereSwitch.setOnCheckedChangeListener(
                (button, checked) -> prefs.setShowAlmostThere(checked));

        MaterialButton sampleButton = findViewById(R.id.load_sample_button);
        sampleButton.setOnClickListener(v -> loadSamplePantry());
        MaterialButton clearButton = findViewById(R.id.clear_pantry_button);
        clearButton.setOnClickListener(v -> confirmClearPantry());
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDatabaseInfo();
    }

    private void setUpDaysSpinner(Spinner spinner) {
        List<String> labels = new ArrayList<>();
        int selected = 0;
        for (int i = 0; i < AppPreferences.EXPIRY_DAY_OPTIONS.length; i++) {
            int days = AppPreferences.EXPIRY_DAY_OPTIONS[i];
            labels.add(getResources().getQuantityString(R.plurals.days_option, days, days));
            if (days == prefs.getExpiryWarningDays()) {
                selected = i;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selected);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.setExpiryWarningDays(AppPreferences.EXPIRY_DAY_OPTIONS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showDatabaseInfo() {
        databaseInfo.setText(getString(R.string.settings_database_info,
                db.getRecipeCount(), db.getAllPantryItems().size()));
    }

    /** Adds the sample items, skipping any ingredient the user already has. */
    private void loadSamplePantry() {
        int added = 0;
        for (PantryItem item : SampleData.samplePantry()) {
            if (db.findPantryItemByName(item.getName(), PantryItem.NO_ID) == null) {
                db.insertPantryItem(item);
                added++;
            }
        }
        Toast.makeText(this, getResources().getQuantityString(
                R.plurals.sample_loaded, added, added), Toast.LENGTH_SHORT).show();
        showDatabaseInfo();
    }

    private void confirmClearPantry() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.clear_confirm_title)
                .setMessage(R.string.clear_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.clear, (dialog, which) -> {
                    db.deleteAllPantryItems();
                    Toast.makeText(this, R.string.pantry_cleared, Toast.LENGTH_SHORT).show();
                    showDatabaseInfo();
                })
                .show();
    }
}
