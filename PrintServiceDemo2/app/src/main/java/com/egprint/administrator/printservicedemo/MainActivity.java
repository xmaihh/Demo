package com.egprint.administrator.printservicedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.egprint.administrator.printservicedemo.gprinter.UsbDeviceList;
import com.egprint.administrator.printservicedemo.project.LogAdapter;
import com.egprint.administrator.printservicedemo.project.LogBean;
import com.egprint.administrator.printservicedemo.project.util.PreferencesUtils;
import com.egprint.administrator.printservicedemo.project.util.SafeHandler;
import com.egprint.administrator.printservicedemo.usbprint.Constants;

import com.egprint.administrator.printservicedemo.usbprint.PrintUsbHandler;
import com.egprint.administrator.printservicedemo.usbprint.UsbPrinterDetector;
import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.command.GpCom;
import com.gprinter.command.TscCommand;
import com.gprinter.io.GpDevice;
import com.gprinter.io.PortParameters;
import com.gprinter.service.GpPrintService;

import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static com.egprint.administrator.printservicedemo.usbprint.Constants.EXTRA_DEVICE_ADDRESS;

public class MainActivity extends Activity implements SafeHandler.HandlerContainer, View.OnClickListener, UsbPrinterDetector.UsbDeviceAttachChangedListener {

    private LogAdapter mLabelLogAdapter = new LogAdapter();
    private LogAdapter mTicketLogAdapter = new LogAdapter();
    private RecyclerView mLabelLogList;
    private RecyclerView mTicketLogList;
    private TextView mTxtLabelName;
    private TextView mTxtTicketName;
    private Button mBtnConnLabel;
    private Button mBtnConnTicket;
    private Button mBtnLabelStatus;
    private Button mBtnTicketStatus;
    private Button mBtnLabelPrint;
    private Button mBtnTicketPrint;
    private SafeHandler<MainActivity> mHandler;
    public static final int REQUEST_USB_DEVICE = 4;
    private PortParameters mPortParam;
    private Vibrator mVibrator;
    //
    private UsbDevice mSelectUsbDevice;
    private PrintUsbHandler printUsbHandler;
    private UsbPrinterDetector usbPrinterDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLabelLogList = findViewById(R.id.label_list);
        mTicketLogList = findViewById(R.id.ticket_list);
        mTxtLabelName = findViewById(R.id.label_name);
        mTxtTicketName = findViewById(R.id.ticket_name);
        mBtnConnLabel = findViewById(R.id.connect_label);
        mBtnConnTicket = findViewById(R.id.connect_ticket);
        mBtnLabelStatus = findViewById(R.id.label_status);
        mBtnTicketStatus = findViewById(R.id.ticket_status);
        mBtnLabelPrint = findViewById(R.id.label_print);
        mBtnTicketPrint = findViewById(R.id.ticket_print);

        mBtnConnTicket.setOnClickListener(this);
        mBtnConnLabel.setOnClickListener(this);
        mBtnLabelStatus.setOnClickListener(this);
        mBtnTicketStatus.setOnClickListener(this);
        mBtnLabelPrint.setOnClickListener(this);
        mBtnTicketPrint.setOnClickListener(this);

