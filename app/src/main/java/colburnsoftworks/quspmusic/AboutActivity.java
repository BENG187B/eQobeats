package colburnsoftworks.quspmusic;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AboutActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_about);

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

        else if(clickedPosition==3){
            Intent intent = new Intent(this, UserSettingsActivity.class);
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

        return;
    }
}
