package ms.sapientia.ro.gaitrecognitionapp.view.auth;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.model.IAfter;
import ms.sapientia.ro.gaitrecognitionapp.presenter.auth.RegisterFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;


public class RegisterFragment extends Fragment implements RegisterFragmentPresenter.View {

    // Constants:
    private static final String TAG = "RegisterFragment";

    // Static members:
    public static RegisterFragment sInstance;

    // View members:
    public EditText mEmailEditText;
    public EditText mPasswordEditText1;
    public EditText mPasswordEditText2;
    private Button mRegisterButton;
    private TextView mLoginTextViewButton;
    private TextView mForgottPasswordTextViewButton;

    // MVP
    private RegisterFragmentPresenter mPresenter;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new RegisterFragmentPresenter(this);
        initView(view);
        bindClickListeners();
        sInstance = this;
    }

    private void initView(View view){
        mEmailEditText = view.findViewById(R.id.email_editText);
        mPasswordEditText1 = view.findViewById(R.id.password_editText_1);
        mPasswordEditText2 = view.findViewById(R.id.password_editText_2);
        mRegisterButton = view.findViewById(R.id.register_button);
        mLoginTextViewButton = view.findViewById(R.id.log_in_textviewbutton);
        mForgottPasswordTextViewButton = view.findViewById(R.id.forgot_password_textviewbutton);
    }

    private void bindClickListeners(){
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtil.hideKeyboard(MainActivity.sInstance);
                registerButtonClick(v);
            }
        });
        mLoginTextViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButtonClick(v);
            }
        });
        mForgottPasswordTextViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgottPasswordClick(v);
            }
        });
        // Clear errors on Edit Text on click
        mEmailEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmailEditText.setError(null);
            }
        });
        mPasswordEditText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordEditText1.setError(null);
            }
        });
        mPasswordEditText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordEditText2.setError(null);
            }
        });
        // Edit Text enter events
        mEmailEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // jump to password edit text
                    mPasswordEditText1.requestFocus();
                    return true;
                }
                return false;
            }
        });
        mPasswordEditText1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // jump to password edit text
                    mPasswordEditText2.requestFocus();
                    return true;
                }
                return false;
            }
        });
        mPasswordEditText2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // jump to password edit text
                    AppUtil.hideKeyboard(MainActivity.sInstance);
                    registerButtonClick(v);
                    return true;
                }
                return false;
            }
        });
    }

    public void registerButtonClick(View view){

        if( MainActivity.sIsProgressBarShown ){
            return;
        }

        // Reset errors:
        mPresenter.resetErrors(mEmailEditText, mPasswordEditText1, mPasswordEditText1);

        // Get input values:
        String email = mEmailEditText.getText().toString();
        String password1 = mPasswordEditText1.getText().toString();
        String password2 = mPasswordEditText2.getText().toString();

        // Try to Register:
        if( mPresenter.verifyInputs(email,password1,password2,mEmailEditText,mPasswordEditText1,mPasswordEditText2) ) {

            mPresenter.Register(email,password1);

        }
    }

    public void loginButtonClick(View view){

        if( MainActivity.sIsProgressBarShown ){
            return;
        }

        mPresenter.goBackToLoginPage();
    }

    public void forgottPasswordClick(View view){

        if( MainActivity.sIsProgressBarShown ){
            return;
        }

        String email = mEmailEditText.getText().toString();

        if( email.isEmpty() ){
            mPresenter.setErrors("Fill email first!",null,null);
            return;
        }

        AppUtil.requestPasswordReset( email );
    }

    @Override
    public void showProgressBar(IAfter after) {
        MainActivity.sInstance.showProgressBar( after );
    }


    @Override
    public void showProgressBar() {
        MainActivity.sInstance.showProgressBar();
    }

    @Override
    public void hideProgressBar() {
        MainActivity.sInstance.hideProgressBar();
    }

/*
    @Override
    public void onClick(View v) {

    }
*/

}