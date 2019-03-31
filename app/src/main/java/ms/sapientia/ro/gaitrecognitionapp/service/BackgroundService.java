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

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //String input = intent.getStringExtra(Common.INPUT_EXTRA_KEY);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  0);

        Notification notification = new NotificationCompat.Builder(this, Common.CHANNEL_ID_01)
                .setContentTitle("Running")
                .setContentText("Tap to open application")
                .setSmallIcon(R.drawable.ic_assignment)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification); // id >= 1

        // Do heavy work

        //stopSelf();

        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
