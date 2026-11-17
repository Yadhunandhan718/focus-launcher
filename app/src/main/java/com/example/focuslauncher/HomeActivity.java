package com.example.focuslauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * The home screen: a clock, a grid of big tiles that fill the screen, and an "All apps" bar.
 *
 *   tap a tile ........ open the app
 *   long-press a tile . change or remove the app
 *   tap an empty "+" .. choose an app
 *   long-press clock .. launcher settings (grayscale, default home app, reset)
 */
public class HomeActivity extends Activity {

    // ===================== TINKER ZONE =====================
    /** Tiles per row. Try 1 for a very "elderly mode" look, or 3 for more apps. */
    private static final int COLUMNS = 2;
    /** Rows of tiles. Total tiles = COLUMNS * ROWS. */
    private static final int ROWS = 3;
    /** Gap around each tile, in dp. */
    private static final int TILE_MARGIN_DP = 6;
    /** Size of the "+" on empty tiles, in sp. */
    private static final float EMPTY_PLUS_SP = 56f;
    // =======================================================

    private Prefs prefs;
    private LinearLayout grid;
    private final ColorFilter grayFilter = makeGrayFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prefs = new Prefs(this);
        grid = findViewById(R.id.grid);
        Ui.applySystemBarPadding(findViewById(R.id.root), false);

        seedDefaults(false);

        findViewById(R.id.allApps).setOnClickListener(v ->
                startActivity(new Intent(this, AppPickerActivity.class)));

        findViewById(R.id.header).setOnLongClickListener(v -> {
            showSettings();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rebuild every time we come back: the picker may have changed a tile.
        render();
    }

    // ---------------------------------------------------------------- grid

    private void render() {
        grid.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        int margin = Ui.dp(this, TILE_MARGIN_DP);

        int slot = 0;
        for (int r = 0; r < ROWS; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            // height 0 + weight 1 => every row gets an equal share of the screen
            grid.addView(row, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

            for (int c = 0; c < COLUMNS; c++, slot++) {
                View tile = inflater.inflate(R.layout.item_tile, row, false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                lp.setMargins(margin, margin, margin, margin);
                row.addView(tile, lp);
                bindTile(tile, slot);
            }
        }
    }

    private void bindTile(View tile, final int slot) {
        ImageView icon = tile.findViewById(R.id.tileIcon);
        TextView label = tile.findViewById(R.id.tileLabel);

        final String pkg = prefs.getSlot(slot);
        Drawable drawable = pkg == null ? null : AppRepository.loadIcon(this, pkg);

        if (pkg == null || drawable == null) {
            // Empty tile (or the app was uninstalled): show a big "+".
            icon.setVisibility(View.GONE);
            label.setText("+");
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, EMPTY_PLUS_SP);
            tile.setOnClickListener(v -> openPicker(slot));
            tile.setOnLongClickListener(v -> {
                openPicker(slot);
                return true;
            });
            return;
        }

        icon.setVisibility(View.VISIBLE);
        icon.setImageDrawable(drawable);
        ColorFilter filter = prefs.isGrayscale() ? grayFilter : null;
        icon.setColorFilter(filter);
        label.setText(AppRepository.labelOf(this, pkg));

        tile.setOnClickListener(v -> {
            if (!AppRepository.launch(this, pkg)) {
                Toast.makeText(this, "Can't open that app", Toast.LENGTH_SHORT).show();
            }
        });
        tile.setOnLongClickListener(v -> {
            showTileMenu(slot, pkg);
            return true;
        });
    }

    // ------------------------------------------------------------ actions

    private void openPicker(int slot) {
        Intent intent = new Intent(this, AppPickerActivity.class);
        intent.putExtra(AppPickerActivity.EXTRA_SLOT, slot);
        startActivity(intent);
    }

    private void showTileMenu(final int slot, String pkg) {
        String[] items = {"Change app", "Remove from home"};
        new AlertDialog.Builder(this)
                .setTitle(AppRepository.labelOf(this, pkg))
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        openPicker(slot);
                    } else {
                        prefs.setSlot(slot, null);
                        render();
                    }
                })
                .show();
    }

    private void showSettings() {
        String[] items = {
                "Grayscale icons: " + (prefs.isGrayscale() ? "ON" : "OFF"),
                "Set as default home app",
                "Reset tiles to defaults"
        };
        new AlertDialog.Builder(this)
                .setTitle("Launcher settings")
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            prefs.setGrayscale(!prefs.isGrayscale());
                            render();
                            break;
                        case 1:
                            openHomeSettings();
                            break;
                        case 2:
                            seedDefaults(true);
                            render();
                            break;
                        default:
                            break;
                    }
                })
                .show();
    }

    private void openHomeSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    // ------------------------------------------------------------ defaults

    /** Fills the tiles with the phone's dialer, SMS, camera, maps and browser. */
    private void seedDefaults(boolean force) {
        if (!force && prefs.isSeeded()) return;

        List<String> defaults = AppRepository.guessDefaultApps(this);
        int total = COLUMNS * ROWS;
        for (int i = 0; i < total; i++) {
            prefs.setSlot(i, i < defaults.size() ? defaults.get(i) : null);
        }
        prefs.markSeeded();
    }

    private static ColorFilter makeGrayFilter() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        return new ColorMatrixColorFilter(matrix);
    }
}
