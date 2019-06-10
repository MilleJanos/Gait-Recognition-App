package ms.sapientia.ro.gaitrecognitionapp.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.MainActivity;
import ms.sapientia.ro.gaitrecognitionapp.presenter.LoginFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.presenter.RegisterFragmentPresenter;


public class RegisterFragment extends Fragment implements RegisterFragmentPresenter.View {

    private static final String TAG = "RegisterFragment";

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
                    mEmailEditText.requestFocus();
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
                    registerButtonClick(v);
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
                    registerButtonClick(v);
                    return true;
                }
                return false;
            }
        });
    }

    public void registerButtonClick(View view){
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
        mPresenter.goBackToLoginPage();
    }

    public void forgottPasswordClick(View view){

        // TODO forgottPasswordClick()

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