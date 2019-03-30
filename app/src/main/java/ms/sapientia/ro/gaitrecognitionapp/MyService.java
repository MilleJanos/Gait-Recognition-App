package ms.sapientia.ro.gaitrecognitionapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class MyService extends Service {

    private static final String TAG = "MyService";

    private IBinder mBinder = new MyBinder();
    private Handler mHandler;
    private int mProgress, mMaxValue;
    private Boolean mIsPaused;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mProgress = 0;
        mIsPaused = true;
        mMaxValue = 5000;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // Provide access point to return service instance
    public class MyBinder extends Binder{
        MyService getService(){
            return MyService.this;
        }
    }

    public void startPretendLongRunningTask(){
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(mProgress >= mMaxValue || mIsPaused){
                    Log.d(TAG, "run: removing callbacks.");
                    mHandler.removeCallbacks(this);
                    pausePretendLongRunningTask();
                }else{
                    Log.d(TAG, "run: progress: "+mProgress);
                    mProgress += 10;
                    mHandler.postDelayed(this, 100);
                }
            }
        };
        mHandler.postDelayed(runnable, 100);
    }

    public void pausePretendLongRunningTask() {
        mIsPaused = true;
    }

    public void unPausePretendLongRunningTask(){
        mIsPaused = false;
        startPretendLongRunningTask();
    }

    public int getProgress() {
        return mProgress;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public Boolean getIsPaused() {
        return mIsPaused;
    }

    public void resetTask(){
        mProgress = 0;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // Stops when the application has been closed:
        //stopSelf();
    }
}
