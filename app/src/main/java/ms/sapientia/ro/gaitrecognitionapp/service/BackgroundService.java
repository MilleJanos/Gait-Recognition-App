package ms.sapientia.ro.gaitrecognitionapp.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.MainActivity;

public class BackgroundService extends Service {

    // Vars
    private Recorder mRecorder;
    private boolean mCreateModel; // true= Create Model; false= Verify last created model

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //String input = intent.getStringExtra(Utils.INPUT_EXTRA_KEY);
        mCreateModel = intent.getBooleanExtra(Utils.INPUT_CREATE_OR_VERIFY, false);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  0);

        Notification notification = new NotificationCompat.Builder(this, Utils.CHANNEL_ID_01)
                .setContentTitle("Running")
                .setContentText("Tap to open application")
                .setSmallIcon(R.drawable.ic_assignment)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification); // id >= 1

        // Do heavy work
        mRecorder = new Recorder(this, mCreateModel);
        mRecorder.startRecording();

        //stopSelf();

        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        mRecorder.stopRecording();
        //mRecorder.resetRecording();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
