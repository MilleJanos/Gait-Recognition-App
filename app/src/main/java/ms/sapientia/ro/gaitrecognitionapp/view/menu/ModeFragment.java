package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.logic.FirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.model.ICallback;
import ms.sapientia.ro.gaitrecognitionapp.model.ISimpleCallback;
import ms.sapientia.ro.gaitrecognitionapp.model.MyFirebaseUser;
import ms.sapientia.ro.gaitrecognitionapp.model.NavigationMenuFragmentItem;
import ms.sapientia.ro.gaitrecognitionapp.presenter.menu.ModeFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.service.Recorder;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;


public class ModeFragment extends NavigationMenuFragmentItem implements ModeFragmentPresenter.View {

    private static final String TAG = "ModeFragment";
    public static ModeFragment sInstance;

    // View members:
    Switch mServiceSwitch;
    LinearLayout mTrainSection;
    LinearLayout mAuthenticationSection;
    LinearLayout mCollectDataSection;

    // MVP:
    private ModeFragmentPresenter mPresenter;

    // Private members:
    private MyFirebaseUser auxUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        bindClickListeners();
        mPresenter = new ModeFragmentPresenter(this);
        sInstance = this;

        // SetLastState
        setLastState();

    }

    private void initView(View view) {
        mServiceSwitch = view.findViewById(R.id.service_switch);
        mTrainSection = view.findViewById(R.id.item_train);
        mAuthenticationSection = view.findViewById(R.id.item_auth);
        mCollectDataSection = view.findViewById(R.id.item_collect_data);
    }

    private void bindClickListeners() {
        mServiceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> clickSwitch(buttonView,isChecked) );
        mTrainSection.setOnClickListener(v -> click_train()) ;
        mAuthenticationSection.setOnClickListener(v -> click_authenticate() );
        mCollectDataSection.setOnClickListener(v -> click_collect() );
    }

    public void clickSwitch(View buttonView, boolean isChecked){
        if (isChecked) {
            // switch on

            // Start Service
            mPresenter.StartServiceIfNotRunning();
        } else {
            // switch off

            // Stop Service
            mPresenter.StopService();
        }
    }

    public void click_train(){
        saveSelectedMode( Recorder.Mode.MODE_TRAIN );
    }

    public void click_authenticate(){
        saveSelectedMode( Recorder.Mode.MODE_AUTHENTICATE );
    }

    public void click_collect(){
        saveSelectedMode( Recorder.Mode.MODE_COLLECT_DATA );
    }


    public void saveSelectedMode(Recorder.Mode mode){
        // Save changes local;
        MyFirebaseUser user = AppUtil.sUser;
        user.selected_mode = mode;
        AppUtil.sUser = user;
        // Save changes firebase;
        FirebaseController.setUserObject( user );
        // Show:
        selectMode(mode);
    }


    public void restoreSelectedMode(){
        // AppUtil.sMode = Get mode from firebase:
        // TODO
        // selectMode( sMode )
        // TODO
    }

    public void selectMode(Recorder.Mode mode){
        resetSelection();
        AppUtil.sMode = mode;
        setSelection(mode);
    }

    private void resetSelection(){
        TextView title;
        title = (TextView) mTrainSection.getChildAt(0);
        title.setTextColor(Color.DKGRAY);
        title = (TextView) mAuthenticationSection.getChildAt(0);
        title.setTextColor(Color.DKGRAY);
        title = (TextView) mCollectDataSection.getChildAt(0);
        title.setTextColor(Color.DKGRAY);
    }

    private void setSelection(Recorder.Mode mode){

       if( mode == Recorder.Mode.MODE_TRAIN){

            TextView title = (TextView) mTrainSection.getChildAt(0);
            title.setTextColor(Color.BLUE);
        }

        if( mode == Recorder.Mode.MODE_AUTHENTICATE){

            TextView title = (TextView) mAuthenticationSection.getChildAt(0);
            title.setTextColor(Color.BLUE);
        }

        if( mode == Recorder.Mode.MODE_COLLECT_DATA){

            TextView title = (TextView) mCollectDataSection.getChildAt(0);
            title.setTextColor(Color.BLUE);
        }
    }

    private void setLastState(){
        boolean checked = mPresenter.isServiceRunning(BackgroundService.NAME);
        mServiceSwitch.setChecked(checked);
        restoreSelectedMode();  // sets: AppUtil.sMode
        resetSelection();

        // After downloading the user object set the selected mode:
        downloadUserObject(AppUtil.sAuth.getUid(), user -> {
            AppUtil.sUser = user;
            setSelection( user.selected_mode );
            hideProgressBar();
        });
    }

    private void downloadUserObject(String user_id, ISimpleCallback sc){

        showProgressBar();

        new FirebaseController().getUserObjectById(user_id, new ICallback() {
            @Override
            public void Success(MyFirebaseUser user) {
                sc.Do( user );
            }

            @Override
            public void Failure() {
                Toast.makeText(MainActivity.sContext, "Error downloading user informations!", Toast.LENGTH_LONG).show();
                hideProgressBar();
            }

            @Override
            public void Error(int error_code) {
                Toast.makeText(MainActivity.sContext, "Error downloading user informations!", Toast.LENGTH_LONG).show();
                hideProgressBar();
            }
        });
    }

    ///**
    // * Radio button view items will call this
    // * method automatic.
    // * @param checked_id radio button checked button id
    // */
    //public void RadioButtonClicked(int checked_id) {
    //    // Is the button now checked?
    //    //boolean checked = ((RadioButton) view).isChecked();
    //
    //    // Check which radio button was clicked
    //    switch(checked_id) {
    //
    //        case R.id.item_train:
    //            mPresenter.Prepare4Train();
    //            break;
    //
    //        case R.id.item_auth:
    //            mPresenter.Prepare4Authentication();
    //            break;
    //
    //        case R.id.item_collect_data:
    //            mPresenter.Prepare4DataCollection();
    //            break;
    //    }
    //}

    @Override
    public void showProgressBar() {
        MainActivity.sInstance.showProgressBar();
    }

    @Override
    public void hideProgressBar() {
        MainActivity.sInstance.hideProgressBar();
    }
}
