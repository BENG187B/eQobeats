package colburnsoftworks.quspmusic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;
import android.widget.Toast;

public class MediaInfo extends AppCompatActivity
        implements MediaPlayerControl{

    // MediaController variables
    public static MusicController controller;

    //gesture
    float downXValue = 0;
    float downYValue = 0;
    int screen_height = 0;
    int screen_width = 0;

    //activity and playback pause flags
    private boolean paused = false, playbackPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MainActivity.hxmSrv.setContext(this);
        MainActivity.musicSrv.setContext(this);
        // Set text
        setText();
        // Setup media controls
        setController();
    }

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
        controller.setAnchorView(findViewById(R.id.bottomSpace));
        controller.setEnabled(true);
    }

    //play next
    private void playNext() {
        MainActivity.musicSrv.playNext();
        setText();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        //controller.show(0);
    }

    //play previous
    private void playPrev() {
        MainActivity.musicSrv.playPrev();
        setText();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        //controller.show(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
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
        if (MainActivity.musicSrv != null && MainActivity.musicBound && MainActivity.musicSrv.isPng())
            return MainActivity.musicSrv.getPosn();
        else return 0;
    }

    @Override
    public int getDuration() {
        if (MainActivity.musicSrv != null && MainActivity.musicBound && MainActivity.musicSrv.isPng())
            return MainActivity.musicSrv.getDur();
        else return 0;
    }

    @Override
    public boolean isPlaying() {
        if (MainActivity.musicSrv != null && MainActivity.musicBound)
            return MainActivity.musicSrv.isPng();
        return false;
    }

    @Override
    public void pause() {
        playbackPaused = true;
        MainActivity.musicSrv.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        MainActivity.musicSrv.seek(pos);
    }

    @Override
    public void start() {
        MainActivity.musicSrv.go();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        try{
            controller.show(0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setText() {
        TextView titleText = (TextView) findViewById(R.id.songTitle);
        titleText.setText(MainActivity.musicSrv.getSongTitle());
        TextView artistText = (TextView) findViewById(R.id.artistAlbum);
        artistText.setText(MainActivity.musicSrv.getSongArtist());
    }
}


