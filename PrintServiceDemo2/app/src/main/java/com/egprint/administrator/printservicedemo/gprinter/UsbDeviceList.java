package com.egprint.administrator.printservicedemo.gprinter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.egprint.administrator.printservicedemo.R;
import com.egprint.administrator.printservicedemo.project.util.PreferencesUtils;
import com.egprint.administrator.printservicedemo.usbprint.Constants;

import java.util.HashMap;
import java.util.Iterator;


public class UsbDeviceList extends Activity {
    // Debugging
    private static final String DEBUG_TAG = "DeviceListActivity";
    // Member fields
    private ListView lvUsbDevice = null;
    private ArrayAdapter<String> mUsbDeviceArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.dialog_usb_list);
        lvUsbDevice = (ListView) findViewById(R.id.lvUsbDevices);
        mUsbDeviceArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.usb_device_name_item);
        lvUsbDevice.setOnItemClickListener(mDeviceClickListener);
        lvUsbDevice.setAdapter(mUsbDeviceArrayAdapter);
        getUsbDeviceList(this);
    }

    void messageBox(String err) {
        Toast.makeText(getApplicationContext(),
                err, Toast.LENGTH_SHORT).show();
    }

    boolean checkUsbDevicePidVid(UsbDevice dev) {
        int pid = dev.getProductId();
        int vid = dev.getVendorId();
        boolean rel = false;
        if (
                (vid == 34918 && pid == 256)
                        || (vid == 1137 && pid == 85)
                        || (vid == 6790 && pid == 30084)
                        || (vid == 26728 && pid == 256)
                        || (vid == 26728 && pid == 512)
                        || (vid == 26728 && pid == 256)
                        || (vid == 26728 && pid == 768)
                        || (vid == 26728 && pid == 1024)
                        || (vid == 26728 && pid == 1280)
                        || (vid == 26728 && pid == 1536)
                ) {
            rel = true;
        }
        return rel;
    }

    public void getUsbDeviceList(Context ctx) {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        // Get the list of attached devices
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = devices.values().iterator();
        int count = devices.size();
        Log.d(DEBUG_TAG, "count " + count);
        if (count > 0) {
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();
                String devicename = device.getDeviceName();
                Log.d("chensy", "getUsbDeviceList: " + "DeviceId:" + device.getDeviceId() + ", ProductId:" + device.getProductId() + ", VendorId:" + device.getVendorId());
//                String devicename = "DeviceId:" + device.getDeviceId() + ", ProductId:" + device.getProductId() + ", VendorId:" + device.getVendorId();
                if (checkUsbDevicePidVid(device)) {
                    mUsbDeviceArrayAdapter.add(devicename);
                    PreferencesUtils.putString(ctx, "device_name", "DeviceId:" + device.getDeviceId() + ", ProductId:" + device.getProductId() + ", VendorId:" + device.getVendorId());
                }
            }
        } else {
            String noDevices = "未连接USB打印机";
            Log.d(DEBUG_TAG, "noDevices " + noDevices);
            mUsbDeviceArrayAdapter.add(noDevices);
        }
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String noDevices = "未连接USB打印机";
            if (!info.equals(noDevices)) {
                String address = info;
                // Create the result Intent and include the MAC address
                Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_DEVICE_ADDRESS, address);
                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
            }
            finish();
        }
    };
}
