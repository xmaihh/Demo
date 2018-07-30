package com.titilife.demo.printservice.usbprinter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.titilife.demo.printservice.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsbPrinterActivity extends AppCompatActivity implements View.OnClickListener, UsbPrinterDetector.UsbDeviceAttachChangedListener {

    private LinearLayout llSelectUsbDevice;
    private TextView tvUsbPrinterInfo;
    private Button btnPrint;

    private UsbDevice mSelectUsbDevice;

    private PrintUsbHandler printUsbHandler;
    private UsbPrinterDetector usbPrinterDetector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_printer);
        llSelectUsbDevice = findViewById(R.id.ll_select_usb_device);
        llSelectUsbDevice.setOnClickListener(this);
        tvUsbPrinterInfo = findViewById(R.id.tv_usb_printer_info);
        btnPrint = findViewById(R.id.btn_print);
        btnPrint.setOnClickListener(this);
        printUsbHandler = new PrintUsbHandler((UsbManager) getSystemService(Context.USB_SERVICE));
        usbPrinterDetector = new UsbPrinterDetector(this);
        usbPrinterDetector.setUsbDeviceAttachChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_select_usb_device:
                showSelectUsbPrinterDeviceListDialog();
                break;
            case R.id.btn_print:
                printTest();
                break;
        }
    }

    private void showSelectUsbPrinterDeviceListDialog() {
        final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        List<UsbDevice> data = new ArrayList<>();
        for (UsbDevice item : deviceList.values()) {
            if (item.getInterfaceCount() > 0) {
                if (item.getInterface(0).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                    data.add(item);
                }
            }
        }
        if (data.size() == 0) {
            Toast.makeText(this, "当前无可选的打印机设备", Toast.LENGTH_SHORT).show();
        } else {
            View view = LayoutInflater.from(this).inflate(R.layout.dlg_recyclerview, null);
            RecyclerView rvList = view.findViewById(R.id.rv_list);
            BaseQuickAdapter<UsbDevice, BaseViewHolder> adapter = new BaseQuickAdapter<UsbDevice, BaseViewHolder>(R.layout.item_text) {
                @Override
                protected void convert(BaseViewHolder helper, UsbDevice item) {
                    helper.setText(R.id.tv_text, "DeviceId:" + item.getDeviceId() + ", ProductId:" + item.getProductId() + ", VendorId:" + item.getVendorId());
                }
            };
            rvList.setAdapter(adapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            rvList.setLayoutManager(layoutManager);
            adapter.bindToRecyclerView(rvList);
            adapter.setNewData(data);
            adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    UsbDevice item = (UsbDevice) adapter.getItem(position);
                    if (null != item) {
                        setSelectedUsbDevice(item);
                    }
                }
            });
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setView(view);
            final AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
            adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    UsbDevice item = (UsbDevice) adapter.getItem(position);
                    if (null != item) {
                        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(UsbPrinterActivity.this, 0, new Intent(Constants.ACTION_USB_PRINTER_PERMISSION), 0);
                        mUsbManager.requestPermission(item, mPermissionIntent);
                    }
                    alertDialog.dismiss();
                }
            });
        }
    }

    private void setSelectedUsbDevice(UsbDevice item) {
        this.mSelectUsbDevice = item;
        if (null != item) {
            tvUsbPrinterInfo.setText("DeviceId:" + item.getDeviceId() + ", ProductId:" + item.getProductId() + ", VendorId:" + item.getVendorId());
        } else {
            tvUsbPrinterInfo.setText(null);
        }
    }

    private void printTest() {
        if (null == mSelectUsbDevice) {
            Toast.makeText(this, "请选择打印机设备", Toast.LENGTH_SHORT).show();
        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    printUsbHandler.attachToUsbDevice(mSelectUsbDevice);
                    boolean connect = printUsbHandler.connect();
                    if (!connect) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(UsbPrinterActivity.this, "建立连接失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    } else {
                        try {
                            byte[] initBuffer = {0x1B, 0x40};
                            printUsbHandler.write(initBuffer, 2000);

                            String test = "test";
                            byte[] buffer;
                            byte[] str = test.getBytes("UTF8");
                            printUsbHandler.write(str);

                            int lineCount = 10; // 换行命令执行次数
                            if (lineCount > 0) {
                                buffer = new byte[lineCount];
                                for (int i = 0; i < lineCount; i++) {
                                    buffer[i] = 0x0A;
                                }
                                if (null != buffer) {
                                    printUsbHandler.write(buffer, 2000);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            printUsbHandler.dispose();
                        }
                    }
                }
            });
            thread.start();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usbPrinterDetector.destroy();
    }

    @Override
    public void onUsbDeviceAttached(UsbDevice usbDevice) {
        setSelectedUsbDevice(usbDevice);
    }

    @Override
    public void onUsbDeviceDetached(UsbDevice usbDevice) {
        if (null != mSelectUsbDevice) {
            if (mSelectUsbDevice.getDeviceId() == usbDevice.getDeviceId() && mSelectUsbDevice.getProductId() == usbDevice.getProductId() && mSelectUsbDevice.getVendorId() == usbDevice.getVendorId()) {
                setSelectedUsbDevice(null);
            }
        }
    }
}
