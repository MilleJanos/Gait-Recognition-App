package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.logic.FirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.model.NavigationMenuFragmentItem;
import ms.sapientia.ro.gaitrecognitionapp.presenter.menu.EditProfileFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

public class EditProfileFragment extends NavigationMenuFragmentItem implements EditProfileFragmentPresenter.View {

    private static final String TAG = "EditProfileFragment";

    // MVP:
    private EditProfileFragmentPresenter mPresenter;

    // View members
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mPhoneNumberEditText;
    private DatePicker mBirthDateDatePicker;
    private Button mSaveButton;
    private Button mCancelButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.sInstance.setTitle("Edit profile");

        mPresenter = new EditProfileFragmentPresenter(this);

        initView(view);
        bindClickListeners();

        loadCurrentUserData();
    }

    private void initView(View view) {
        mFirstNameEditText = view.findViewById(R.id.first_name_textview);
        mLastNameEditText = view.findViewById(R.id.last_name_textview);
        mPhoneNumberEditText = view.findViewById(R.id.phone_number_textview);
        mBirthDateDatePicker = view.findViewById(R.id.birth_date_datepicker);
        mSaveButton = view.findViewById(R.id.save_button);
        mCancelButton = view.findViewById(R.id.cancel_button);
    }

    private void bindClickListeners() {
        mFirstNameEditText.setOnClickListener( v -> mFirstNameEditText.setError(null) );
        mLastNameEditText.setOnClickListener( v -> mLastNameEditText.setError(null) );
        mPhoneNumberEditText.setOnClickListener( v -> mPhoneNumberEditText.setError(null) );
        mSaveButton.setOnClickListener( v -> saveChanges() );
        mCancelButton.setOnClickListener( v -> cancelChanges());

        mFirstNameEditText.setOnKeyListener((View.OnKeyListener) (v, keyCode, event) -> {
            //If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // jump to next field
                mLastNameEditText.requestFocus();
                return true;
            }
            return false;
        });

        mLastNameEditText.setOnKeyListener((View.OnKeyListener) (v, keyCode, event) -> {
            //If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // jump to next field
                mPhoneNumberEditText.requestFocus();
                return true;
            }
            return false;
        });

        mPhoneNumberEditText.setOnKeyListener((View.OnKeyListener) (v, keyCode, event) -> {
            //If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // jump to next field
                mBirthDateDatePicker.requestFocus();
                return true;
            }
            return false;
        });
    }

    private void loadCurrentUserData(){
        mFirstNameEditText.setText( AppUtil.sUser.first_name );
        mLastNameEditText.setText( AppUtil.sUser.last_name );
        mPhoneNumberEditText.setText( AppUtil.sUser.phone_number );
        long timestamp = AppUtil.sUser.birth_date;
        Date date = new Date(timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        mBirthDateDatePicker.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                null
        );

    }

    private void saveChanges(){
        // Verify inputs:
        if( ! (mFirstNameEditText.length() > 1) ){
            mFirstNameEditText.requestFocus();
            mFirstNameEditText.setError("Invalid field");
            return;
        }

        if( ! (mLastNameEditText.length() > 1) ){
            mLastNameEditText.requestFocus();
            mLastNameEditText.setError("Invalid field");
            return;
        }

        if( mPhoneNumberEditText.getText().toString().charAt(0) != '+' ){
            mPhoneNumberEditText.requestFocus();
            mPhoneNumberEditText.setError("Phone number has to start with \"+\"");
            return;
        }

        if( ! (mPhoneNumberEditText.length() > 6) ){
            mPhoneNumberEditText.requestFocus();
            mPhoneNumberEditText.setError("Invalid field");
            return;
        }

        AppUtil.sUser.first_name = mFirstNameEditText.getText().toString();
        AppUtil.sUser.last_name = mLastNameEditText.getText().toString();
        AppUtil.sUser.phone_number = mPhoneNumberEditText.getText().toString();
        Calendar calendar = new GregorianCalendar(mBirthDateDatePicker.getYear(), mBirthDateDatePicker.getMonth(), mBirthDateDatePicker.getDayOfMonth());
        AppUtil.sUser.birth_date = calendar.getTimeInMillis();

        // Upload them:
        FirebaseController.setUserObject( AppUtil.sUser );

        Toast.makeText(MainActivity.sContext,"Saved.",Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> MainActivity.sInstance.onBackPressed(),1000);

        // Refresh drawer
        MainActivity.sInstance.refreshNavigationMenuDraverNameAndEmail();
    }

    private void cancelChanges(){

        MainActivity.sInstance.onBackPressed();

        // // Ask to cancel:
        // AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.sContext );
        //
        // builder.setTitle("Confirm");
        // builder.setMessage("Are you sure you want to cancel changes?");
        //
        // builder.setPositiveButton("YES", (dialog, which) -> {
        //     // Do nothing but close the dialog
        //     MainActivity.sInstance.onBackPressed();
        //     dialog.dismiss();
        // });
        //
        // builder.setNegativeButton("NO", (dialog, which) -> {
        //
        //     // Do nothing
        //     dialog.dismiss();
        // });
        // AlertDialog alert = builder.create();
        // alert.show();
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
