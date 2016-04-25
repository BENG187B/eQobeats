package colburnsoftworks.quspmusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;

import colburnsoftworks.quspmusic.MusicService.MusicBinder;
import colburnsoftworks.quspmusic.HxMService.HxMBinder;
import colburnsoftworks.quspmusic.NeuroService.NeuroBinder;

import android.widget.MediaController.MediaPlayerControl;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity
        implements MediaPlayerControl, SharedPreferences.OnSharedPreferenceChangeListener {

    // Instance variables
    private ArrayList<Song> songList;
    private ListView songView;

    // Drawer variables
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;

    // Hamburger variables
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    // Music service variables
    public static MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    // HxM service variables
    private HxMService hxmSrv;
    private Intent hxmIntent;
    private boolean hxmBound = false;

    // NeuroService service variables
    public static NeuroService neuroSrv;
    private Intent neuroIntent;
    private boolean neuroBound = false;

    // MediaController variables
    public static MusicController controller;

    //activity and playback pause flags
    private boolean paused = false, playbackPaused = false;

    //gesture
    float downXValue = 0;
    float downYValue = 0;
    int screen_height = 0;
    int screen_width = 0;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.toolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Retrieve ListView
        songView = (ListView) findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        //Retrieve Drawer
        mDrawerList = (ListView) findViewById(R.id.navList);

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

        //Populate songList
        getSongList();

        //Alphabetize
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        // Display Songs
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

        // Drawer Hamburger Icon Setup
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Hamburger variables
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        // Hamburger helper method
        setupDrawer();

        // Setup media controls
        setController();

        // Get screen dimensions
        getScreenSize();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    //connect to the music service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    // connect to the hxm service
    private ServiceConnection hxmConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HxMBinder binder = (HxMBinder) service;
            //get service
            hxmSrv = binder.getService();
            hxmBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            hxmBound = false;
        }
    };

    // connect to the neuroscale service
    private ServiceConnection neuroConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NeuroBinder binder = (NeuroBinder) service;
            //get service
            neuroSrv = binder.getService();
            neuroBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            neuroBound = false;
        }
    };

    // set up the media controller
    private void setController() {
        //set the controller up
        controller = new MusicController(this);

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
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
        String[] osArray = {"Music", "About", "Setup", "Settings"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
    }

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        //Retrieve song info of .wav files
        /*ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = null;
        String sortOrder = null;
        String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("wav");
        String[] selectionArgsWav = new String[]{ mimeType };
        Cursor musicCursor = musicResolver.query(musicUri, null, selectionMimeType, selectionArgsWav, null);*/

        //Iterate over results
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int formatColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisFormat = musicCursor.getString(formatColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisFormat));
            }
            while (musicCursor.moveToNext());
        }
    }

    // Called when click on a Song
    public void songPicked(View view) {
        if (NeuroService.neuroReady) {
            musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
            musicSrv.playSong();
            if (playbackPaused) {
                setController();
                playbackPaused = false;
            }
            controller.show(0);
        } else {
            Toast.makeText(this, "The neuroservice is not ready yet. Please wait for 30s and try again", Toast.LENGTH_LONG).show();
        }
    }

    //play next
    private void playNext() {
        musicSrv.playNext();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    //play previous
    private void playPrev() {
        musicSrv.playPrev();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
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
        if (clickedPosition == 0) {
            return "Music";
        } else if (clickedPosition == 1) {
            return "Settings";
        } else if (clickedPosition == 2) {
            return "About";
        }

        return "Error";
    }

    // Use drawer position to open new activity
    private void drawerStartActivity(int clickedPosition) {
        if (clickedPosition == 0) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (clickedPosition == 3) {
            Intent intent = new Intent(this, UserSettingsActivity.class);
            startActivity(intent);
        } else if (clickedPosition == 1) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else if (clickedPosition == 2) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        /*if (key.equals(KEY_OPERATION) || key.equals(KEY_TARGET)) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }*/
        Toast.makeText(MainActivity.this, "Hello", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        // clean up music service
        stopService(playIntent);
        if (musicConnection != null) {
            unbindService(musicConnection);
        }
        musicSrv = null;

        //clean up hxm service
        stopService(hxmIntent);
        if (hxmConnection != null) {
            unbindService(hxmConnection);
        }
        hxmSrv = null;

        // clean up neuroscale service
        stopService(neuroIntent);
        if (neuroConnection != null) {
            unbindService(neuroConnection);
        }
        neuroSrv = null;

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
        /*getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);*/
        //showSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
        /*getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);*/
        //showSettings();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // Start music service
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        // Start hxm service
        if (hxmIntent == null) {
            hxmIntent = new Intent(this, HxMService.class);
            bindService(hxmIntent, hxmConnection, Context.BIND_AUTO_CREATE);
            startService(hxmIntent);
        }

        // start neuroscale service
        if (neuroIntent == null) {
            neuroIntent = new Intent(this, NeuroService.class);
            bindService(neuroIntent, neuroConnection, Context.BIND_AUTO_CREATE);
            startService(neuroIntent);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://colburnsoftworks.quspmusic/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://colburnsoftworks.quspmusic/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    public void showSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String operationMode = sharedPref.getString(UserSettingsActivity.KEY_OPERATION, "");
        String targetHeartRate = sharedPref.getString(UserSettingsActivity.KEY_TARGET, "");
        Toast.makeText(MainActivity.this, operationMode + " and " + targetHeartRate, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //the MediaController will hide after 3 seconds - Scroll to make it show again
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // store initial position
                downXValue = event.getX();
                downYValue = event.getY();
                break;
            }

            case MotionEvent.ACTION_UP: {
                // Get the final position
                float currentX = event.getX();
                float currentY = event.getY();

                //if sideways movement was greater
                if (Math.abs(downXValue - currentX) > Math.abs(downYValue
                        - currentY)) {
                    // pushing stuff to the right
                    if (downXValue < currentX) {
                    }
                    // pushing stuff to the left
                    if (downXValue > currentX) {
                    }
                }

                // vertical movement was greater
                else {
                    System.out.println("Initial Y: " + downYValue);
                    // moving finger down; show media player
                    if (downYValue > (screen_height - controller.getHeight())) {
                        System.out.println("Over player");
                    }
                    if (downYValue < currentY) {
                        controller.show();
                    }

                    // moving finger up; if over media player and its showing, open fullscreen
                    if (downYValue > currentY && controller.isShowing()
                            && downYValue > (screen_height - controller.getHeight())) {
                        System.out.println("Moved your finger up over the controller");
                    }
                }
                break;
            }
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    public void getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width= size.x;
        screen_height = size.y;
    }
}
