package com.egprint.administrator.printservicedemo.usbprint;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.io.IOException;

/**
 * 描述：usb连接方式打印机通信管理类
 * 备注：
 * <p>
 * 1、当取得usb打印设备权限时（不管是已连接的usb打印设备，还是后来才连接的usb打印设备），建立连接通信
 * 2、当usb打印设备中断连接时，中断连接通信
 */

public class PrintUsbHandler {

    public PrintUsbHandler(UsbManager usbManager) {
        this.usbManager = usbManager;
    }

    private static final int TIME_OUT_DATA_TRANSFER = 2 * 1000;

    private UsbManager usbManager;
    private UsbDevice usbDevice;
    protected UsbDeviceConnection usbDeviceConnection;
    private UsbEndpoint epOut;
    private UsbEndpoint epIn;

    public UsbDevice getUsbDevice() {
        return this.usbDevice;
    }

    public void attachToUsbDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    public void detachFromUsbDevice() {
        this.usbDevice = null;
    }

    protected int write(byte[] buffer, int off, int length, int timeout) throws IOException {
        if (null == usbDeviceConnection || epOut == null) {
            return -1;
        } else {
            return usbDeviceConnection.bulkTransfer(epOut, buffer, off, length, timeout);
        }
    }

    protected int write(byte[] buffer, int off, int length) throws IOException {
        return write(buffer, off, length, TIME_OUT_DATA_TRANSFER);
    }

    public int write(byte[] buffer, int timeout) throws IOException {
        if (null == usbDeviceConnection || epOut == null) {
            return -1;
        } else {
            return usbDeviceConnection.bulkTransfer(epOut, buffer, buffer.length, timeout);
        }
    }

    public int write(byte[] buffer) throws IOException {
        return write(buffer, TIME_OUT_DATA_TRANSFER);
    }

    protected int read(byte[] buffer, int timeout) throws IOException {
        if (null == usbDeviceConnection || null == epIn) {
            return -1;
        } else {
            return usbDeviceConnection.bulkTransfer(epIn, buffer, buffer.length, timeout);
        }
    }

    protected int read(byte[] buffer) throws IOException {
        return read(buffer, TIME_OUT_DATA_TRANSFER);
    }

    public boolean connect() {
        if (null == usbDevice) {
            clearConnect();
            return false;
        } else {
            if (usbManager.hasPermission(usbDevice)) { // 只处理有权限的情况
                if (null == usbDeviceConnection) {
                    int interfaceCount = usbDevice.getInterfaceCount();
                    if (interfaceCount >= 1) {
                        UsbInterface usbInterface = usbDevice.getInterface(0);
                        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                            final UsbEndpoint ep = usbInterface.getEndpoint(i);
                            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                                    epOut = ep;
                                } else if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                                    epIn = ep;
                                }
                            }
                        }

                        if (null != epOut && null != epIn) {
                            usbDeviceConnection = usbManager.openDevice(usbDevice);
                            if (usbDeviceConnection != null) {
                                usbDeviceConnection.claimInterface(usbInterface, true);
                            }
                        }
                    }
                }

                if (null == usbDeviceConnection) {
                    clearConnect();
                    return false;
                } else {
                    return true;
                }

            } else {
                clearConnect();
                return false;
            }
        }
    }


    public void clearConnect() {
        if (null != usbDevice) {
            if (null != usbDeviceConnection) {
                usbDeviceConnection.releaseInterface(usbDevice.getInterface(0));
                usbDeviceConnection.close();
                usbDeviceConnection = null;
                epOut = null;
                epIn = null;
            }
            usbDevice = null;
        }
    }

    public void dispose() {
        clearConnect();
    }
}
