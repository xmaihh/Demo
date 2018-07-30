package com.egprint.administrator.printservicedemo.project;

import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2018/3/12.
 * 显示打印日志
 */

public class LogBean {
    public String mTime;
    public String mLog;

    public LogBean(long time, String log) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        mTime = format.format(time);
        mLog = log;
    }
}
