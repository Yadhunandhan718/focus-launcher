package com.example.focuslauncher;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A searchable, plain-text list of every installed app.
 *
 * Two modes, decided by the EXTRA_SLOT extra:
 *   - no extra (NO_SLOT): "All apps". Tapping a row opens that app.
 *   - a tile number:      "Pick an app". Tapping a row saves it to that tile and closes.
 *
 * Tip: type a few letters and press Go on the keyboard to open/pick the first match.
 */
public class AppPickerActivity extends Activity {

    static final String EXTRA_SLOT = "slot";
    static final int NO_SLOT = -1;

    private int slot;
    private Prefs prefs;
    private List<AppInfo> allApps;
    private ArrayAdapter<AppInfo> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
        Ui.applySystemBarPadding(findViewById(R.id.pickerRoot), true);

        slot = getIntent().getIntExtra(EXTRA_SLOT, NO_SLOT);
        prefs = new Prefs(this);

        TextView title = findViewById(R.id.pickerTitle);
        title.setText(slot == NO_SLOT ? "All apps" : "Choose an app");

        // Fine for a hundred or so apps. If it ever feels slow, move this to a background thread.
        allApps = AppRepository.loadLaunchableApps(this);

        adapter = new ArrayAdapter<>(this, R.layout.item_app_row, R.id.rowText,
                new ArrayList<>(allApps));

        ListView list = findViewById(R.id.appList);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) ->
                choose(adapter.getItem(position)));

        EditText search = findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
        search.setOnEditorActionListener((v, actionId, event) -> {
            if (adapter.getCount() > 0) {
                choose(adapter.getItem(0));
            }
            return true;
        });
    }

    private void filter(String query) {
        String q = query.trim().toLowerCase(Locale.getDefault());
        List<AppInfo> matches = new ArrayList<>();
        for (AppInfo app : allApps) {
            if (q.isEmpty() || app.label.toLowerCase(Locale.getDefault()).contains(q)) {
                matches.add(app);
            }
        }
        adapter.clear();
        adapter.addAll(matches);
    }

    private void choose(AppInfo app) {
        if (app == null) return;

        if (slot != NO_SLOT) {
            prefs.setSlot(slot, app.packageName);
            finish();
        } else if (AppRepository.launch(this, app.packageName)) {
            finish(); // so Back from that app lands on the home screen
        } else {
            Toast.makeText(this, "Can't open that app", Toast.LENGTH_SHORT).show();
        }
    }
}
