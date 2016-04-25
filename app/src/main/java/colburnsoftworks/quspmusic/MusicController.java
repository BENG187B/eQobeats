package colburnsoftworks.quspmusic;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;

/**
 * Created by Admin on 4/19/2016.
 */
public class MusicController extends MediaController {

    private Button fullscreenButton;
    public MusicController(Context context) {
        super(context);
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        Button fullscreenButton = new Button(getContext());
        fullscreenButton.setText("Fullscreen");
        fullscreenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        addView(fullscreenButton, params);
    }

    /*@Override
    public void hide() {
        //do Nothing
    }*/

    //Handle BACK button
    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            super.hide();//Hide mediaController
            ((Activity) getContext()).finish();
            //finish();//Close this activity
            return true;//If press Back button, finish here
        }
        //If not Back button, other button (volume) work as usual.
        return super.dispatchKeyEvent(event);
    }*/


}
