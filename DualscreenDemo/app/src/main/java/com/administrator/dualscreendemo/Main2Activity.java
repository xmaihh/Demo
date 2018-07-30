package com.administrator.dualscreendemo;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Administrator on 2018/7/6 0006.
 */

public class Main2Activity extends Activity {

    private DifferentDislay mPresentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        DisplayManager manager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = manager.getDisplays();
        // displays[0] 主屏
        // displays[1] 副屏
        DifferentDislay differentDislay = new DifferentDislay(this, displays[1]);
        differentDislay.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        differentDislay.show();
    }

    public class DifferentDislay extends Presentation {

        public DifferentDislay(Context outerContext, Display display) {
            super(outerContext, display);

        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.second_screen);

        }
    }
}
