package com.administrator.sdcardappdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.android.internal.app.procstats.ProcessStats;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {

    private final static String PATH = "/mnt/internal_sd/baipu";
    private final static String FILENAME = "/test.txt";
    FileInputStream is;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void onWrite() {
        try {
            //判断是否存在sdcard
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File path = new File(PATH);
                File f = new File(PATH + FILENAME);
                if (!path.exists()) {
                    path.mkdirs();
                }
                if (!f.exists()) {
                    f.createNewFile();
                }
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(f));
                osw.write("\nthis is a txt!");
                osw.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String onRead() {
        try {
            File file = Environment.getExternalStorageDirectory();
            File fileDir = new File(PATH, "test.txt");

            is = new FileInputStream(fileDir);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] array = new byte[1024];
            int len = -1;
            while ((len = is.read(array)) != -1) {
                bos.write(array, 0, len);
            }
            bos.close();
            is.close();
            return bos.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "err";
    }

    public void onWrite(View view) {
        onWrite();
    }

    public void onRead(View view) {
        Toast.makeText(view.getContext(), onRead(), Toast.LENGTH_SHORT).show();
    }

    //  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    ///////////////////////////////////
}