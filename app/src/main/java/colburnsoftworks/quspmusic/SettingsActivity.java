package colburnsoftworks.quspmusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import zephyr.android.HxMBT.*;

public class SettingsActivity extends AppCompatActivity {

    // Drawer variables
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;

    // Hamburger variables
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Retrieve Drawer
        mDrawerList = (ListView)findViewById(R.id.navList);

        // Populate Drawer
        addDrawerItems();

        // Drawer OnClick Listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this, drawerToText(position), Toast.LENGTH_SHORT).show();
                drawerStartActivity(position);
            }
        });

        // Drawer Hamburger Icon Setup
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Hamburger variables
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        // Hamburger helper method
        setupDrawer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addDrawerItems() {
        String[] osArray = { "Music", "About", "Setup", "Settings" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
    }

    // Hamburger helper method
    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state.
             public void onDrawerOpened(View drawerView) {
             super.onDrawerOpened(drawerView);
             getSupportActionBar().setTitle("Navigation!");
             invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }/*

             /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    // Keep hamburger icon in sync
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    // Convert drawer position to text
    private String drawerToText(int clickedPosition) {
        if(clickedPosition==0){
            return "Music";
        }

        else if(clickedPosition==1){
            return "Settings";
        }

        else if(clickedPosition==2){
            return "About";
        }

        return "Error";
    }

    // Use drawer position to open new activity
    private void drawerStartActivity(int clickedPosition) {
        if(clickedPosition==0){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        else if(clickedPosition==1){
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        else if(clickedPosition==2){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        else if(clickedPosition==3){
            Intent intent = new Intent(this, UserSettingsActivity.class);
            startActivity(intent);
        }
    }

    public void showSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String operationMode = sharedPref.getString(UserSettingsActivity.KEY_OPERATION, "");
        String targetHeartRate = sharedPref.getString(UserSettingsActivity.KEY_TARGET, "");
        Toast.makeText(SettingsActivity.this, operationMode + " and " + targetHeartRate, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);*/
        //showSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);*/
        //showSettings();
    }

    public void testMessagePack() throws Exception{
        String src1 = "first message";
        String src2 = "Second message";
        // String out = MsgPak.packMessage(src1, src2);
        //String out = MsgPak.unpackMessage(src1, src2);
        //Toast.makeText(SettingsActivity.this, out, Toast.LENGTH_LONG).show();
    }
}