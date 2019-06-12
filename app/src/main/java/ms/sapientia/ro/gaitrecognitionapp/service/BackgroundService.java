package ms.sapientia.ro.gaitrecognitionapp.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

public class BackgroundService extends Service {

    // Static members
    private static final String TAG = "BackgroundService";
    public static final String NAME = "ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService";

    public static boolean isRunning = false;
    public static BackgroundService Instance = null;
    public static BackgroundService storedService = null;
    public static Notification mNotification = null;




    // Vars
    private Recorder mRecorder;
    private boolean mCreateModel; // true= Create Model; false= Verify last created model
    private IBinder mBinder = new LocalBinder();
    private int debugCounter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        Instance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { debugCounter++;
        //String input = intent.getStringExtra(RecorderUtils.INPUT_EXTRA_KEY);
        mCreateModel = intent.getBooleanExtra(RecorderUtils.INPUT_CREATE_OR_VERIFY, false);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  0);

        mNotification = new NotificationCompat.Builder(this, RecorderUtils.CHANNEL_ID_01)
                .setContentTitle("Running")
                .setContentText("Tap to open application")
                .setSmallIcon(R.drawable.ic_assignment)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, mNotification); // id >= 1


        // Do heavy work
        //mRecorder = new Recorder(this, mCreateModel);
        //mRecorder.startRecording();
        StartRecording(Recorder.Mode.MODE_TRAIN);


        //stopSelf();

        //return START_STICKY;
        return START_NOT_STICKY;

    }



    public void StartRecording(Recorder.Mode mode){
        if(mRecorder == null) {
            mRecorder = new Recorder(this, mode);
        }
        mRecorder.startRecording();
        isRunning = true;
    }

    public void StopRecording(){
        if(mRecorder != null) {
            mRecorder.stopRecording();
        }
        isRunning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StopRecording();
        Instance = null;
        //mRecorder.resetRecording();

}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class LocalBinder extends Binder {

        public BackgroundService getService() {
            return BackgroundService.this;
        }

    }

    public void StopService(){
        Log.i(TAG, "StopService()");
        StopRecording();
        stopSelf();
        //onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public BackgroundService getStoredService(){
        return storedService;
    }

    public void setStoredService(BackgroundService service){
        storedService = service;
    }
}
