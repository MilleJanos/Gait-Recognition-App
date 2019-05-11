package ms.sapientia.ro.gaitrecognitionapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import java.io.Console;
import java.io.File;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.service.ActivityBase;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.service.FirebaseUtils;
import ms.sapientia.ro.gaitrecognitionapp.service.Utils;

import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService.LocalBinder;

public class MainActivity extends ActivityBase {

    private static final String TAG = "MainActivity";

    // UI Components
    private EditText mEditText;
    private Button mStartServiceButton;
    private Button mStopServiceButton;
    private Switch mModelSwitch;
    private TextView mDebugTextView;

    // Vars
    private Intent mServiceIntent;
    //private ServiceConnection mServiceConnection; // declared at the bottom
    BackgroundService mService;
    private boolean mServiceIsBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        FirebaseUtils.Init( MainActivity.this );

        bindViews();
        bindClickListeners();

        //if(mServiceIsBound){
        //    // service is working
        //}
    }

    @Override
    protected void bindViews() {
        mEditText = findViewById(R.id.edit_text_input);
        mStartServiceButton = findViewById(R.id.start_service_button);
        mStopServiceButton = findViewById(R.id.stop_service_button);
        mModelSwitch = findViewById(R.id.model_switch);
        mDebugTextView = findViewById(R.id.debug_text_view);
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

    private void bind(){
        try {
            getApplicationContext().bindService(
                    mServiceIntent,
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Can't bind!", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Can't bind!");
            e.printStackTrace();
        }
    }

    public void startService(View v){

        //region OLD CODE

        //String input = mEditText.getText().toString();

        //*//Intent serviceIntent = new Intent(this, BackgroundService.class);
        //*////serviceIntent.putExtra(Utils.INPUT_EXTRA_KEY, input);
        //*//serviceIntent.putExtra(Utils.INPUT_CREATE_OR_VERIFY, ! mModelSwitch.isChecked() );
        //*//
        //*//startService(serviceIntent);

        //endregion

        /*with Thread*/
        mServiceIntent = new Intent(getApplicationContext(), BackgroundService.class);
        mServiceIntent.putExtra(Utils.INPUT_CREATE_OR_VERIFY, ! mModelSwitch.isChecked() );

        Thread thread = new Thread(){
            public void start(){
                // Start Service:
                //startService(mServiceIntent);
                // Bind to it:
                if ( ! mServiceIsBound) {
                    bind();
                }
            }
        };
        thread.start();
        /*with Thread (end)*/

    }


    public void stopService(View v){

        // Unbinding the BIND_AUTO_CREATE one;

        mServiceIntent = new Intent(getApplicationContext(), BackgroundService.class);
        mServiceIntent.putExtra(Utils.INPUT_CREATE_OR_VERIFY, ! mModelSwitch.isChecked() );

        if ( ! mServiceIsBound) {
            bind();
        }

        //TODO: WAIT PREV/ bind() async !!!

        if (mServiceIsBound) {
            try {
                if (mService != null) {
                    mService.StopService();
                } else {
                    mServiceIsBound = false; // TODO szukseges ez ide ?
                    return;
                }
                getApplicationContext().unbindService(mServiceConnection);
                mServiceIsBound = false;
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Can't unbind!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Can't unbind!");
                e.printStackTrace();
            }
        }



        //region OLD CODE

        //mService.StopService();


        //mService.StopService();
//        //*//Intent serviceIntent = new Intent( this, BackgroundService.class);
//        try {
//            unbindService(mServiceConnection);
//            Toast.makeText(this, "Unbinded", Toast.LENGTH_SHORT).show();
//        }catch (Exception e){
//            e.printStackTrace();
//            Toast.makeText(this, "Already Unbinded", Toast.LENGTH_SHORT).show();
//
//        }
//
//        try {
//
//            stopService(mServiceIntent);
//            Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
//            mService.StopService();
//            Toast.makeText(this, "STOP SELF", Toast.LENGTH_SHORT).show();
//        }catch (Exception e){
//            e.printStackTrace();
//            Toast.makeText(this, "Already Stopped", Toast.LENGTH_SHORT).show();
//
//        }


        //*//stopService(serviceIntent);

        //if(mServiceIsBound){
        //    unbindService(mServiceConnection);
        //    mServiceIsBound = false;
        //}
        //Intent intent = new Intent(MainActivity.this, BackgroundService.class);
        //stopService(intent);

        //endregion

    }

    @Override
    protected void onResume(){
        super.onResume();

        mDebugTextView.setText(mServiceIsBound?"true":"false");

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Internal Saving Location for ALL hidden files:
        Utils.internalFilesRoot = new File(getFilesDir().toString());
        Log.i(TAG, "Utils.internalFilesRoot.getAbsolutePath() = " + Utils.internalFilesRoot.getAbsolutePath());



        Utils.deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();

            if(mService != null){
                Toast.makeText(MainActivity.this, "mService NOT NULL",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(MainActivity.this, "mService NULL",Toast.LENGTH_LONG).show();
            }

            Log.i("serviceID","mService = " + mService);

            mServiceIsBound = true;
            mService.onStartCommand(mServiceIntent,0,0);                   // TODO: is there a better way ? (with startService was called automated)
            mDebugTextView.setText(mServiceIsBound?"true":"false");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            mServiceIsBound = false;
            mDebugTextView.setText(mServiceIsBound?"true":"false");
        }
    };

}
