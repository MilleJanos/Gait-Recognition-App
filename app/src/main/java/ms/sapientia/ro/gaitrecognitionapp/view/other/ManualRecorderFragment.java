package ms.sapientia.ro.gaitrecognitionapp.view.other;


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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.presenter.other.ManualRecorderFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.service.RecorderUtils;

public class ManualRecorderFragment extends Fragment implements ManualRecorderFragmentPresenter.View {

    private static final String TAG = "HomeFragmentPresenter";

    // Constants
    public static String PREF_IS_RUNNING = "is_service_running";

    // UI Components
    //private EditText mEditText;
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
    ManualRecorderFragmentPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manual_recorder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new ManualRecorderFragmentPresenter(this);
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
    ////////////////////////    sContext = getContext();
    ////////////////////////}


    //@Override
    protected void initView(View view) {
        //mEditText = view.findViewById(R.id.edit_text_input);
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


    private void bind() {

        mServiceIntent = new Intent(getContext(), BackgroundService.class);
        mServiceIntent.putExtra(RecorderUtils.INPUT_CREATE_OR_VERIFY, !mModelSwitch.isChecked());

        try {
            getContext().bindService(
                    mServiceIntent,
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Toast.makeText(context, "Can't bind!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Can't bind!");
            e.printStackTrace();
        }
    }


    public void startService(View v) {
        // If service exists then restart it, do not create a new one

        if (BackgroundService.sInstance == null || BackgroundService.sInstance.getStoredService() == null) {
            Thread thread = new Thread() {
                public void start() {
                    bind();
                }
            };
            thread.start();
        } else {
            mService = BackgroundService.sInstance.getStoredService();
            if (!BackgroundService.sIsRunning) {
                mService.StartRecording(AppUtil.sUser.selected_mode, AppUtil.sTrainNewOne);
            }
        }
    }


    public void stopService(View v) {

        //mServiceIntent = new Intent(getContext(), BackgroundService.class);
        //mServiceIntent.putExtra(RecorderUtils.INPUT_CREATE_OR_VERIFY, ! mModelSwitch.isChecked() );

        // TRY TO GET mService:

        if (BackgroundService.sInstance != null) {
            bind();
        }
        //if(mService == null) {
        //    if (BackgroundService.sInstance != null) {
        //        mService = BackgroundService.sInstance.getStoredService();
        //    }
        //}

        // USE IT TO STOP THE RUNNING SERVICE:

        if (mService != null) {
            mService.StopService();
            getContext().unbindService(mServiceConnection);
        } else {
            if (BackgroundService.sInstance != null) {
                BackgroundService.sInstance.StopService();
                getContext().unbindService(mServiceConnection);
            }
        }

    }

    //@Override
    //protected void onResume(){
    //    super.onResume();
    //    //sharedPref//mDebugTextView.setText(sIsRunning(this)?"true":"false");
    //}

    //@Override
    //protected void onStart() {
    //    super.onStart();
    //
    //    //if(BackgroundService.sInstance != null && BackgroundService.sIsRunning){
    //    //    bind();
    //    //}
    //
    //    //DEBUG//try {
    //    //DEBUG//    BackgroundService.sInstance.getStoredService().onRebind(mServiceIntent);
    //    //DEBUG//    Toast.makeText(sContext, "onRebind OK", Toast.LENGTH_SHORT).show();
    //    //DEBUG//}catch (Exception e){
    //    //DEBUG//    Toast.makeText(sContext, "onRebind FAIL", Toast.LENGTH_SHORT).show();
    //    //DEBUG//}
    //
    //
    //}

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) service;
            mService = binder.getService();

            BackgroundService.sInstance.setStoredService(mService);

            Log.i("serviceID", "mService = " + mService);

            mService.onStartCommand(mServiceIntent, 0, 0);                   // TODO: is there a better way ? (with startService was called automated)

            //sharedPref//setRunning(true);
            //sharedPref//mDebugTextView.setText(sIsRunning(sContext)?"true":"false");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            int x;

            //sharedPref//setRunning(false);
            //sharedPref//mDebugTextView.setText(sIsRunning(sContext)?"true":"false");
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
//sharedPref//private static boolean sIsRunning(sContext sContext){
//sharedPref//    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(sContext.getContext());
//sharedPref//    return pref.getBoolean(PREF_IS_RUNNING,false);
//sharedPref//}
}