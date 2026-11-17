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
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Minimal multi-page launcher. Every app-facing surface is filtered through the approved-app allowlist. */
public class HomeActivity extends Activity {
    private static final int COLUMNS = 2, ROWS = 3, TILE_MARGIN_DP = 6;
    private static final float EMPTY_PLUS_SP = 56f;
    private Prefs prefs;
    private LinearLayout grid;
    private int currentPage = 0;
    private final ColorFilter grayFilter = makeGrayFilter();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_home);
        prefs = new Prefs(this); grid = findViewById(R.id.grid); Ui.applySystemBarPadding(findViewById(R.id.root), false);
        if (!prefs.isSetupComplete()) { openSetup(); return; }
        findViewById(R.id.allApps).setOnClickListener(v -> startActivity(new Intent(this, AppPickerActivity.class)));
        findViewById(R.id.header).setOnLongClickListener(v -> { showSettings(); return true; });
        findViewById(R.id.previousPage).setOnClickListener(v -> { if (currentPage > 0) { currentPage--; render(); } });
        findViewById(R.id.nextPage).setOnClickListener(v -> { if (currentPage < prefs.getPageCount() - 1) { currentPage++; render(); } });
        findViewById(R.id.addPage).setOnClickListener(v -> addPage());
        findViewById(R.id.pageName).setOnClickListener(v -> managePage());
        findViewById(R.id.pageName).setOnLongClickListener(v -> { managePage(); return true; });
    }
    private void openSetup() { Intent i = new Intent(this, AppPickerActivity.class); i.putExtra(AppPickerActivity.EXTRA_SETUP, true); startActivityForResult(i, 10); }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) { super.onActivityResult(requestCode,resultCode,data); if (requestCode == 10) { if (prefs.isSetupComplete()) render(); else finish(); } }
    @Override protected void onResume() { super.onResume(); if (prefs != null && prefs.isSetupComplete()) { if (currentPage >= prefs.getPageCount()) currentPage = prefs.getPageCount()-1; render(); } }

    private void render() {
        grid.removeAllViews(); LayoutInflater inflater = getLayoutInflater(); int margin = Ui.dp(this, TILE_MARGIN_DP);
        for (int r=0, slot=0; r<ROWS; r++) {
            LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
            grid.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f));
            for (int c=0;c<COLUMNS;c++,slot++) {
                View tile = inflater.inflate(R.layout.item_tile,row,false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1f); lp.setMargins(margin,margin,margin,margin); row.addView(tile,lp); bindTile(tile,slot);
            }
        }
        TextView name = findViewById(R.id.pageName); name.setText(prefs.getPageName(currentPage) + "  •  " + (currentPage+1) + "/" + prefs.getPageCount());
        findViewById(R.id.previousPage).setAlpha(currentPage == 0 ? 0.25f : 1f);
        findViewById(R.id.nextPage).setAlpha(currentPage == prefs.getPageCount()-1 ? 0.25f : 1f);
    }
    private void bindTile(View tile, int slot) {
        ImageView icon=tile.findViewById(R.id.tileIcon); TextView label=tile.findViewById(R.id.tileLabel);
        String pkg=prefs.getSlot(currentPage,slot);
        if (pkg != null && !prefs.isAllowed(pkg)) { prefs.setSlot(currentPage,slot,null); pkg=null; }
        Drawable drawable=pkg==null?null:AppRepository.loadIcon(this,pkg);
        if (pkg==null || drawable==null) {
            icon.setVisibility(View.GONE); label.setText("+"); label.setTextSize(TypedValue.COMPLEX_UNIT_SP,EMPTY_PLUS_SP);
            tile.setOnClickListener(v->openPicker(slot)); tile.setOnLongClickListener(v->{openPicker(slot);return true;}); return;
        }
        icon.setVisibility(View.VISIBLE); icon.setImageDrawable(drawable); icon.setColorFilter(prefs.isGrayscale()?grayFilter:null); label.setText(AppRepository.labelOf(this,pkg));
        final String app=pkg; tile.setOnClickListener(v->{ if (!prefs.isAllowed(app) || !AppRepository.launch(this,app)) Toast.makeText(this,"Can't open that app",Toast.LENGTH_SHORT).show(); });
        tile.setOnLongClickListener(v->{showTileMenu(slot,app);return true;});
    }
    private void openPicker(int slot) { Intent i=new Intent(this,AppPickerActivity.class); i.putExtra(AppPickerActivity.EXTRA_SLOT,slot); i.putExtra(AppPickerActivity.EXTRA_PAGE,currentPage); startActivity(i); }
    private void showTileMenu(int slot,String pkg) {
        new AlertDialog.Builder(this).setTitle(AppRepository.labelOf(this,pkg)).setItems(new String[]{"Change app","Remove from page"},(d,w)->{if(w==0)openPicker(slot);else{prefs.setSlot(currentPage,slot,null);render();}}).show();
    }
    private void addPage() {
        EditText input=new EditText(this); input.setSingleLine(true); input.setHint("Page name");
        new AlertDialog.Builder(this).setTitle("New page").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{String n=input.getText().toString().trim(); if(n.isEmpty())n="Page "+(prefs.getPageCount()+1); prefs.addPage(n); currentPage=prefs.getPageCount()-1; render();}).show();
    }
    private void managePage() {
        String[] options={"Rename page","Delete page"}; new AlertDialog.Builder(this).setTitle(prefs.getPageName(currentPage)).setItems(options,(d,w)->{
            if(w==0){EditText input=new EditText(this);input.setSingleLine(true);input.setText(prefs.getPageName(currentPage));new AlertDialog.Builder(this).setTitle("Rename page").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Save",(x,y)->{String n=input.getText().toString().trim();if(!n.isEmpty())prefs.setPageName(currentPage,n);render();}).show();}
            else if(prefs.getPageCount()>1){new AlertDialog.Builder(this).setMessage("Delete this page and its tile arrangement?").setNegativeButton("Cancel",null).setPositiveButton("Delete",(x,y)->{prefs.deletePage(currentPage);currentPage=Math.max(0,Math.min(currentPage,prefs.getPageCount()-1));render();}).show();}
            else Toast.makeText(this,"Keep at least one page",Toast.LENGTH_SHORT).show();
        }).show();
    }
    private void showSettings() {
        String[] items={"Grayscale icons: "+(prefs.isGrayscale()?"ON":"OFF"),"Manage permitted apps","Set as default home app"};
        new AlertDialog.Builder(this).setTitle("Launcher settings").setItems(items,(d,w)->{if(w==0){prefs.setGrayscale(!prefs.isGrayscale());render();}else if(w==1){Intent i=new Intent(this,AppPickerActivity.class);i.putExtra(AppPickerActivity.EXTRA_SETUP,true);startActivity(i);}else openHomeSettings();}).show();
    }
    private void openHomeSettings(){try{startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}catch(ActivityNotFoundException e){startActivity(new Intent(Settings.ACTION_SETTINGS));}}
    private static ColorFilter makeGrayFilter(){ColorMatrix m=new ColorMatrix();m.setSaturation(0f);return new ColorMatrixColorFilter(m);}
}
