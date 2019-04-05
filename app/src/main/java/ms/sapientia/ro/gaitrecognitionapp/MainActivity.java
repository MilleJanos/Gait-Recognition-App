package ms.sapientia.ro.gaitrecognitionapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import java.io.File;

import ms.sapientia.gaitrecognitionapp.R;
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

    // Vars


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                }
            }
        });

        mStopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(v);
            }
        });
    }

    // Service methods:

    public void startService(View v){
        String input = mEditText.getText().toString();

        Intent serviceIntent = new Intent(this, BackgroundService.class);
        //serviceIntent.putExtra(Utils.INPUT_EXTRA_KEY, input);

        startService(serviceIntent);
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
    }
}
