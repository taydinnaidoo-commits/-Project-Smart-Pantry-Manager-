package za.ac.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import za.ac.richfield.smartpantry.data.PantryDbHelper;

/**
 * Shared set-up for every screen: the database helper, the toolbar, keeping
 * content clear of the status/navigation bars, and the bottom navigation.
 *
 * <p>Every layout uses the ids {@code root} and {@code toolbar}; the three
 * top-level screens also have {@code bottom_navigation}.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected PantryDbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Draw behind the system bars on every Android version (Android 15+
        // forces this anyway), then pad the content back in below.
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        db = PantryDbHelper.getInstance(this);
    }

    /** Call straight after setContentView(). */
    protected void setUpScreen(boolean showUpArrow) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && showUpArrow) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Pad the root view by the size of the status bar, navigation bar and
        // keyboard, so nothing is hidden behind them.
        View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /** The toolbar's back arrow behaves like the system Back button. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Wires up the bottom navigation bar. Each tab is its own Activity, opened
     * with an explicit Intent. The Pantry screen stays at the bottom of the
     * back stack, so pressing Back from Recipes or Settings returns to it.
     */
    protected void setUpBottomNavigation(int currentItemId) {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(currentItemId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentItemId) {
                return true;
            }
            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = MainActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedRecipesActivity.class;
            } else {
                target = SettingsActivity.class;
            }
            Intent intent = new Intent(this, target);
            // Reuse the existing Pantry screen instead of stacking a new one.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            if (!(this instanceof MainActivity)) {
                finish();
            }
            // false: leave this screen's own tab highlighted while it closes.
            return false;
        });
    }
}
