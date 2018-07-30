package com.ex.administrator.hometest;

import android.app.Activity;
import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
//    IntentFilter mHomeFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resetPreferredLauncherAndOpenChooser(this);
    }


    public static void resetPreferredLauncherAndOpenChooser(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Log.d("chensy", "resetPreferredLauncherAndOpenChooser: " + getLauncherPackageName(context));
//        ComponentName componentName = new ComponentName(context, MainActivity.class);
//        IntentFilter mHomeFilter = new IntentFilter(Intent.ACTION_MAIN);
//        mHomeFilter.addCategory(Intent.CATEGORY_HOME);
//        mHomeFilter.addCategory(Intent.CATEGORY_DEFAULT);
//        ArrayList<ResolveInfo> homeActivities = new ArrayList<ResolveInfo>();
//        ComponentName[] mHomeComponentSet = new ComponentName[homeActivities.size()];
//
//        packageManager.replacePreferredActivity(mHomeFilter, IntentFilter.MATCH_CATEGORY_EMPTY,mHomeComponentSet,
//                componentName);
//        ComponentName componentName = new ComponentName(context, MainActivity.class);
//        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//
//        Intent selector = new Intent(Intent.ACTION_MAIN);
//        selector.addCategory(Intent.CATEGORY_HOME);
//        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(selector);
//
//        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }

    /**
     * 获取正在运行桌面包名（注：存在多个桌面时且未指定默认桌面时，该方法返回Null,使用时需处理这个情况）
     */
    public static String getLauncherPackageName(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            // should not happen. A home is always installed, isn't it?
            return null;
        }
        if (res.activityInfo.packageName.equals("android")) {
            // 有多个桌面程序存在，且未指定默认项时；
//            return null;
            return "123131";
        } else {
            return res.activityInfo.packageName;
        }
    }


    /**
     * 判断自己是否为默认桌面
     */
    public final boolean isDefaultHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);//Intent.ACTION_VIEW
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        PackageManager pm = getPackageManager();
        ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

        boolean isDefault = getPackageName().equals(info.activityInfo.packageName);
        return isDefault;
    }

    private boolean hasApkInstalled(Context ctx, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        android.content.pm.ApplicationInfo info = null;
        try {
            info = ctx.getPackageManager().getApplicationInfo(packageName, 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

//    private void setDefaultLauncher(Context c) {
//        // get default component
//        String packageName = "你指定的包名";//默认launcher包名
//        String className = "你指定的类名";////默认launcher入口
//        IPackageManager pm = ActivityThread.getPackageManager();
//        //判断指定的launcher是否存在
//        if (hasApkInstalled(c,packageName)) {
//            //清除当前默认launcher
//            ArrayList<IntentFilter> intentList = new ArrayList<IntentFilter>();
//            ArrayList<ComponentName> cnList = new ArrayList<ComponentName>();
//            c.getPackageManager().getPreferredActivities(intentList, cnList, null);
//            IntentFilter dhIF = null;
//            for (int i = 0; i < cnList.size(); i++) {
//                dhIF = intentList.get(i);
//                if (dhIF.hasAction(Intent.ACTION_MAIN) && dhIF.hasCategory(Intent.CATEGORY_HOME)) {
//                    c.getPackageManager().clearPackagePreferredActivities(cnList.get(i).getPackageName());
//                }
//            }
//            //获取所有launcher activity
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            List<ResolveInfo> list = new ArrayList<ResolveInfo>();
//            try {
//                list = pm.queryIntentActivities(intent,
//                        intent.resolveTypeIfNeeded(c.getContentResolver()),
//                        PackageManager.MATCH_DEFAULT_ONLY, getCurrentUserIdLocked());
//            } catch (RemoteException e) {
//                throw new RuntimeException("Package manager has died", e);
//            }
//            // get all components and the best match
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(Intent.ACTION_MAIN);
//            filter.addCategory(Intent.CATEGORY_HOME);
//            filter.addCategory(Intent.CATEGORY_DEFAULT);
//            final int N = list.size();
//            //设置默认launcher
//            ComponentName launcher = new ComponentName(packageName, className);
//            ComponentName[] set = new ComponentName[N];
//            int defaultMatch = 0;
//            for (int i = 0; i < N; i++) {
//                ResolveInfo r = list.get(i);
//                set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
//                if (launcher.getClassName().equals(r.activityInfo.name)) {
//                    defaultMatch = r.match;
//                }
//            }
//            try {
//                pm.addPreferredActivity(filter, defaultMatch, set, launcher, getCurrentUserIdLocked());
//            } catch (RemoteException e) {
//                throw new RuntimeException("Package manager has died", e);
//            }
//        }
//    }


}
