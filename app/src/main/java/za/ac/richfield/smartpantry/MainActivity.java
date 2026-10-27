package za.ac.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import za.ac.richfield.smartpantry.adapter.PantryAdapter;
import za.ac.richfield.smartpantry.data.AppPreferences;
import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.util.DateUtils;

/**
 * Pantry List screen (launcher). Shows every pantry item from the database in
 * a RecyclerView. Tap an item to edit it, tap the bin to delete it, or tap
 * "Add ingredient" to create a new one.
 */
public class MainActivity extends BaseActivity implements PantryAdapter.Listener {

    private PantryAdapter adapter;
    private TextView emptyView;
    private TextView expiryBanner;
    private View bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpScreen(false);
        setUpBottomNavigation(R.id.nav_pantry);

        emptyView = findViewById(R.id.empty_view);
        expiryBanner = findViewById(R.id.expiry_banner);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        RecyclerView list = findViewById(R.id.pantry_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        list.setAdapter(adapter);

        ExtendedFloatingActionButton addButton = findViewById(R.id.fab_add);
        addButton.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditItemActivity.class)));
    }

    /**
     * onResume runs every time this screen comes back into view - including
     * after the user saves on the Add/Edit screen - so reloading here keeps the
     * list in step with the database without any extra signalling.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        AppPreferences prefs = new AppPreferences(this);
        boolean alerts = prefs.isExpiryAlertsEnabled();
        int warningDays = prefs.getExpiryWarningDays();

        adapter.setItems(items, alerts, warningDays);
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        int expiringCount = 0;
        for (PantryItem item : items) {
            Integer days = DateUtils.daysUntil(item.getExpiryDate());
            if (days != null && days <= warningDays) {
                expiringCount++;
            }
        }
        if (alerts && expiringCount > 0) {
            expiryBanner.setText(getResources().getQuantityString(
                    R.plurals.expiry_banner, expiringCount, expiringCount, warningDays));
            expiryBanner.setVisibility(View.VISIBLE);
        } else {
            expiryBanner.setVisibility(View.GONE);
        }
    }

    /** Opens the form in edit mode, passing the item's id in the Intent. */
    @Override
    public void onItemClicked(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(PantryItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.delete_confirm_title, item.getName()))
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteItem(item))
                .show();
    }

    private void deleteItem(PantryItem item) {
        db.deletePantryItem(item.getId());
        loadPantry();
        Snackbar.make(findViewById(R.id.root),
                        getString(R.string.item_deleted, item.getName()), Snackbar.LENGTH_LONG)
                .setAnchorView(bottomNavigation)
                .setAction(R.string.undo, v -> {
                    db.insertPantryItem(item);
                    loadPantry();
                })
                .show();
    }
}
