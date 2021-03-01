package org.sheedon.crashlibrary;

import android.app.Application;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * 提供的功能调度
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 3/1/21 2:26 PM
 */
public class Crash {

    /**
     * 初始化
     * @param application 应用上下文
     * @param isDebug 是否是Debug模式
     * @param isRestartApp 崩溃后是否启动App
     * @param restartTime 重新启动时间
     * @param classOfFirstActivity 启动的Activity
     * @param buglyAppId buglyId
     */
    public static void init(Application application, boolean isDebug, boolean isRestartApp,
                            long restartTime, Class classOfFirstActivity, String buglyAppId) {
        CrashHandler.getInstance().init(application, isDebug, isRestartApp, restartTime, classOfFirstActivity);
        CrashReport.initCrashReport(application, buglyAppId, isDebug);
    }

    /**
     * 关闭动画
     */
    public static void setCloseAnimation(int closeAnimation) {
        CrashHandler.setCloseAnimation(closeAnimation);
    }

    /**
     * 自定义Toast
     */
    public static void setCustomToast(Toast customToast) {
        CrashHandler.setCustomToast(customToast);
    }

    /**
     * 设置崩溃提示
     */
    public static void setCrashTip(String crashTip) {
        CrashHandler.setCrashTip(crashTip);
    }
}
