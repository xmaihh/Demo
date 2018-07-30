package com.administrator.dualscreendemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends Activity {
    private final String TAG = "PresentationActivity";

    private Button btnPre;
    private Button btnNext;
    private ImageView img;
    private ImageView mFullScreenImage;
    private int position = 0;
    private DisplayManager mDisplayManager;
    private Display[] displays;

    private PresentationDemo mPresentation;

    private int photoId[] = {R.mipmap.test1, R.mipmap.test2, R.mipmap.test3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        initView();
        setDefaultImage();
        displays = mDisplayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        show();

        Display[] displayss = mDisplayManager.getDisplays();
        Log.d(TAG, "onCreate: " + displayss.length + "\t" + displays.length);
    }

    private void initView() {
        btnPre = (Button) findViewById(R.id.pre);
        btnNext = (Button) findViewById(R.id.next);
        img = (ImageView) findViewById(R.id.preview_img);
    }

    private void setDefaultImage() {
        img.setImageResource(photoId[position]);
        if (mFullScreenImage != null) {
            mFullScreenImage.setImageResource(photoId[position]);
        }
    }

    private void show() {
        Log.e(TAG, "00000000000000000000000>>>" + displays.length);
        for (int i = 0; i < displays.length; i++) {
            final Display display = displays[i];
            if (display != null)
                showPresentation(display);
        }
    }


    private void showPresentation(Display display) {
        mPresentation = new PresentationDemo(this, display);
        mPresentation.show();
        mFullScreenImage = mPresentation.getImageView();
    }

    private void hidePresentation(Display display) {

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pre:
                if (position > 0) {
                    position--;
                    setDefaultImage();
                } else if (position < 2) {
                    position++;
                    setDefaultImage();
                }
                break;
            case R.id.next:
                if (position < 2) {
                    position++;
                    setDefaultImage();
                } else if (position > 0) {
                    position--;
                    setDefaultImage();
                }
                break;
        }
    }
}
