package com.titilife.demo.printservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.titilife.demo.printservice.usbprinter.UsbPrinterActivity;

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button usbPrinterBtn = findViewById(R.id.btn_usb_printer);
        usbPrinterBtn.setOnClickListener(this);
        int n = android.hardware.Camera.getNumberOfCameras();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_usb_printer:
                intentToUsbPrinterPage();
                break;
        }
    }

    private void intentToUsbPrinterPage() {
        Intent intent = new Intent();
        intent.setClass(this, UsbPrinterActivity.class);
        startActivity(intent);
    }
}
