package com.example.sais.mocklocation;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ClearEditText et_search;

    private ArrayList<ApplicationInfo> mAppList = new ArrayList<ApplicationInfo>();
    private ArrayList<ApplicationInfo> filteredAppList = new ArrayList<ApplicationInfo>();

    private AppAdapter mAppAdapter;
    private CharSequence nameFilter;

    private List<SettingInfo> settings;

    private static File prefsFile = new File(Environment.getDataDirectory(),
            "data/" + Common.MY_PACKAGE_NAME +"/shared_prefs/" + Common.PREFS + ".xml");
    //导出配置文件
    private static File backupPrefsFile = new File(Environment.getExternalStorageDirectory(),
            "AppSettings-Backup.xml");
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTitle(R.string.app_name);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle("位置模拟");
        setSupportActionBar(mToolbar);

        prefsFile.setReadable(true, false);
        prefs = getSharedPreferences(Common.PREFS, Context.MODE_WORLD_READABLE);
        loadSettings();



        ListView list = (ListView)findViewById(R.id.app_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //open settings activity when clicking on an application
                String pkgName = ((TextView) view.findViewById(R.id.pkg_name)).getText().toString();
                Intent i = new Intent(getApplicationContext(), PerAppSettingActivity.class);
                i.putExtra("package", pkgName);
                startActivityForResult(i, position);
            }
        });

        refreshApps();
    }

    private void loadSettings(){
        settings = new ArrayList<SettingInfo>();

        settings.add(new SettingInfo(Common.PREFS_LATITUDE, getString(R.string.settings_latitude)));
        settings.add(new SettingInfo(Common.PREFS_LONGITUDE, getString(R.string.settings_longitude)));
        settings.add(new SettingInfo(Common.PREFS_LAC, getString(R.string.settings_lac)));
        settings.add(new SettingInfo(Common.PREFS_CID, getString(R.string.settings_cid)));
        settings.add(new SettingInfo(Common.PREFS_MNC, getString(R.string.settings_mnc)));
        settings.add(new SettingInfo(Common.PREFS_CELL_TYPE, getString(R.string.settings_celltype)));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        //refresh the app that was just edited, if it's visible in the list
        ListView list = (ListView)findViewById(R.id.app_list);
        if (requestCode >= list.getFirstVisiblePosition() &&
                requestCode <= list.getLastVisiblePosition()) {
            View v = list.getChildAt(requestCode - list.getFirstVisiblePosition());
            list.getAdapter().getView(requestCode, v, list);
        } else if (requestCode == Integer.MAX_VALUE) {
            list.invalidateViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_refresh:
                refreshApps();
                return true;
            case R.id.menu_export:
                doExport();
                return true;
            case R.id.menu_import:
                doImport();
                return true;
            case R.id.menu_about:
                showAboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshApps(){
        mAppList.clear();
        //reload the list of apps in the background
        new PrepareAppsAdapter().execute();
    }

    private void doExport(){
        new ExportTask().execute(backupPrefsFile);
    }

    private void doImport(){
        if(!backupPrefsFile.exists()){
            Toast.makeText(this, getString(R.string.imp_exp_file_doesnt_exist, backupPrefsFile.getAbsolutePath()),
                    Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.menu_import);
        builder.setMessage(R.string.imp_exp_confirm);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                new ImportTask().execute(backupPrefsFile);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                //do nothing
                dialog.dismiss();
            }
        });
    }

    private class ExportTask extends AsyncTask<File, String, String>{
        protected String doInBackground(File... params){
            File outFile = params[0];
            try{
                copyFile(prefsFile, outFile);
                return getString(R.string.imp_exp_exported, outFile.getAbsolutePath());
            }catch(IOException ex){
                return getString(R.string.imp_exp_export_error, ex.getMessage());
            }
        }

        protected void onPostExecute(String result){
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }

    protected class ImportTask extends AsyncTask<File, String, String>{
        private boolean importSuccessful;

        protected String doInBackground(File... params){
            importSuccessful = false;
            File inFile = params[0];
            String tempFileName = Common.PREFS+"-new";
            File newPrefsFile = new File(prefsFile.getParentFile(), tempFileName + ".xml");
            //make sure the shared_prefs foleder exists
            getSharedPreferences(tempFileName, Context.MODE_WORLD_READABLE).edit().commit();
            try{
                copyFile(inFile, newPrefsFile);
            }catch(IOException ex){
                return getString(R.string.imp_exp_import_error, ex.getMessage());
            }

            //仅对文件操作者可读
            newPrefsFile.setReadable(true, false);
            SharedPreferences newPrefs = getSharedPreferences(tempFileName, Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
            if(newPrefs.getAll().size() == 0){
                //No entries in imported file, discard it
                newPrefsFile.delete();
                return getString(R.string.imp_exp_invalid_import_file, inFile.getAbsolutePath());
            }
            else{
                if(newPrefsFile.renameTo(prefsFile)){
                    importSuccessful = true;
                }
                else{
                    prefsFile.delete();
                    if(newPrefsFile.renameTo(prefsFile))
                        importSuccessful = true;
                }
                return getString(R.string.imp_exp_imported);
            }
        }

        protected void onPostExecute(String result){
            if(importSuccessful){
                //refresh preferences
                prefs = getSharedPreferences(Common.PREFS, Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
                //refresh listed apps
                AppAdapter appAdapter = (AppAdapter)((ListView)findViewById(R.id.app_list)).getAdapter();
                appAdapter.getFilter().filter(nameFilter);
            }

            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }

    private static void copyFile(File source, File destiny) throws IOException{
        InputStream in = null;
        OutputStream out = null;
        boolean success = false;
        try{
            in = new FileInputStream(source);
            out = new FileOutputStream(destiny);
            byte[] buf = new byte[10*1024];
            int len;
            while((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
            out = null;
            success = true;
        }catch (IOException ex){
            throw ex;
        }finally{
            if(in != null){
                try{
                    in.close();
                }catch (Exception e){

                }
            }
            if(out != null){
                try{
                    out.close();
                }catch (Exception e){

                }
            }
            if(!success){
                destiny.delete();
            }
        }
    }

    private void showAboutDialog(){
        View vAbout;
        vAbout = getLayoutInflater().inflate(R.layout.about, null);

        //warning if the module is not active
        if(!isModActive())
            vAbout.findViewById(R.id.about_notactive).setVisibility(View.VISIBLE);

        //clickable links
        ((TextView)vAbout.findViewById(R.id.about_title)).setMovementMethod(LinkMovementMethod.getInstance());

        //prepare and show the dialog
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setTitle(R.string.app_name);
        dlgBuilder.setCancelable(true);
        //dlgBuilder.setIcon(R.drawable.ic_launcher);
        dlgBuilder.setPositiveButton(android.R.string.ok, null);
        dlgBuilder.setView(vAbout);
        dlgBuilder.show();
    }

    private static boolean isModActive(){
        return false;
    }

    private void loadApps(){
        mAppList.clear();

        PackageManager pm = this.getPackageManager();
        //return a list of all installed packages in the device
        List<PackageInfo> mPackageInfo = pm.getInstalledPackages(0);
        //dialog.setMax(mPackageInfo.size());
        //int i = 1;
        for(PackageInfo pkgInfo : mPackageInfo){
            //dialog.setProgress(i++);

            //非系统应用
            if((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                ApplicationInfo info = pkgInfo.applicationInfo;
                if(info == null)
                    continue;

                info.name = info.loadLabel(pm).toString();
                mAppList.add(info);
            }
        }

        Collections.sort(mAppList, new Comparator<ApplicationInfo>() {
            @Override
            public int compare(ApplicationInfo lhs, ApplicationInfo rhs) {
                if (lhs.name == null) {
                    return -1;
                } else if (rhs.name == null) {
                    return 1;
                } else {
                    return lhs.name.toUpperCase().compareTo(rhs.name.toUpperCase());
                }
            }
        });

    }

    private void initView(){
        final AppAdapter appListAdapter = new AppAdapter(MainActivity.this, mAppList);

        ((ListView)findViewById(R.id.app_list)).setAdapter(appListAdapter);
        appListAdapter.getFilter().filter(nameFilter);
        et_search = (ClearEditText)findViewById(R.id.et_search);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameFilter = s;
                appListAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //handle background loading of apps
    private class PrepareAppsAdapter extends AsyncTask<Void, Void, AppAdapter>{
        ProgressDialog dialog;

        @Override
        protected void onPreExecute(){
//            dialog = new ProgressDialog(((ListView)findViewById(R.id.app_list)).getContext());
//            dialog.setMessage(getString(R.string.app_loading));
//            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            dialog.setCancelable(false);
//            dialog.show();
        }

        protected AppAdapter doInBackground(Void... params){
            if(mAppList.size() == 0){
                loadApps();
            }
            return null;
        }

        protected void onPostExecute(final AppAdapter result){
            initView();
            try{
                dialog.dismiss();
            }catch(Exception e){

            }
        }
    }

    private static class SettingInfo{
        String settingkey;
        String label;

        SettingInfo(String setting, String label){
            this.settingkey = setting;
            this.label = label;
        }
    }

    private class AppListFilter extends Filter{
        private AppAdapter adapter;

        AppListFilter(AppAdapter adapter){
            super();
            this.adapter = adapter;
        }

        protected FilterResults performFiltering(CharSequence constraint){
            ArrayList<ApplicationInfo> items = new ArrayList<ApplicationInfo>();
            synchronized (this){
                items.addAll(mAppList);
            }

            FilterResults result = new FilterResults();
            if(constraint != null && constraint.length() > 0){
                Pattern regexp = Pattern.compile(constraint.toString(), Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
                for(Iterator<ApplicationInfo> i = items.iterator(); i.hasNext();){
                    ApplicationInfo app = i.next();
                    if(!regexp.matcher(app.name == null ? "" : app.name).find()
                        &&!regexp.matcher(app.packageName).find()){
                        i.remove();
                    }
                }
            }

            result.values = items;
            result.count = items.size();

            return result;
        }

        protected void publishResults(CharSequence constraint, FilterResults results){
            filteredAppList = (ArrayList<ApplicationInfo>)results.values;
            adapter.notifyDataSetChanged();
            adapter.clear();
            for(int i = 0; i<filteredAppList.size(); i++){
                adapter.add(filteredAppList.get(i));
            }
            adapter.notifyDataSetInvalidated();
        }
    }

    static class ViewHolder {
        public TextView appName;
        public TextView pkgName;
        public ImageView appIcon;
        AsyncTask<ViewHolder, Void, Drawable> imageLoader;
    }

    class AppAdapter extends ArrayAdapter implements SectionIndexer {

        private Map<String, Integer> alphaIndexer;
        private String[] sections;
        private Filter filter;
        private LayoutInflater inflater;
        private Drawable defaultIcon;

        public AppAdapter(Context context, List<ApplicationInfo> items){
            super(context, R.layout.app_item, new ArrayList<ApplicationInfo>(items));

            filteredAppList.addAll(items);

            filter = new AppListFilter(this);
            inflater = getLayoutInflater();
            defaultIcon = getResources().getDrawable(android.R.drawable.sym_def_app_icon);

            alphaIndexer = new HashMap<String, Integer>();
            for(int i = filteredAppList.size() - 1; i >= 0; i--){
                ApplicationInfo app = filteredAppList.get(i);
                String appName = app.name;
                String firstChar;
                if(appName == null || appName.length() < 1){
                    firstChar = "@";
                }else {
                    firstChar = appName.substring(0, 1).toUpperCase();
                    if(firstChar.charAt(0) > 'Z' || firstChar.charAt(0) < 'A')
                        firstChar = "@";
                }

                alphaIndexer.put(firstChar, i);
            }

            Set<String> sectionLetters = alphaIndexer.keySet();

            //create a list from the set to sort
            //根据应用名的首字母(key)进行排序
            List<String> sectionList = new ArrayList<String>(sectionLetters);
            Collections.sort(sectionList);
            sections = new String[sectionList.size()];
            sectionList.toArray(sections);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View row = convertView;
            ViewHolder holder;
            if(row == null){
                row = inflater.inflate(R.layout.app_item, parent, false);
                holder = new ViewHolder();
                holder.appName = (TextView)row.findViewById(R.id.app_name);
                holder.appIcon = (ImageView)row.findViewById(R.id.app_icon);
                holder.pkgName = (TextView)row.findViewById(R.id.pkg_name);
                row.setTag(holder);
            }else{
                holder = (ViewHolder)row.getTag();
                holder.imageLoader.cancel(true);
            }

            final ApplicationInfo app = filteredAppList.get(position);

            holder.appName.setText(app.name == null ? "" : app.name);
            holder.pkgName.setText(app.packageName);
            holder.pkgName.setTextColor(((prefs.getBoolean(app.packageName + Common.PREFS_GPS_ACTIVE, false) || prefs.getBoolean(app.packageName + Common.PREFS_CELL_ACTIVE, false))
                    ? Color.RED : Color.parseColor("#0099CC")));
            holder.appIcon.setImageDrawable(defaultIcon);

            if (app.enabled) {
                holder.appName.setPaintFlags(holder.appName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                holder.pkgName.setPaintFlags(holder.pkgName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.appName.setPaintFlags(holder.appName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.pkgName.setPaintFlags(holder.pkgName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            holder.imageLoader = new AsyncTask<ViewHolder, Void, Drawable>() {
                private ViewHolder v;

                @Override
                protected Drawable doInBackground(ViewHolder... params) {
                    v = params[0];
                    return app.loadIcon(getPackageManager());
                }

                @Override
                protected void onPostExecute(Drawable result) {
                    v.appIcon.setImageDrawable(result);
                }
            }.execute(holder);

            return row;
        }

        public void notifyDataSetInvalidated() {
            alphaIndexer.clear();
            for (int i = filteredAppList.size() - 1; i >= 0; i--) {
                ApplicationInfo app = filteredAppList.get(i);
                String appName = app.name;
                String firstChar;
                if (appName == null || appName.length() < 1) {
                    firstChar = "@";
                } else {
                    firstChar = appName.substring(0, 1).toUpperCase();
                    if (firstChar.charAt(0) > 'Z' || firstChar.charAt(0) < 'A')
                        firstChar = "@";
                }
                alphaIndexer.put(firstChar, i);
            }

            Set<String> keys = alphaIndexer.keySet();
            Iterator<String> it = keys.iterator();
            ArrayList<String> keyList = new ArrayList<String>();
            while (it.hasNext()) {
                keyList.add(it.next());
            }

            Collections.sort(keyList);
            sections = new String[keyList.size()];
            keyList.toArray(sections);

            super.notifyDataSetInvalidated();
        }

        public int getPositionForSection(int section) {
            if (section >= sections.length)
                return filteredAppList.size() - 1;

            return alphaIndexer.get(sections[section]);
        }

        public int getSectionForPosition(int position) {

            // Iterate over the sections to find the closest index
            // that is not greater than the position
            int closestIndex = 0;
            int latestDelta = Integer.MAX_VALUE;

            for (int i = 0; i < sections.length; i++) {
                int current = alphaIndexer.get(sections[i]);
                if (current == position) {
                    // If position matches an index, return it immediately
                    return i;
                } else if (current < position) {
                    // Check if this is closer than the last index we inspected
                    int delta = position - current;
                    if (delta < latestDelta) {
                        closestIndex = i;
                        latestDelta = delta;
                    }
                }
            }

            return closestIndex;
        }

        public Object[] getSections() {
            return sections;
        }

        @Override
        public Filter getFilter() {
            return filter;
        }

    }

}
