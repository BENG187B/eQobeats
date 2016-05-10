package colburnsoftworks.quspmusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;

/**
 * Created by Admin on 4/19/2016.
 */
public class MusicController extends MediaController {

    public ImageButton fullscreenButton;
    private Context ctx;
    public MusicController(Context context) {
        super(context, false);
        ctx = context;
    }


    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        fullscreenButton = new ImageButton(getContext());
        fullscreenButton.setImageResource(R.drawable.fullscreen);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        params.setMargins(0,0,0,0);
        addView(fullscreenButton, params);
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFullscreen();
            }
        });
    }

    /*@Override
    public void hide() {
    }*/

    public void toggleFullscreen(){
        if (ctx.getClass().getSimpleName().equals("MediaInfo")) {
            Intent intent = new Intent(ctx, MainActivity.class);
            ctx.startActivity(intent);
        } else {
            Intent intent = new Intent(ctx, MediaInfo.class);
            ctx.startActivity(intent);
        }
    }
}
