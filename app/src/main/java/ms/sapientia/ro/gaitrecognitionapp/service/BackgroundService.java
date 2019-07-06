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

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

/**
 * This class holds the service running in background and controlling the Recorder class.
 *
 * @author MilleJanos
 */
public class BackgroundService extends Service {

    // Static members:
    private static final String TAG = "BackgroundService";
    public static final String NAME = "ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService";
    public static boolean sIsRunning = false;
    public static BackgroundService sInstance = null;
    public static BackgroundService sStoredService = null;
    public static Notification sNotification = null;
    // Other members:
    private Recorder mRecorder;
    private boolean mCreateModel; // true= Create Model; false= Verify last created model
    private IBinder mBinder = new LocalBinder();
    private int debugCounter = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        sIsRunning = true;
        sInstance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { debugCounter++;
        //String input = intent.getStringExtra(RecorderUtils.INPUT_EXTRA_KEY);
        mCreateModel = intent.getBooleanExtra(RecorderUtils.INPUT_CREATE_OR_VERIFY, false);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("started_by_notification","true");  // ONLY FOR FUTURE USAGES
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT); // PendingIntent.FLAG_UPDATE_CURRENT instead of 0 // ONLY FOR FUTURE USAGES

        sNotification = new NotificationCompat.Builder(this, RecorderUtils.CHANNEL_ID_01)
                .setContentTitle("Running")
                .setContentText("Tap to open application")
                .setSmallIcon(R.drawable.ic_assignment)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_gait_recognition_app)
                .build();
        startForeground(1, sNotification); // id >= 1

        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
        }

        // Do heavy work:
        StartRecording(AppUtil.sUser.selected_mode, AppUtil.sTrainNewOne);

        return START_NOT_STICKY;
    }

    /**
     * This method starts the recording.
     * @param mode starts in mode
     * @param train_new_one true-train new; false-train more the previous one
     */
    public void StartRecording(Recorder.Mode mode, boolean train_new_one){
        if(mRecorder == null) {
            // Start Recorder with selected mode & train mode
            mRecorder = new Recorder(this, AppUtil.sAuth, mode, train_new_one);
        }
        mRecorder.startRecording();
        sIsRunning = true;
    }

    /**
     * This method stops the recording.
     */
    public void StopRecording(){
        if(mRecorder != null) {
            mRecorder.stopRecording();
        }
        sIsRunning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StopRecording();
        sInstance = null;
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

    /**
     * This method stops the service.
     */
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

    /**
     * This method returns the stored service value.
     * @return stored service
     */
    public BackgroundService getStoredService(){
        return sStoredService;
    }

    /**
     * This method sets the stored service value.
     * @param service stored service
     */
    public void setStoredService(BackgroundService service){
        sStoredService = service;
    }
}
