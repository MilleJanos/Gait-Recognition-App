package ms.sapientia.ro.gaitrecognitionapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
import java.security.spec.EncodedKeySpec;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.service.ActivityBase;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.service.FirebaseUtils;
import ms.sapientia.ro.gaitrecognitionapp.service.Utils;

import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService.LocalBinder;

public class MainActivity extends ActivityBase {

    private static final String TAG = "MainActivity";

    // Constants
    public static String PREF_IS_RUNNING = "is_service_running";

    // UI Components
    private EditText mEditText;
    private Button mStartServiceButton;
    private Button mStopServiceButton;
    private Switch mModelSwitch;
    private TextView mDebugTextView;

    // Vars
    private Context context = null;
    private Intent mServiceIntent;
    //private ServiceConnection mServiceConnection; // declared at the bottom
    BackgroundService mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        FirebaseApp.initializeApp(this);
        FirebaseUtils.Init( MainActivity.this );

        bindViews();
        bindClickListeners();
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
                    startService(v);
                    mModelSwitch.setEnabled(false);
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


    private void bind(){

        mServiceIntent = new Intent(getApplicationContext(), BackgroundService.class);
        mServiceIntent.putExtra(Utils.INPUT_CREATE_OR_VERIFY, ! mModelSwitch.isChecked() );

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

        // If service exists then restart it, do not create a new one

        if( BackgroundService.Instance == null || BackgroundService.Instance.getStoredService() == null ) {
            Thread thread = new Thread() {
                public void start() {
                    bind();
                }
            };
            thread.start();
        } else {
            mService = BackgroundService.Instance.getStoredService();
            if( ! BackgroundService.isRunning ){
                mService.StartRecording();
            }
        }
    }


    public void stopService(View v) {

        //mServiceIntent = new Intent(getApplicationContext(), BackgroundService.class);
        //mServiceIntent.putExtra(Utils.INPUT_CREATE_OR_VERIFY, ! mModelSwitch.isChecked() );

        // TRY TO GET mService:

        if (BackgroundService.Instance != null) {
            bind();
        }
        //if(mService == null) {
        //    if (BackgroundService.Instance != null) {
        //        mService = BackgroundService.Instance.getStoredService();
        //    }
        //}

        // USE IT TO STOP THE RUNNING SERVICE:

        if(mService != null) {
            mService.StopService();
            getApplicationContext().unbindService(mServiceConnection);
        }else{
            if( BackgroundService.Instance != null){
                BackgroundService.Instance.StopService();
                getApplicationContext().unbindService(mServiceConnection);
            }
        }

    }



    @Override
    protected void onResume(){
        super.onResume();
        //sharedPref//mDebugTextView.setText(isRunning(this)?"true":"false");
    }


    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    protected void onStart() {
        super.onStart();

        //if(BackgroundService.Instance != null && BackgroundService.isRunning){
        //    bind();
        //}

        //DEBUG//try {
        //DEBUG//    BackgroundService.Instance.getStoredService().onRebind(mServiceIntent);
        //DEBUG//    Toast.makeText(context, "onRebind OK", Toast.LENGTH_SHORT).show();
        //DEBUG//}catch (Exception e){
        //DEBUG//    Toast.makeText(context, "onRebind FAIL", Toast.LENGTH_SHORT).show();
        //DEBUG//}

        // Internal Saving Location for ALL hidden files:
        Utils.internalFilesRoot = new File(getFilesDir().toString());
        Log.i(TAG, "Utils.internalFilesRoot.getAbsolutePath() = " + Utils.internalFilesRoot.getAbsolutePath());

        Utils.deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();

            BackgroundService.Instance.setStoredService(mService);

            Log.i("serviceID","mService = " + mService);

            mService.onStartCommand(mServiceIntent,0,0);                   // TODO: is there a better way ? (with startService was called automated)

            //sharedPref//setRunning(true);
            //sharedPref//mDebugTextView.setText(isRunning(context)?"true":"false");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            int x;

            //sharedPref//setRunning(false);
            //sharedPref//mDebugTextView.setText(isRunning(context)?"true":"false");
        }
    };

    //sharedPref//private void setRunning(boolean running){
    //sharedPref//    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    //sharedPref//    SharedPreferences.Editor editor = pref.edit();
    //sharedPref//
    //sharedPref//    editor.putBoolean(PREF_IS_RUNNING, running);
    //sharedPref//
    //sharedPref//    editor.apply();
    //sharedPref//}
    //sharedPref//
    //sharedPref//private static boolean isRunning(Context context){
    //sharedPref//    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    //sharedPref//    return pref.getBoolean(PREF_IS_RUNNING,false);
    //sharedPref//}

}
