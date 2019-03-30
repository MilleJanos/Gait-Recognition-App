package ms.sapientia.ro.gaitrecognitionapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import ms.sapientia.gaitrecognitionapp.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // UI Components
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private Button mButton;

    // Vars
    private MyService mService;
    private MainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindViews();

        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        setObservers();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: toggle updates");
                toggleUpdates();
            }
        });

    }


    private void setObservers() {

        mViewModel.getBinder().observe(this, new Observer<MyService.MyBinder>() {
            @Override
            public void onChanged(@Nullable MyService.MyBinder myBinder) {
                if(myBinder != null){
                    Log.d(TAG, "onChanged: connected to service.");
                    mService = myBinder.getService();
                }else{
                    Log.d(TAG, "onChanged: unbound from service.");
                    mService = null;
                }
            }
        });

        mViewModel.getIsProgressUpdating().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean aBoolean) {
                // Chacking progress in every 100ms:
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        //if(aBoolean){ // if needs to update
                        if(mViewModel.getIsProgressUpdating().getValue()){
                            if(mViewModel.getBinder().getValue() != null){  // meaning the service is bound
                                if(mService.getProgress() == mService.getMaxValue()){
                                    mViewModel.setIsUploading(false);
                                }
                                mProgressBar.setProgress(mService.getProgress());
                                mProgressBar.setMax(mService.getMaxValue());
                                String progress =
                                        String.valueOf(100 * mService.getProgress() / mService.getMaxValue()) + "%";
                                mTextView.setText(progress);
                            }
                            handler.postDelayed(this,100);
                        }else{
                            // Stop chacking for updates
                            handler.removeCallbacks(this);
                        }
                    }
                };

                // control what the button shows
                if(aBoolean){
                    mButton.setText("Pause");
                    handler.postDelayed(runnable, 100);
                }else{
                    if(mService.getProgress() == mService.getMaxValue()){
                        mButton.setText("Restart");
                    }else{
                        mButton.setText("Start");
                    }
                }

            }
        });
    }

    private void toggleUpdates(){
        if(mService != null){
            if(mService.getProgress() == mService.getMaxValue()){
                mService.resetTask();
                mButton.setText("Start");
            }else{
                if(mService.getIsPaused()){
                    mService.unPausePretendLongRunningTask();
                    mViewModel.setIsUploading(true);
                }else{
                    mService.pausePretendLongRunningTask();
                    mViewModel.setIsUploading(false);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mViewModel.getBinder() != null){
            unbindService(mViewModel.getServiceConnection());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startService();
    }

    private void startService(){
        Intent serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);

        bindService();
    }

    private void bindService(){
        Intent serviceIntent = new Intent(this, MyService.class);
        bindService(serviceIntent, mViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    private void BindViews() {
        mProgressBar = findViewById(R.id.progress_bar);
        mTextView = findViewById(R.id.text_view);
        mButton = findViewById(R.id.button);
    }


}
