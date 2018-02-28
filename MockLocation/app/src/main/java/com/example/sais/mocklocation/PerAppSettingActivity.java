package com.example.sais.mocklocation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PerAppSettingActivity extends AppCompatActivity {

    //private Switch swtActive;

    private String pkgName;
    private SharedPreferences prefs;
    private Set<String> settingkeys;
    private Map<String, Object> initialSettings;
    private boolean allowRevoking;
    private Intent parentIntent;

    private Toolbar setting_toolbar;

    //GPS lat long
    private Switch gps_switch;
    private Button mapButton;

    //Cell
    private EditText lac_edittext;
    private EditText cid_edittext;
    private int lac;
    private int cid;
    private Switch cell_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_per_app_setting);

        setting_toolbar = (Toolbar)findViewById(R.id.toolbar_setting);
        setting_toolbar.setTitle("设置");
        setSupportActionBar(setting_toolbar);

        Intent i = getIntent();
        parentIntent = i;

        prefs = getSharedPreferences(Common.PREFS, Context.MODE_WORLD_READABLE);

        ApplicationInfo app;
        try{
            app = getPackageManager().getApplicationInfo(i.getStringExtra("package"), 0);
            pkgName = app.packageName;
        }catch(PackageManager.NameNotFoundException e){
            finish();
            return;
        }

        //display app information
        ((TextView)findViewById(R.id.app_name_setting)).setText(app.loadLabel(getPackageManager()));
        ((TextView)findViewById(R.id.pkg_name_setting)).setText(app.packageName);
        ((ImageView)findViewById(R.id.app_icon_setting)).setImageDrawable(app.loadIcon(getPackageManager()));

        mapButton = (Button)findViewById(R.id.MAP);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PerAppSettingActivity.this, MapActivity.class);
                startActivityForResult(intent,0x01 );
            }
        });

        //Update gps switch of active/inactive tweaks
        gps_switch = (Switch)findViewById(R.id.gps_switch);
        if(prefs.getBoolean(pkgName + Common.PREFS_GPS_ACTIVE, false)){
            gps_switch.setChecked(true);
            findViewById(R.id.gps_tweaks).setVisibility(View.VISIBLE);
        }else{
            gps_switch.setChecked(false);
            findViewById(R.id.gps_tweaks).setVisibility(View.GONE);
        }

        //Toggle the visibility of the gps setting panel when changed
        gps_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findViewById(R.id.gps_tweaks).setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        //Update Gps Field
        if(prefs.getBoolean(pkgName + Common.PREFS_GPS_ACTIVE, false)){
            ((EditText)findViewById(R.id.lat_editText)).setText(prefs.getString(pkgName + Common.PREFS_LATITUDE, ""));
            ((EditText)findViewById(R.id.log_editText)).setText(prefs.getString(pkgName + Common.PREFS_LONGITUDE, ""));
        }else{
            ((EditText)findViewById(R.id.lat_editText)).setText("");
            ((EditText)findViewById(R.id.log_editText)).setText("");
        }

        //update switch of active/inactive cell tweaks
        cell_switch = (Switch)findViewById(R.id.cell_switch);
        if(prefs.getBoolean(pkgName + Common.PREFS_CELL_ACTIVE, false)){
            cell_switch.setChecked(true);
            findViewById(R.id.cell_tweaks).setVisibility(View.VISIBLE);
        } else{
            cell_switch.setChecked(false);
            findViewById(R.id.cell_tweaks).setVisibility(View.GONE);
        }

        //toggle the visibility of the cell setting panel when changed
        cell_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findViewById(R.id.cell_tweaks).setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        //cell type spinner
        int cellType = prefs.getInt(pkgName + Common.PREFS_CELL_TYPE, Common.CELL_TYPE_GSM);
        Spinner spnType = (Spinner)findViewById(R.id.cell_type);
        String[] cellTypeArray = new String[]{
                getString(R.string.settings_celltype_gsm),
                getString(R.string.settings_celltype_lte),
                getString(R.string.settings_celltype_cdma),
                getString(R.string.settings_celltype_wcdma)
        };
        List<String> lstCellType = Arrays.asList(cellTypeArray);
        ArrayAdapter<String> cellTypeAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lstCellType);
        cellTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(cellTypeAdapter);
        spnType.setSelection(cellType);

        //update cell field
        if(prefs.getBoolean(pkgName + Common.PREFS_CELL_ACTIVE, false)){
            ((EditText)findViewById(R.id.mnc_editText)).setText(String.valueOf(
                    prefs.getInt(pkgName + Common.PREFS_MNC, 0)));
            ((EditText)findViewById(R.id.lac_editText)).setText(String.valueOf(
                    prefs.getInt(pkgName + Common.PREFS_LAC, 0)));
            ((EditText)findViewById(R.id.cid_editText)).setText(String.valueOf(
                    prefs.getInt(pkgName + Common.PREFS_CID, 0)));

        } else {
            ((EditText)findViewById(R.id.mnc_editText)).setText("0");
            ((EditText)findViewById(R.id.lac_editText)).setText("0");
            ((EditText)findViewById(R.id.cid_editText)).setText("0");
        }



        settingkeys = getSettingKeys();
        initialSettings = getSettings();

    }

    private Set<String> getSettingKeys(){
        HashSet<String> settingkeys = new HashSet<String>();
        settingkeys.add(pkgName + Common.PREFS_GPS_ACTIVE);
        settingkeys.add(pkgName + Common.PREFS_LATITUDE);
        settingkeys.add(pkgName + Common.PREFS_LONGITUDE);
        settingkeys.add(pkgName + Common.PREFS_CELL_ACTIVE);
        settingkeys.add(pkgName + Common.PREFS_CELL_TYPE);
        settingkeys.add(pkgName + Common.PREFS_MNC);
        settingkeys.add(pkgName + Common.PREFS_LAC);
        settingkeys.add(pkgName + Common.PREFS_CID);


        return settingkeys;
    }

    private Map<String, Object> getSettings(){
        Map<String, Object> settings = new HashMap<String, Object>();
        if(gps_switch.isChecked()){
            settings.put(pkgName + Common.PREFS_GPS_ACTIVE, true);

            String lat;
            String log;
            try{
                lat = ((EditText)findViewById(R.id.lat_editText)).getText().toString();
            }catch (Exception e){
                lat = "";
            }
            if(lat != ""){
                settings.put(pkgName + Common.PREFS_LATITUDE, lat);
            }
            try{
                log = ((EditText)findViewById(R.id.log_editText)).getText().toString();
            }catch (Exception e){
                log = "";
            }
            if(log != ""){
                settings.put(pkgName + Common.PREFS_LONGITUDE, log);
            }
        }

        if(cell_switch.isChecked()){
            settings.put(pkgName + Common.PREFS_CELL_ACTIVE, true);

            int cellType = ((Spinner)findViewById(R.id.cell_type)).getSelectedItemPosition();
            if(cellType > 0){
                settings.put(pkgName + Common.PREFS_CELL_TYPE, cellType);
            }

            int mnc;
            int lac;
            int cid;
            try{
                mnc = Integer.parseInt(((EditText)findViewById(R.id.mnc_editText)).getText().toString());
                lac = Integer.parseInt(((EditText)findViewById(R.id.lac_editText)).getText().toString());
                cid = Integer.parseInt(((EditText)findViewById(R.id.cid_editText)).getText().toString());
            }catch (Exception e){
                mnc = 0;
                lac = 0;
                cid = 0;
            }
            if(lac > 0 && cid > 0){
                settings.put(pkgName + Common.PREFS_MNC, mnc);
                settings.put(pkgName + Common.PREFS_LAC, lac);
                settings.put(pkgName + Common.PREFS_CID, cid);
            }
        }

        return settings;
    }

    @Override
    public void onBackPressed(){
        //If form wasn't changed, exit whitout prompting
        if(getSettings().equals(initialSettings)){
            finish();
            return;
        }

        //Require confirmation to exit the screen and lose the configuration changes
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.settings_unsaved_title);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setMessage(R.string.settings_unsaved_detail);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PerAppSettingActivity.this.finish();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        setResult(RESULT_OK, parentIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_app, menu);
        updateMenuEntries(getApplicationContext(), menu, pkgName);
        return true;
    }

    public static void updateMenuEntries(Context context, Menu menu, String pkgName){
        if(context.getPackageManager().getLaunchIntentForPackage(pkgName) == null){
            menu.findItem(R.id.menu_app_launch).setEnabled(false);
            Drawable icon = menu.findItem(R.id.menu_app_launch).getIcon().mutate();
            icon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            menu.findItem(R.id.menu_app_launch).setIcon(icon);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.menu_save){
            SharedPreferences.Editor prefsEditor = prefs.edit();
            Map<String, Object> newSettings = getSettings();
            for(String key : settingkeys){
                Object value = newSettings.get(key);
                if(value == null){
                    prefsEditor.remove(key);
                } else{
                    if(value instanceof Boolean){
                        prefsEditor.putBoolean(key, ((Boolean) value).booleanValue());
                    } else if(value instanceof Integer){
                        prefsEditor.putInt(key, ((Integer) value).intValue());
                    } else if (value instanceof String) {
                        prefsEditor.putString(key, (String) value);
                    } else if (value instanceof Set) {
                        prefsEditor.remove(key);
                        // Commit and reopen the editor, as it seems to be bugged when updating a StringSet
                        prefsEditor.commit();
                        prefsEditor = prefs.edit();
                        prefsEditor.putStringSet(key, (Set<String>) value);
                    } else {
                        // Should never happen
                        throw new IllegalStateException("Invalid setting type: " + key + "=" + value);
                    }

                }
            }
            prefsEditor.commit();

            //Update saved settings to detect modifications later
            initialSettings = newSettings;

            //check if in addition to saving the settings, the app should also be killed
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.settings_apply_title);
            builder.setMessage(R.string.settings_apply_detail);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //send the broadcast requesting to kill the app
                    Intent applyIntent = new Intent(Common.MY_PACKAGE_NAME + ".UPATE_PERMISSIONS");
                    applyIntent.putExtra("action", Common.ACTION_PERMISSIONS);
                    applyIntent.putExtra("Package", pkgName);
                    applyIntent.putExtra("kill", true);
                    sendBroadcast(applyIntent, Common.MY_PACKAGE_NAME + ".BROADCAST_PERSSION");

                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //send the broadcast but not requesting kill
                    Intent applyIntent = new Intent(Common.MY_PACKAGE_NAME + ".UPATE_PERMISSIONS");
                    applyIntent.putExtra("action", Common.ACTION_PERMISSIONS);
                    applyIntent.putExtra("Package", pkgName);
                    applyIntent.putExtra("kill", false);
                    sendBroadcast(applyIntent, Common.MY_PACKAGE_NAME + ".BROADCAST_PERSSION");

                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else if(item.getItemId() == R.id.menu_app_launch){
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkgName);
            startActivity(launchIntent);
        } else if(item.getItemId() == R.id.menu_app_settings){
            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + pkgName)));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 0x01 && resultCode == Activity.RESULT_OK){
            Double lat = data.getDoubleExtra("lat_map", 0);
            Double log = data.getDoubleExtra("log_map", 0);
            ((EditText)findViewById(R.id.lat_editText)).setText(lat.toString());
            ((EditText)findViewById(R.id.log_editText)).setText(log.toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 在布局中返回即保存配置
     * @param v
     */
//    public void save(View v){
//        String lat = lat_edittext.getText().toString();
//        String log = log_edittext.getText().toString();
//        Boolean gflag = gps_switch.isChecked();
//        service.save(perapp_pkg, lat, log, gflag);
//        Toast.makeText(getApplicationContext(), R.string.save, Toast.LENGTH_SHORT).show();
//    }
}
