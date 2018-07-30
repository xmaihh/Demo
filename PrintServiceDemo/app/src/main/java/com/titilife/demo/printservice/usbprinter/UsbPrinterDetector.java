package com.titilife.demo.printservice.usbprinter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.util.HashMap;

/**
 * USB连接方式打印机设备检测(广播监听)类
 */
public class UsbPrinterDetector extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        UsbManager mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(Constants.ACTION_USB_PRINTER_PERMISSION), 0);
        synchronized (this) {
            if (Constants.ACTION_USB_PRINTER_DETECT.equals(action)) {
                // 列出所有的USB设备，请求获取一个USB打印机的权限
                HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                if (!deviceList.isEmpty()) {
                    for (UsbDevice device : deviceList.values()) {
                        if (device.getInterface(0) != null && device.getInterface(0).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                            mUsbManager.requestPermission(device, mPermissionIntent);
                            break;
                        }
                    }
                }
            } else if (Constants.ACTION_USB_PRINTER_PERMISSION.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (usbDevice.getInterface(0) != null && usbDevice.getInterface(0).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                        if (null != usbDeviceAttachChangedListener) {
                            usbDeviceAttachChangedListener.onUsbDeviceAttached(usbDevice);
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null && usbDevice.getInterface(0) != null && usbDevice.getInterface(0).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                    if (null != usbDeviceAttachChangedListener) {
                        usbDeviceAttachChangedListener.onUsbDeviceDetached(usbDevice);
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null && usbDevice.getInterface(0) != null && usbDevice.getInterface(0).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                    if (!mUsbManager.hasPermission(usbDevice)) {
                        mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                    }
                }
            }
        }
    }

    private Context mContext;

    public UsbPrinterDetector(Context mContext) {
        this.mContext = mContext;
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(Constants.ACTION_USB_PRINTER_DETECT);
        filter.addAction(Constants.ACTION_USB_PRINTER_PERMISSION);
        this.mContext.registerReceiver(this, filter);
    }

    public void destroy() {
        mContext.unregisterReceiver(this);
        mContext = null;
    }

    private UsbDeviceAttachChangedListener usbDeviceAttachChangedListener;

    public void setUsbDeviceAttachChangedListener(UsbDeviceAttachChangedListener usbDeviceAttachChangedListener) {
        this.usbDeviceAttachChangedListener = usbDeviceAttachChangedListener;
    }

    public interface UsbDeviceAttachChangedListener {

        void onUsbDeviceAttached(UsbDevice usbDevice);

        void onUsbDeviceDetached(UsbDevice usbDevice);
    }

}
