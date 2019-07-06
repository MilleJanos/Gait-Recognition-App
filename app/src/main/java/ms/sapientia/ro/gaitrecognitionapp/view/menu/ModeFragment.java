package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.Animator;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.logic.FirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.model.MyFirebaseUser;
import ms.sapientia.ro.gaitrecognitionapp.model.NavigationMenuFragmentItem;
import ms.sapientia.ro.gaitrecognitionapp.presenter.menu.ModeFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.service.Recorder;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

/**
 * This class is responsible for Training, Authentication, Data Collection and
 * manages the BackgroundService.
 *
 * @author Mille Janos
 */
public class ModeFragment extends NavigationMenuFragmentItem implements ModeFragmentPresenter.View {

    // Constant members:
    private static final String TAG = "ModeFragment";
    // Static members:
    public static ModeFragment sInstance;
    // MVP member:
    private ModeFragmentPresenter mPresenter;
    // Final members:
    private final int selectedColor = Color.BLACK;
    private final int notSelectedColor = Color.DKGRAY;
    private final int selectedDescriptionColor = Color.BLACK;
    private final int notSelectedDescriptionColor = Color.DKGRAY;
    // View members:
    private Switch mServiceSwitch;
    private LinearLayout mTrainSection;
    private LinearLayout mAuthenticationSection;
    private LinearLayout mCollectDataSection;
    private CheckBox mTrainNewOneCheckBox;
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

        MainActivity.sInstance.setTitle("Mode");

        initView(view);
        bindClickListeners();
        mPresenter = new ModeFragmentPresenter(this);
        sInstance = this;

        // SetLastState
        restoreLastState();

