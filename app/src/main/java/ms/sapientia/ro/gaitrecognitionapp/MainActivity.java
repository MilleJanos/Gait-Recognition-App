package ms.sapientia.ro.gaitrecognitionapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import java.io.File;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.Presenter.fragment.LoginFragment;
import ms.sapientia.ro.gaitrecognitionapp.Presenter.LoginPresenter;
import ms.sapientia.ro.gaitrecognitionapp.Presenter.interfaces.ILoginPresenter;
import ms.sapientia.ro.gaitrecognitionapp.service.ActivityBase;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.service.FirebaseUtils;
import ms.sapientia.ro.gaitrecognitionapp.service.Utils;

import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService.LocalBinder;

public class MainActivity extends ActivityBase {

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init. Firebase
        FirebaseApp.initializeApp(this);
        FirebaseUtils.Init( MainActivity.this );

        // Check internet permission
        if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 212);

            Log.e(TAG, "No Internet Permission!");
            Toast.makeText(MainActivity.this,"No Internet Permission!", Toast.LENGTH_LONG).show();
        }

        // MVP test
        //mLoginPresenter = new LoginPresenter(this);
        //EVENT:  button-onClick: mLoginPresenter.onLogin(_,_);

        // Fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,new LoginFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void bindViews() {

    }

    @Override
    protected void bindClickListeners() {

    }

    @Override
    public void onBackPressed() {
        //TODO: ON BACK PRESSED !
        //Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        //if(fragment != null){
        //    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //    fragmentTransaction.remove(fragment);
        //    fragmentTransaction.commit();
        //}else{
        //    super.onBackPressed();
        //}
    }
}
