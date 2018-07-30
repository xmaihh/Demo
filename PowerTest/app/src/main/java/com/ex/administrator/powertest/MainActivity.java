package com.ex.administrator.powertest;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private Button power_off;
    private Button power_reboot;
    private static final String TAG = MainActivity.class.getSimpleName()+"--->PowerTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        power_off = findViewById(R.id.power_off);
        power_reboot = findViewById(R.id.power_reboot);

        power_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "system service->shutdown");
                SystemProperties.set("ctl.start", "system_shutdown");
            }
        });


        power_reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "system service->reboot");
                SystemProperties.set("ctl.start", "system_reboot");
            }
        });
    }
}
