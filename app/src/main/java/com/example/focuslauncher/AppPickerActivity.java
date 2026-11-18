package com.example.focuslauncher;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** App search/picker. First-run setup is a multi-select allowlist; all later modes are allowlist-only. */
public class AppPickerActivity extends Activity {
    static final String EXTRA_SLOT = "slot";
    static final String EXTRA_PAGE = "page";
    static final String EXTRA_SETUP = "setup";
    static final int NO_SLOT = -1;
    private int slot, page;
    private boolean setup;
    private Prefs prefs;
    private List<AppInfo> allApps;
    private ArrayAdapter<AppInfo> adapter;
    private final Set<String> selected = new HashSet<>();
    private ListView list;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_picker);
        Ui.applySystemBarPadding(findViewById(R.id.pickerRoot), true);
        prefs = new Prefs(this);
        slot = getIntent().getIntExtra(EXTRA_SLOT, NO_SLOT);
        page = getIntent().getIntExtra(EXTRA_PAGE, 0);
        setup = getIntent().getBooleanExtra(EXTRA_SETUP, false);
        TextView title = findViewById(R.id.pickerTitle);
        title.setText(setup ? (prefs.isSetupComplete() ? "Manage your apps" : "Choose your apps")
                : slot == NO_SLOT ? "Search apps" : "Choose an app");
        TextView description = findViewById(R.id.pickerDescription);
        if (setup) {
            description.setText(prefs.isSetupComplete()
                    ? "Select the apps Focus is allowed to show. You can update this list whenever you want in Settings."
                    : "Choose the apps you want on your Focus home screen. This is your final app list for now: apps you leave unchecked will not appear in Focus. You can change this list later in Settings.");
        } else {
            description.setText(slot == NO_SLOT
                    ? "Search and open apps you've permitted in Focus settings."
                    : "Choose an app for this home-screen tile.");
        }
        allApps = AppRepository.loadLaunchableApps(this);
        if (setup) { list = findViewById(R.id.appList); list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); if (prefs.isSetupComplete()) selected.addAll(prefs.getAllowedApps()); }
        else {
            Set<String> allowed = prefs.getAllowedApps();
            List<AppInfo> filtered = new ArrayList<>();
            for (AppInfo app : allApps) if (allowed.contains(app.packageName)) filtered.add(app);
            allApps = filtered;
        }
        adapter = new ArrayAdapter<>(this, R.layout.item_app_row, R.id.rowText, new ArrayList<>(allApps));
        list = findViewById(R.id.appList); list.setAdapter(adapter);
        if (setup) for (int i = 0; i < allApps.size(); i++) list.setItemChecked(i, selected.contains(allApps.get(i).packageName));
        if (setup) {
            Button done = findViewById(R.id.pickerDone);
            done.setVisibility(View.VISIBLE);
            done.setText(prefs.isSetupComplete() ? "Save app list" : "Confirm my app list");
            done.setOnClickListener(v -> {
                if (selected.isEmpty()) {
                    Toast.makeText(this, "Choose at least one app", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!prefs.isSetupComplete()) {
                    new android.app.AlertDialog.Builder(this)
                        .setTitle("This is your Focus app list")
                        .setMessage("Only the apps selected here will appear in Focus search and app tiles. You can change the list later from Settings. Continue with this selection?")
                        .setNegativeButton("Review list", null)
                        .setPositiveButton("Confirm and continue", (dialog, which) -> {
                            prefs.completeSetup(selected);
                            setResult(RESULT_OK);
                            finish();
                        }).show();
                } else {
                    prefs.setAllowedApps(selected);
                    setResult(RESULT_OK);
                    finish();
                }
            });
            list.setOnItemClickListener((parent, view, position, id) -> { AppInfo app = adapter.getItem(position); if (app != null) { if (!selected.add(app.packageName)) selected.remove(app.packageName); list.setItemChecked(position, selected.contains(app.packageName)); } });
        } else {
            list.setOnItemClickListener((parent, view, position, id) -> choose(adapter.getItem(position)));
        }
        EditText search = findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int before,int count){}
            public void afterTextChanged(Editable s) { filter(s.toString()); }
        });
        search.setOnEditorActionListener((v, actionId, event) -> { if (!setup && adapter.getCount() > 0) choose(adapter.getItem(0)); return true; });
    }
    private void filter(String query) {
        String q = query.trim().toLowerCase(Locale.getDefault()); List<AppInfo> matches = new ArrayList<>();
        for (AppInfo app : allApps) if (q.isEmpty() || app.label.toLowerCase(Locale.getDefault()).contains(q)) matches.add(app);
        adapter.clear(); adapter.addAll(matches);
        if (setup) for (int i = 0; i < matches.size(); i++) list.setItemChecked(i, selected.contains(matches.get(i).packageName));
    }
    private void choose(AppInfo app) {
        if (app == null || !prefs.isAllowed(app.packageName)) return;
        if (slot != NO_SLOT) prefs.setSlot(page, slot, app.packageName);
        else if (AppRepository.launch(this, app.packageName)) { finish(); return; }
        finish();
    }
}
