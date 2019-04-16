package ms.sapientia.ro.gaitrecognitionapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.util.Map;

import javax.security.auth.login.LoginException;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.pushnotification.FirebaseMessagingService;
import ms.sapientia.ro.gaitrecognitionapp.service.ActivityBase;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.service.FirebaseUtils;
import ms.sapientia.ro.gaitrecognitionapp.service.Utils;
import weka.classifiers.evaluation.output.prediction.Null;

public class MainActivity extends ActivityBase {

    private static final String TAG = "MainActivity";

    // UI Components
    private EditText mEditText;
    private Button mStartServiceButton;
    private Button mStopServiceButton;
    private Switch mModelSwitch;

    // Vars
    private String tipicStr = "mytopic";
    private String mMyId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        FirebaseUtils.Init( MainActivity.this );

        bindViews();
        bindClickListeners();

        //
        // Push Notification
        //

        FirebaseMessagingService fms = new FirebaseMessagingService(MainActivity.this);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        try {

                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            // Log and toast
                            String msg = "my_custom_res_id";
                            Log.d(TAG, msg);
                            Toast.makeText(MainActivity.this, "Get Instance Id:" + msg, Toast.LENGTH_SHORT).show();
                            mMyId = msg;

                        }catch(NullPointerException e){
                            Log.e(TAG, "onComplete: NullPointerException");
                            e.printStackTrace();
                        }

                    }
                });

        Button registerTopic = findViewById(R.id.register_topic_button);
        registerTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                FirebaseMessaging.getInstance().subscribeToTopic( tipicStr )
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                String msg = "my_msg_subscribed";
                                if (!task.isSuccessful()) {
                                    msg = "my_msg_subscribe_failed";
                                }
                                Log.d(TAG, msg);
                                Toast.makeText(MainActivity.this, "Register: " + msg, Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        Button sendTopic = findViewById(R.id.send_topic_button);
        sendTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // See documentation on defining a message payload.
//                Message message = Message.builder()
//                        .putData("score", "850")
//                        .putData("time", "2:45")
//                        .setTopic(topic)
//                        .build();

                //RemoteMessage rm = new RemoteMessage();

                String sender_id = "sender_id";
                String message_id = "message_id";

                RemoteMessage msg = new RemoteMessage.Builder(sender_id + "@gcm.googleapis.com")    
                                .setMessageId(message_id)
                                .addData(tipicStr, "Hello World")
                                .addData(tipicStr + "_1","SAY_HELLO")
                                .build();

                // Send a message to the devices subscribed to the provided topic.
                //String response = FirebaseMessaging.getInstance().send(msg);

                FirebaseMessaging.getInstance().send(msg);

                // Response is a message ID string.
                //System.out.println("Successfully sent message: " + response);

                Toast.makeText(MainActivity.this, "Send: " + tipicStr + " : Hello World"+ msg, Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "Send: " + tipicStr + "_1" + " : SAY_HELLO"+ msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void bindViews() {
        mEditText = findViewById(R.id.edit_text_input);
        mStartServiceButton = findViewById(R.id.start_service_button);
        mStopServiceButton = findViewById(R.id.stop_service_button);
        mModelSwitch = findViewById(R.id.model_switch);
    }

    @Override
    protected void bindClickListeners() {
        mStartServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 212);

                    Log.e(TAG, "No Internet Permission!");
                    Toast.makeText(MainActivity.this,"No Internet Permission!", Toast.LENGTH_LONG).show();

                }else{
                    mModelSwitch.setEnabled(false);
                    startService(v);
                }
            }
        });

        mStopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopService(v);
                mModelSwitch.setEnabled(true);
            }
        });
    }

    // Service methods:

    public void startService(View v){
        //String input = mEditText.getText().toString();

        Intent serviceIntent = new Intent(this, BackgroundService.class);
        //serviceIntent.putExtra(Utils.INPUT_EXTRA_KEY, input);
        serviceIntent.putExtra(Utils.INPUT_CREATE_OR_VERIFY, ! mModelSwitch.isChecked() );

        startService(serviceIntent);

        /*with Thread*/
//        final Intent serviceIntent = new Intent(getApplicationContext(), BackgroundService.class);
//
//        final ServiceConnection serviceConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                int x;
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//                int y;
//            }
//        };
//
//        Thread thread = new Thread(){
//            public void run(){
//                getApplicationContext().bindService(
//                        serviceIntent,
//                        serviceConnection,
//                        Context.BIND_AUTO_CREATE
//                );
//            }
//        };
//        thread.run(); // thread.START() !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        /*with Thread (end)*/

    }

    public void stopService(View v){
        Intent serviceIntent = new Intent(this, BackgroundService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Internal Saving Location for ALL hidden files:
        Utils.internalFilesRoot = new File(getFilesDir().toString());
        Log.i(TAG, "Utils.internalFilesRoot.getAbsolutePath() = " + Utils.internalFilesRoot.getAbsolutePath());



        Utils.deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