        Animator.Slide(view.findViewById(R.id.item_train), 0, 0, -100, 0, 1000);
        Animator.Slide(view.findViewById(R.id.item_auth), 0, 0, -130, 0, 1000);
        Animator.Slide(view.findViewById(R.id.item_collect_data), 0, 0, -160, 0, 1000);

    }

    /**
     * This method binds the view elements.
     * @param view
     */
    private void initView(View view) {
        mServiceSwitch = view.findViewById(R.id.service_switch);
        mTrainSection = view.findViewById(R.id.item_train);
        mAuthenticationSection = view.findViewById(R.id.item_auth);
        mCollectDataSection = view.findViewById(R.id.item_collect_data);
        mTrainNewOneCheckBox = view.findViewById(R.id.train_new_switch);
    }

    /**
     * This method binds the listeners to view elements.
     */
    private void bindClickListeners() {
        mServiceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> click_switch(buttonView,isChecked) );
        mTrainSection.setOnClickListener(v -> click_train()) ;
        mAuthenticationSection.setOnClickListener(v -> click_authenticate() );
        mCollectDataSection.setOnClickListener(v -> click_collect() );
        mTrainNewOneCheckBox.setOnClickListener(v -> click_train_new_switch(v));
    }

    /**
     * This method binds handles the switch click events.
     * @param buttonView
     * @param isChecked
     */
    public void click_switch(View buttonView, boolean isChecked){
        if (isChecked) {
            // switch on

            // Do not start auth. if no trained data.
            if( AppUtil.sUser.selected_mode == Recorder.Mode.MODE_AUTHENTICATE && AppUtil.sUser.merged_model_count <= 0 && AppUtil.sUser.raw_count <= 0){
                Toast.makeText(MainActivity.sContext,"Train before authentication!",Toast.LENGTH_SHORT).show();
                mServiceSwitch.setChecked(false);
                return;
            }

            // Start Service
            mPresenter.StartServiceIfNotRunning();
        } else {
            // switch off

            // Stop Service
            mPresenter.StopService();
        }
    }

    /**
     * This method sets the service mode to train.
     */
    public void click_train(){
        if( mPresenter.isServiceRunning(BackgroundService.NAME) ){
            Toast.makeText(MainActivity.sContext, "Turn off the service before!", Toast.LENGTH_SHORT).show();
            return;
        }
        saveSelectedMode( Recorder.Mode.MODE_TRAIN );
    }

    /**
     * This method sets the service mode to authentication.
     */
    public void click_authenticate(){
        if( mPresenter.isServiceRunning(BackgroundService.NAME) ){
            Toast.makeText(MainActivity.sContext, "Turn off the service before!", Toast.LENGTH_SHORT).show();
            return;
        }
        saveSelectedMode( Recorder.Mode.MODE_AUTHENTICATE );
    }

    /**
     * This method sets the service mode to collect data.
     */
    public void click_collect(){
        if( mPresenter.isServiceRunning(BackgroundService.NAME) ){
            Toast.makeText(MainActivity.sContext, "Turn off the service before!", Toast.LENGTH_SHORT).show();
            return;
        }
        saveSelectedMode( Recorder.Mode.MODE_COLLECT_DATA );
    }

    /**
     * This method handles the two train mode.
     * - Train new one.
     * - Train the last trained.
     * (Future plans)
     */
    public void click_train_new_switch(View view){
        if( mPresenter.isServiceRunning(BackgroundService.NAME) ){
            Toast.makeText(MainActivity.sContext, "Turn off the service before!", Toast.LENGTH_SHORT).show();
            // set it how it was before click
            ((CheckBox) view).setChecked( ! ((CheckBox) view).isChecked() );
            return;
        }
        AppUtil.sTrainNewOne = ((CheckBox) view).isChecked();
    }

    /**
     * This method saves the selected mode to firebase.
     * @param mode
     */
    public void saveSelectedMode(Recorder.Mode mode){
        // Save changes local;
        AppUtil.sUser.selected_mode = mode;
        // Save changes firebase;
        FirebaseController.setUserObject( AppUtil.sUser );
        // Show:
        selectMode(mode);
    }

    /**
     * This method refreshes the selection on UI.
     */
    public void selectMode(Recorder.Mode mode){
        resetSelection();
        AppUtil.sUser.selected_mode = mode;
        setSelection(mode);
    }

    /**
     * This method resets the selection on UI.
     */
    @SuppressLint("ResourceAsColor")
    private void resetSelection(){
        // Set Titles color:
        ((TextView) mTrainSection.getChildAt(0)).setTextColor( notSelectedColor );
        ((TextView) mAuthenticationSection.getChildAt(0)).setTextColor( notSelectedColor );
        ((TextView) mCollectDataSection.getChildAt(0)).setTextColor( notSelectedColor );
        // Set Images color
        ((ImageView)((LinearLayout) mTrainSection.getChildAt(1)).getChildAt(0)).setColorFilter( notSelectedColor );
        ((ImageView)((LinearLayout) mAuthenticationSection.getChildAt(1)).getChildAt(0)).setColorFilter( notSelectedColor );
        ((ImageView)((LinearLayout) mCollectDataSection.getChildAt(1)).getChildAt(0)).setColorFilter( notSelectedColor );
        // Set Description color
        ((TextView)((LinearLayout) mTrainSection.getChildAt(1)).getChildAt(1)).setTextColor( notSelectedDescriptionColor );
        ((TextView)((LinearLayout) mAuthenticationSection.getChildAt(1)).getChildAt(1)).setTextColor( notSelectedDescriptionColor );
        ((TextView)((LinearLayout) mCollectDataSection.getChildAt(1)).getChildAt(1)).setTextColor( notSelectedDescriptionColor );
    }

    /**
     * This method sets the selected item on UI.
     * @param mode
     */
    private void setSelection(Recorder.Mode mode){

       if( mode == Recorder.Mode.MODE_TRAIN){
           // Set Title color:
           ((TextView) mTrainSection.getChildAt(0)).setTextColor( selectedColor );
           // Set Image color:
           ((ImageView)((LinearLayout) mTrainSection.getChildAt(1)).getChildAt(0)).setColorFilter( selectedColor );
           // Set Description color:
           ((TextView)((LinearLayout) mTrainSection.getChildAt(1)).getChildAt(1)).setTextColor( selectedDescriptionColor );
       }

        if( mode == Recorder.Mode.MODE_AUTHENTICATE){
            // Set Title color:
            ((TextView) mAuthenticationSection.getChildAt(0)).setTextColor( selectedColor );
            // Set Image color:
            ((ImageView)((LinearLayout) mAuthenticationSection.getChildAt(1)).getChildAt(0)).setColorFilter( selectedColor );
            // Set Description color:
            ((TextView)((LinearLayout) mAuthenticationSection.getChildAt(1)).getChildAt(1)).setTextColor( selectedDescriptionColor );
        }

        if( mode == Recorder.Mode.MODE_COLLECT_DATA){
            // Set Title color:
            ((TextView) mCollectDataSection.getChildAt(0)).setTextColor( selectedColor );
            // Set Image color:
            ((ImageView)((LinearLayout) mCollectDataSection.getChildAt(1)).getChildAt(0)).setColorFilter( selectedColor );
            // Set Description color:
            ((TextView)((LinearLayout) mCollectDataSection.getChildAt(1)).getChildAt(1)).setTextColor( selectedDescriptionColor );
        }
    }

    /**
     * This method loads the selected mode from user object.
     */
    private void restoreLastState(){
        boolean checked = mPresenter.isServiceRunning(BackgroundService.NAME);
        mServiceSwitch.setChecked(checked);
        //restoreSelectedMode();  // sets: AppUtil.sMode
        resetSelection();


        // After downloading the user object set the selected mode: (already set after login/register)
        // downloadUserObject(AppUtil.sAuth.getUid(), user -> {
        //     AppUtil.sUser = user;
        //     setSelection( user.selected_mode );
        //     hideProgressBar();
        // });
        setSelection( AppUtil.sUser.selected_mode );
        hideProgressBar();
    }

    /**
     * This method shows the progress bar.
     */
    @Override
    public void showProgressBar() {
        MainActivity.sInstance.showProgressBar();
    }

    /**
     * This method hides the progress bar.
     */
    @Override
    public void hideProgressBar() {
        MainActivity.sInstance.hideProgressBar();
    }
}
