package ms.sapientia.ro.gaitrecognitionapp.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.presenter.MainFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.service.Utils;

public class MainFragment extends Fragment implements MainFragmentPresenter.View {

    private static final String TAG = "MainFragmentPresenter";

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

    // MVP
    MainFragmentPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new MainFragmentPresenter(this);
        initView(view);
        bindClickListeners();

        //ConstraintLayout layout = (ConstraintLayout) getView().findViewById(R.id.login_fragment);
        //final int sdk = android.os.Build.VERSION.SDK_INT;
        //if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
        //    layout.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.image_asphalt_road) );
        //} else {
        //    layout.setBackground(ContextCompat.getDrawable(getContext(), R.mipmap.image_asphalt_road));
        //}
    }

    ////////////////////////@Override
    ////////////////////////public void onCreate(Bundle savedInstanceState) {
    ////////////////////////    super.onCreate(savedInstanceState);
    ////////////////////////    if (getArguments() != null) {
    ////////////////////////        //mParam1 = getArguments().getString(ARG_PARAM1);
    ////////////////////////        //mParam2 = getArguments().getString(ARG_PARAM2);
    ////////////////////////    }
    ////////////////////////    context = getContext();
    ////////////////////////}


    //@Override
    protected void initView(View view) {
        mEditText = view.findViewById(R.id.edit_text_input);
        mStartServiceButton = view.findViewById(R.id.start_service_button);
        mStopServiceButton = view.findViewById(R.id.stop_service_button);
        mModelSwitch = view.findViewById(R.id.model_switch);
        mDebugTextView = view.findViewById(R.id.debug_text_view);
    }

    //@Override
    protected void bindClickListeners() {
        mStartServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
                //    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 212);
                //    Log.e(TAG, "No Internet Permission!");
                //    Toast.makeText(MainActivity.this,"No Internet Permission!", Toast.LENGTH_LONG).show();
                //}else{
                startService(v);
                mModelSwitch.setEnabled(false);
                //}
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

        mServiceIntent = new Intent(getContext(), BackgroundService.class);
        mServiceIntent.putExtra(Utils.INPUT_CREATE_OR_VERIFY, ! mModelSwitch.isChecked() );

        try {
            getContext().bindService(
                    mServiceIntent,
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Toast.makeText(context, "Can't bind!", Toast.LENGTH_SHORT).show();
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

        //mServiceIntent = new Intent(getContext(), BackgroundService.class);
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
            getContext().unbindService(mServiceConnection);
        }else{
            if( BackgroundService.Instance != null){
                BackgroundService.Instance.StopService();
                getContext().unbindService(mServiceConnection);
            }
        }

    }

    //@Override
    //protected void onResume(){
    //    super.onResume();
    //    //sharedPref//mDebugTextView.setText(isRunning(this)?"true":"false");
    //}

    //@Override
    //protected void onStart() {
    //    super.onStart();
    //
    //    //if(BackgroundService.Instance != null && BackgroundService.isRunning){
    //    //    bind();
    //    //}
    //
    //    //DEBUG//try {
    //    //DEBUG//    BackgroundService.Instance.getStoredService().onRebind(mServiceIntent);
    //    //DEBUG//    Toast.makeText(context, "onRebind OK", Toast.LENGTH_SHORT).show();
    //    //DEBUG//}catch (Exception e){
    //    //DEBUG//    Toast.makeText(context, "onRebind FAIL", Toast.LENGTH_SHORT).show();
    //    //DEBUG//}
    //
    //
    //}

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) service;
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

    @Override
    public void initProgressBar() {

    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    //sharedPref//private void setRunning(boolean running){
    //sharedPref//    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
    //sharedPref//    SharedPreferences.Editor editor = pref.edit();
    //sharedPref//
    //sharedPref//    editor.putBoolean(PREF_IS_RUNNING, running);
    //sharedPref//
    //sharedPref//    editor.apply();
    //sharedPref//}
    //sharedPref//
    //sharedPref//private static boolean isRunning(Context context){
    //sharedPref//    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getContext());
    //sharedPref//    return pref.getBoolean(PREF_IS_RUNNING,false);
    //sharedPref//}

}
