package com.ex.administrator.hubscanning;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxTimeUtil.interval(100, new RxTimeUtil.IRxNext() {
            @Override
            public void doNext(long number) {
//                Log.d("chensy", "doNext: ");
//                Hubscanning();
            }
        });


        Button click = findViewById(R.id.click);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Hubscanning();
            }
        });
    }


    private void Hubscanning() {
        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
//        Log.d("chensy", "Hubscanning: deviceList.size()" + deviceList.size());
        List<UsbDevice> data = new ArrayList<>();

        for (UsbDevice item : deviceList.values()) {
            if (item.getInterfaceCount() > 0) {
                if (item.getInterface(0).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
//                    data.add(item);
                    Log.d("chensy", "Hubscanning: 打印机存在");
                }
            }
        }

        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        StringBuilder sb = new StringBuilder();
        while (deviceIterator.hasNext()) {
            UsbDevice usbDevice = deviceIterator.next();
            sb.append("DeviceName=" + usbDevice.getDeviceName() + "\n");
            sb.append("DeviceId=" + usbDevice.getDeviceId() + "\n");
            sb.append("VendorId=" + usbDevice.getVendorId() + "\n");
            sb.append("ProductId=" + usbDevice.getProductId() + "\n");
            sb.append("DeviceClass=" + usbDevice.getDeviceClass() + "\n");
            int deviceClass = usbDevice.getDeviceClass();
            if (deviceClass == 0) {
                UsbInterface anInterface = usbDevice.getInterface(0);
                int interfaceClass = anInterface.getInterfaceClass();

                sb.append("device Class 为0-------------\n");
                sb.append("Interface.describeContents()=" + anInterface.describeContents() + "\n");
                sb.append("Interface.getEndpointCount()=" + anInterface.getEndpointCount() + "\n");
                sb.append("Interface.getId()=" + anInterface.getId() + "\n");
                //http://blog.csdn.net/u013686019/article/details/50409421
                //http://www.usb.org/developers/defined_class/#BaseClassFFh
                //通过下面的InterfaceClass 来判断到底是哪一种的，例如7就是打印机，8就是usb的U盘
                sb.append("Interface.getInterfaceClass()=" + anInterface.getInterfaceClass() + "\n");
                if (anInterface.getInterfaceClass() == 7) {
                    sb.append("此设备是打印机\n");
                } else if (anInterface.getInterfaceClass() == 8) {
                    sb.append("此设备是U盘\n");
                }
                sb.append("anInterface.getInterfaceProtocol()=" + anInterface.getInterfaceProtocol() + "\n");
                sb.append("anInterface.getInterfaceSubclass()=" + anInterface.getInterfaceSubclass() + "\n");
                sb.append("device Class 为0------end-------\n");
            }

            sb.append("DeviceProtocol=" + usbDevice.getDeviceProtocol() + "\n");
            sb.append("DeviceSubclass=" + usbDevice.getDeviceSubclass() + "\n");
            sb.append("+++++++++++++++++++++++++++\n");
            sb.append("                           \n");
        }
        Log.d("ceehensy", "Hubscanning: "+sb);
    }
}