        mHandler = new SafeHandler<MainActivity>(this);
        mPortParam = new PortParameters();
        mPortParam.setPortType(PortParameters.USB);
        initData();
        startService();
        connection();
        registerBroadcast();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //
        printUsbHandler = new PrintUsbHandler((UsbManager) getSystemService(Context.USB_SERVICE));
        usbPrinterDetector = new UsbPrinterDetector(this);
        usbPrinterDetector.setUsbDeviceAttachChangedListener(this);
    }


    private void initData() {
        LinearLayoutManager manager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLabelLogList.setLayoutManager(manager1);
        mLabelLogList.setAdapter(mLabelLogAdapter);

        LinearLayoutManager manager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mTicketLogList.setLayoutManager(manager2);
        mTicketLogList.setAdapter(mTicketLogAdapter);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.MSG.MSG_INIT_SERVICE:
                break;
        }
    }

    private void labelLog(String log) {
        LogBean logBean = new LogBean(System.currentTimeMillis(), log);
        mLabelLogAdapter.getDataList().add(0, logBean);
        mLabelLogAdapter.notifyDataSetChanged();
    }

    private void ticketLog(String log) {
        LogBean logBean = new LogBean(System.currentTimeMillis(), log);
        mTicketLogAdapter.getDataList().add(0, logBean);
        mTicketLogAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.connect_label:
                Intent intent = new Intent(MainActivity.this,
                        UsbDeviceList.class);
                startActivityForResult(intent, REQUEST_USB_DEVICE);
                break;
            case R.id.connect_ticket:
                showSelectUsbPrinterDeviceListDialog();
                break;
            case R.id.label_status:
                if (mGpService != null) {
                    getPrinterStatusClicked();
                } else {
                    Toast.makeText(getApplicationContext(), "请先连接再获取状态", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ticket_status:
                UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                List<UsbDevice> data = new ArrayList<>();

                for (UsbDevice item : deviceList.values()) {
                    if (item.getInterfaceCount() > 0) {
                        if (item.getInterface(0).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                            if (item.getVendorId() == 10473) {
                                ticketLog("票据打印机状态：打印机 正常");
                                Toast.makeText(getApplicationContext(), "票据打印机状态：打印机 正常", Toast.LENGTH_SHORT).show();
                            } else {
                                ticketLog("票据打印机状态：打印机 脱机");
                                Toast.makeText(getApplicationContext(), "票据打印机状态：打印机 脱机", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                break;
            case R.id.label_print:
                mVibrator.vibrate(5000);
//                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
//                printLabelClicked();
//                printTestPageClicked();
                break;
            case R.id.ticket_print:
                printTest();
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;

        }
    }


    /**
     * 启动服务
     */
    private void startService() {
        Intent i = new Intent(this, GpPrintService.class);
        startService(i);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 绑定服务
     */
    private GpService mGpService = null;
    private PrinterServiceConnection conn = null;

    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("chensy", "onServiceDisconnected() called");
            mGpService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGpService = GpService.Stub.asInterface(service);
        }
    }

    private void connection() {
        conn = new PrinterServiceConnection();
        /**
         * Android5.0中service的intent一定要显性声明
         * ERROR!! IllegalArgumentException: Service Intent must be explicit
         * https://blog.csdn.net/shenzhonglaoxu/article/details/42675287
         */
        Intent intent = new Intent("com.gprinter.aidl.GpPrintService");
        intent.setPackage(this.getPackageName());
        bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService
    }

    /**
     * 注册状态接收广播，通过此广播可以获取当前端口的连接状态
     *
     * @parm GpPrintService.PRINTER_ID 返回打印机的 ID 序号
     * @parm GpPrintService.CONNECT_STATUS 返回状态
     * @parm GpPrintService 可以同时连接三台打印机，可以通过此广播获取到哪台打印机处于何种状态
     */
    public static final String ACTION_CONNECT_STATUS = "action.connect.status";

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CONNECT_STATUS);
        this.registerReceiver(PrinterStatusBroadcastReceiver, filter);
    }

    private BroadcastReceiver PrinterStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CONNECT_STATUS.equals(intent.getAction())) {
                int type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0);
                int id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0);
                if (type == GpDevice.STATE_CONNECTING) {
                } else if (type == GpDevice.STATE_NONE) {

                } else if (type == GpDevice.STATE_VALID_PRINTER) {  //正常
                    labelLog("标签打印机已连接");
                } else if (type == GpDevice.STATE_INVALID_PRINTER) { //无效
                    labelLog("标签打印机未连接");
                    mTxtLabelName.setText("");
                }
            }
        }
    };

    /**
     * 连接
     */
    void connectOrDisConnectToDevice() {

        int rel = 0;

        if (mPortParam.getPortOpenState() == false) {
            if (CheckPortParamters(mPortParam)) {
                try {
                    mGpService.closePort(0);
                    mTxtLabelName.setText("uid/pid");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                switch (mPortParam.getPortType()) {
                    case PortParameters.USB:
                        try {

                            rel = mGpService.openPort(0, mPortParam.getPortType(), mPortParam.getUsbDeviceName(), 0);
                            String device_name = PreferencesUtils.getString(MainActivity.this, "device_name") + "";
                            mTxtLabelName.setText(device_name);
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        break;
                }
                GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];

                if (r != GpCom.ERROR_CODE.SUCCESS) {
                    if (r == GpCom.ERROR_CODE.DEVICE_ALREADY_OPEN) {
                        mPortParam.setPortOpenState(true);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                GpCom.getErrorText(r), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        "端口参数错误！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("chensy", "DisconnectToDevice ");
            try {
                mGpService.closePort(0);
                mTxtLabelName.setText("uid/pid");
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public void printTestPageClicked() {
        try {
            labelLog("正在打印...");
            int rell = mGpService.printeTestPage(0); //
            Log.i("ServiceConnection", "rell " + rell);
            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rell];
            if (r != GpCom.ERROR_CODE.SUCCESS) {
                Toast.makeText(getApplicationContext(), GpCom.getErrorText(r),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public void printLabelClicked() {
        try {
            int type = mGpService.getPrinterCommandType(0);
            if (type == GpCom.TSC_COMMAND) {
                int status = mGpService.queryPrinterStatus(0, 500);
                if (status == GpCom.STATE_NO_ERR) {
                    TscCommand tsc = new TscCommand();
                    tsc.addSize(60, 60); //设置标签尺寸，按照实际尺寸设置
                    tsc.addGap(0);           //设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
                    tsc.addDirection(TscCommand.DIRECTION.BACKWARD, TscCommand.MIRROR.NORMAL);//设置打印方向
                    tsc.addReference(0, 0);//设置原点坐标
                    tsc.addTear(EscCommand.ENABLE.ON); //撕纸模式开启
                    tsc.addCls();// 清除打印缓冲区
                    //绘制简体中文
                    tsc.addText(20, 20, TscCommand.FONTTYPE.SIMPLIFIED_CHINESE, TscCommand.ROTATION.ROTATION_0, TscCommand.FONTMUL.MUL_1, TscCommand.FONTMUL.MUL_1, "Welcome to use Gprinter!");
                    //绘制图片
                    Bitmap b = BitmapFactory.decodeResource(getResources(),
                            R.drawable.gprinter);
                    tsc.addBitmap(20, 50, TscCommand.BITMAP_MODE.OVERWRITE, b.getWidth() * 2, b);

                    tsc.addQRCode(250, 80, TscCommand.EEC.LEVEL_L, 5, TscCommand.ROTATION.ROTATION_0, " www.gprinter.com.cn");
                    //绘制一维条码
                    tsc.add1DBarcode(20, 250, TscCommand.BARCODETYPE.CODE128, 100, TscCommand.READABEL.EANBEL, TscCommand.ROTATION.ROTATION_0, "Gprinter");
                    tsc.addPrint(1, 1); // 打印标签
                    tsc.addSound(2, 100); //打印标签后 蜂鸣器响
                    Vector<Byte> datas = tsc.getCommand(); //发送数据
                    Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
                    byte[] bytes = ArrayUtils.toPrimitive(Bytes);
                    String str = Base64.encodeToString(bytes, Base64.DEFAULT);
                    int rel;
                    try {
                        rel = mGpService.sendTscCommand(0, str);
                        GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
                        if (r != GpCom.ERROR_CODE.SUCCESS) {
                            Toast.makeText(getApplicationContext(), GpCom.getErrorText(r),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "标签打印机错误！", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (RemoteException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public void getPrinterStatusClicked() {
        try {
            int status = mGpService.queryPrinterStatus(0, 500);
            String str = new String();
            if (status == GpCom.STATE_NO_ERR) {
                str = "标签打印机正常";
            } else {
                str = "打印机 ";
                if ((byte) (status & GpCom.STATE_OFFLINE) > 0) {
                    str += "脱机";
                }
                if ((byte) (status & GpCom.STATE_PAPER_ERR) > 0) {
                    str += "缺纸";
                }
                if ((byte) (status & GpCom.STATE_COVER_OPEN) > 0) {
                    str += "标签打印机开盖";
                }
                if ((byte) (status & GpCom.STATE_ERR_OCCURS) > 0) {
                    str += "标签打印机出错";
                }
                if ((byte) (status & GpCom.STATE_TIMES_OUT) > 0) {
                    str += "查询超时";
                }
            }
            labelLog("标签打印机状态：" + str);
            Toast.makeText(getApplicationContext(),
                    "标签打印机状态：" + str, Toast.LENGTH_SHORT).show();
        } catch (RemoteException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    Boolean CheckPortParamters(PortParameters param) {
        boolean rel = false;
        int type = param.getPortType();
        if (type == PortParameters.BLUETOOTH) {
            if (!param.getBluetoothAddr().equals("")) {
                rel = true;
            }
        } else if (type == PortParameters.ETHERNET) {
            if ((!param.getIpAddr().equals("")) && (param.getPortNumber() != 0)) {
                rel = true;
            }
        } else if (type == PortParameters.USB) {
            if (!param.getUsbDeviceName().equals("")) {
                rel = true;
            }
        }
        return rel;
    }

    /**
     *
     *
     *
     *
     */


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USB_DEVICE) {
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(
                        EXTRA_DEVICE_ADDRESS);
                // fill in some parameters
                Log.d("chensy", "onActivityResult: 选中的设备" + address);
                mPortParam.setUsbDeviceName(address);

                connectOrDisConnectToDevice();
            }
        }
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
                        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(Constants.ACTION_USB_PRINTER_PERMISSION), 0);
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
            mTxtTicketName.setText("DeviceId:" + item.getDeviceId() + ", ProductId:" + item.getProductId() + ", VendorId:" + item.getVendorId());
            ticketLog("票据打印机已连接" +
                    "");
        } else {
            mTxtTicketName.setText("uid/pid");
            ticketLog("票据打印机未连接");
        }
    }

    private void printTest() {
        if (null == mSelectUsbDevice) {
            Toast.makeText(this, "请选择打印机设备", Toast.LENGTH_SHORT).show();
        } else {
            ticketLog("正在打印...");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    printUsbHandler.attachToUsbDevice(mSelectUsbDevice);
                    boolean connect = printUsbHandler.connect();
                    if (!connect) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "建立连接失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    } else {
                        try {
                            byte[] initBuffer = {0x1B, 0x40};
                            printUsbHandler.write(initBuffer, 2000);

                            String test = "Print test\nCongratulations !\nPrinter Connected OK\nYou can use printer now\n Thank you";
                            byte[] buffer;
                            byte[] str = test.getBytes("UTF8");
                            printUsbHandler.write(str);

                            int lineCount = 4; // 换行命令执行次数
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
        this.unregisterReceiver(PrinterStatusBroadcastReceiver);
        usbPrinterDetector.destroy();
        if (conn != null) {
            unbindService(conn);
        }
    }

    public static void test1() {
        //创建Timer
        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
//                xxx
            }
        };
        //设定定时任务
        timer.schedule(timerTask, 1000);

        timerTask.cancel();
        timer.cancel();
    }

    /**
     * 倒计时
     *
     * @param min 倒计时间（分钟）
     */
    public void CountDown(int min) {
        String piece = String.format("", (String) "");

        //开始时间
        long start = System.currentTimeMillis();
        //结束时间
        final long end = start + min * 60 * 1000;

        final Timer timer = new Timer();
        //延迟0毫秒（即立即执行）开始，每隔1000毫秒执行一次
        timer.schedule(new TimerTask() {
            public void run() {
                Log.e("MainActivity", "此处实现倒计时，指定时长内，每隔1秒执行一次该任务");
            }
        }, 0, 1000);
        //计时结束时候，停止全部timer计时计划任务
        timer.schedule(new TimerTask() {
            public void run() {
                timer.cancel();
            }

        }, new Date(end));
    }


    private static Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 1:
//                    button1.setText("点击安装");
//                    down = 1;
                    break;

                default:
                    break;
            }
        }

    };

}
