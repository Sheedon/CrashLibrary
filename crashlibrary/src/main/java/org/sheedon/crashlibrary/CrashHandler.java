package org.sheedon.crashlibrary;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;


/**
 * 当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 * 需要在Application中注册，为了要在程序启动器就监控整个程序。
 */
class CrashHandler implements Thread.UncaughtExceptionHandler {
    //TAG
    public static final String TAG = "CrashHandler";
    //自定义Toast
    private static Toast mCustomToast;
    //提示文字
    private static String mCrashTip = "系统正在重启中.";
    //CrashHandler实例
    private static volatile CrashHandler instance;
    //程序的App对象
    public Application mApplication;
    //生命周期监听
    MyActivityLifecycleCallbacks mMyActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
    //是否是Debug模式
    private boolean mIsDebug;
    //是否重启APP
    private boolean mIsRestartApp;
    //重启APP时间
    private long mRestartTime;
    //重启后的第一个Activity class文件
    private Class mClassOfFirstActivity;
    //是否已经toast
    private boolean hasToast;

    /**
     * 私有构造函数
     */
    private CrashHandler() {

    }

    /**
     * 获取CrashHandler实例 ,单例模式
     *
     * @return
     * @since V1.0
     */
    public static CrashHandler getInstance() {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler();
                }
            }
        }
        return instance;
    }

    public static void setCloseAnimation(int closeAnimation) {
        MyActivityLifecycleCallbacks.sAnimationId = closeAnimation;
    }

    public static void setCustomToast(Toast customToast) {
        mCustomToast = customToast;
    }

    public static void setCrashTip(String crashTip) {
        mCrashTip = crashTip;
    }

    public void init(Application application, boolean isDebug, boolean isRestartApp, long restartTime, Class classOfFirstActivity) {
        mIsRestartApp = isRestartApp;
        mRestartTime = restartTime;
        mClassOfFirstActivity = classOfFirstActivity;
        initCrashHandler(application, isDebug);

    }

    public void init(Application application, boolean isDebug) {
        initCrashHandler(application, isDebug);
    }

    /**
     * 初始化
     *
     * @since V1.0
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void initCrashHandler(Application application, boolean isDebug) {
        mIsDebug = isDebug;
        mApplication = application;
        mApplication.registerActivityLifecycleCallbacks(mMyActivityLifecycleCallbacks);
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);

    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        handleException(ex);

        try {
            //给Toast留出时间
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.e(TAG, "uncaughtException() InterruptedException:" + e);
        }

        if (mIsRestartApp) {
            //利用系统时钟进行重启任务
            AlarmManager mgr = (AlarmManager) mApplication.getSystemService(Context.ALARM_SERVICE);
            try {
                Intent intent = new Intent(mApplication, mClassOfFirstActivity);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent restartIntent = PendingIntent.getActivity(mApplication, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + mRestartTime, restartIntent); // x秒钟后重启应用
            } catch (Exception e) {
                Log.e(TAG, "first class error:" + e);
            }
        }

        mMyActivityLifecycleCallbacks.removeAllActivities();
        mApplication.onTerminate();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
        System.gc();


    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private void handleException(Throwable ex) {
        if (!hasToast) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Looper.prepare();
                        Toast toast;
                        if (mCustomToast == null) {
                            toast = Toast.makeText(mApplication, mCrashTip, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                        } else {
                            toast = mCustomToast;
                        }
                        toast.show();
                        Looper.loop();
                        hasToast = true;
                    } catch (Exception e) {
//                        Log.e(TAG, "handleException Toast error" + e);
                    }
                }
            }).start();
        }

        if (ex == null) {
            return;
        }

        if (mIsDebug) {
            CrashReport.postCatchedException(ex);
        }
    }

}
