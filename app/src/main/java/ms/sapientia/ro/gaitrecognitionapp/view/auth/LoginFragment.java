package ms.sapientia.ro.gaitrecognitionapp.view.auth;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.Animator;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.presenter.auth.LoginFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

public class LoginFragment extends Fragment implements LoginFragmentPresenter.View {
    // Static members:
    public static LoginFragment sInstance;

    // View members:
    public EditText mEmailEditText;
    public EditText mPasswordEditText;
    private Button mLoginButton;
    private TextView mRegisterTextViewButton;
    private TextView mForgottPasswordTextViewButton;

    // MVP:
    private LoginFragmentPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new LoginFragmentPresenter(this);
        initView(view);
        bindClickListeners();
        sInstance = this;

        Animator.LogoIntro(view.findViewById(R.id.ic_imageview));
        Animator.Slide(view.findViewById(R.id.login_title_textview),-1000,0,0,0);

        //region check SDK
        //ConstraintLayout layout = (ConstraintLayout) getView().findViewById(R.id.login_fragment);
        //final int sdk = android.os.Build.VERSION.SDK_INT;
        //if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
        //    layout.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.image_asphalt_road) );
        //} else {
        //    layout.setBackground(ContextCompat.getDrawable(getContext(), R.mipmap.image_asphalt_road));
        //}
        //endregion
    }

    private void initView(View view){
        mEmailEditText = getView().findViewById(R.id.email_editText);
        mPasswordEditText = getView().findViewById(R.id.password_editText);
        mLoginButton = getView().findViewById(R.id.login_button);
        mRegisterTextViewButton = getView().findViewById(R.id.sign_up_textviewbutton);
        mForgottPasswordTextViewButton = getView().findViewById(R.id.forgot_password_textviewbutton);
    }

    public void bindClickListeners() {
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtil.hideKeyboard(MainActivity.sInstance);
                loginButtonClick(v);
            }
        });
        mRegisterTextViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerButtonClick(v);
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
        mPasswordEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordEditText.setError(null);
            }
        });
        // Edit Text enter events
        mEmailEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // jump to password edit text
                    mPasswordEditText.requestFocus();
                    return true;
                }
                return false;
            }
        });
        mPasswordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // jump to password edit text
                    AppUtil.hideKeyboard(MainActivity.sInstance);
                    loginButtonClick(v);
                    return true;
                }
                return false;
            }
        });
    }

    public void loginButtonClick(View view){
        // Reset errors:
        mPresenter.resetErrors(mEmailEditText, mPasswordEditText);

        // Get input values:
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        if(email.equals("x") && password.equals("")){       // TODO: DELETE THIS !
            email = "millejanos31@gmail.com";
            password = "01234567";
            LoginFragment.sInstance.mEmailEditText.setText(email);
            LoginFragment.sInstance.mPasswordEditText.setText(password);
        }

        // Try to login:
        if( mPresenter.verifyInputs(email,password,mEmailEditText,mPasswordEditText) ) {

            mPresenter.Login(email,password);

        }
    }

    public void registerButtonClick(View view){
        mPresenter.goToRegisterPage();
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
}
