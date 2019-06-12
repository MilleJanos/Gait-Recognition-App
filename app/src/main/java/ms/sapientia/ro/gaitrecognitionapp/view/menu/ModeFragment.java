package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Switch;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.model.NavigationMenuFragmentItem;
import ms.sapientia.ro.gaitrecognitionapp.presenter.menu.ModeFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;


public class ModeFragment extends NavigationMenuFragmentItem implements ModeFragmentPresenter.View {

    private static final String TAG = "ModeFragment";

    // View members:
    Switch mServiceSwitch;
    RadioGroup mRadioGroup;

    // MVP:
    private ModeFragmentPresenter mPresenter;

    // Private members:

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

        // SetLastState
        setLastState();
    }

    private void initView(View view) {
        mServiceSwitch = view.findViewById(R.id.service_switch);
        mRadioGroup = view.findViewById(R.id.radioGroup);

    }

    private void bindClickListeners() {
        mServiceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // switch on

                // Start Service
                mPresenter.StartServiceIfNotRunning();
            } else {
                // switch off

                // Stop Service
                mPresenter.StopService();
            }
        });
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> RadioButtonClicked(checkedId));
    }

    private void setLastState(){
        boolean checked = mPresenter.isServiceRunning(BackgroundService.NAME);
        mServiceSwitch.setChecked(checked);
    }

    /**
     * Radio button view items will call this
     * method automatic.
     * @param checked_id radio button checked button id
     */
    public void RadioButtonClicked(int checked_id) {
        // Is the button now checked?
        //boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(checked_id) {

            case R.id.radio_train:
                mPresenter.Prepare4Train();
                break;

            case R.id.radio_auth:
                mPresenter.Prepare4Authentication();
                break;

            case R.id.radio_collect_data:
                mPresenter.Prepare4DataCollection();
                break;
        }
    }

    @Override
    public void showProgressBar() {
        MainActivity.sInstance.showProgressBar();
    }

    @Override
    public void hideProgressBar() {
        MainActivity.sInstance.hideProgressBar();
    }
}
