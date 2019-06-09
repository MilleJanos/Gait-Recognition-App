package ms.sapientia.ro.gaitrecognitionapp.fragment;


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

public class LoginFragment extends Fragment implements LoginFragmentPresenter.View {

    // MVP:
    private LoginFragmentPresenter mPresenter;

    // View:
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private TextView mRegisterTextViewButton;
    private TextView mForgottPasswordTextViewButton;

    // Variables:


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new LoginFragmentPresenter(this);
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

    //@Override
    private void initView(View view){
        mEmailEditText = getView().findViewById(R.id.email_editText);
        mPasswordEditText = getView().findViewById(R.id.password_editText);
        mLoginButton = getView().findViewById(R.id.login_button);
        mRegisterTextViewButton = getView().findViewById(R.id.sign_up_textviewbutton);
        mForgottPasswordTextViewButton = getView().findViewById(R.id.forgot_password_textviewbutton);
    }

    //@Override
    public void bindClickListeners() {
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        // Edit Text error message cleaner
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
                    mEmailEditText.requestFocus();
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
                    loginButtonClick(v);
                    return true;
                }
                return false;
            }
        });
    }




    public void loginButtonClick(View view){
        resetErrors();
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        if( mPresenter.verifyInputs(email,password,mEmailEditText,mPasswordEditText) ) {
            mPresenter.Login(email,password);
        }
    }

    public void registerButtonClick(View view){
        FragmentManager fragmentManager = getFragmentManager();
        MainActivity.Instance.addFragmentToStack(new RegisterFragment(), fragmentManager, "register_fragment");
    }

    public void forgottPasswordClick(View view){

        // TODO forgottPasswordClick()

    }




    public void resetErrors(){
        mEmailEditText.setError(null);
        mPasswordEditText.setError(null);
    }




    @Override
    public void initProgressBar() {
        MainActivity.Instance.initProgressBar();
    }

    @Override
    public void showProgressBar() {
        MainActivity.Instance.showProgressBar();
    }

    @Override
    public void hideProgressBar() {
        MainActivity.Instance.hideProgressBar();
    }
}
