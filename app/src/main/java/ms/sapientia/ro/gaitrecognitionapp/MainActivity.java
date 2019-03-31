package ms.sapientia.ro.gaitrecognitionapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.service.Common;

public class MainActivity extends AppCompatActivity {

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

        BindViews();
        BindClickListeners();
    }

    private void BindViews() {
        mEditText = findViewById(R.id.edit_text_input);
        mStartServiceButton = findViewById(R.id.start_service_button);
        mStopServiceButton = findViewById(R.id.stop_service_button);
    }

    private void BindClickListeners() {
        mStartServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(v);
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
        //serviceIntent.putExtra(Common.INPUT_EXTRA_KEY, input);

        startService(serviceIntent);
    }

    public void stopService(View v){
        Intent serviceIntent = new Intent(this, BackgroundService.class);
        stopService(serviceIntent);
    }


}
