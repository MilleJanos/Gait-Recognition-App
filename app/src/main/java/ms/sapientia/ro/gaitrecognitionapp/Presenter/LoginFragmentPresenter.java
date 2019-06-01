package ms.sapientia.ro.gaitrecognitionapp.Presenter;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.Presenter.interfaces.ILoginPresenter;

public class LoginFragmentPresenter extends Fragment implements ILoginPresenter {

    // View:
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private TextView mRegisterTextViewButton;
    private TextView mForgottPasswordTextViewButton;


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

        if( verifyInputs(email,password) ) {
            onLogin(email,password);
        }
    }

    public void registerButtonClick(View view){
        addFragmentToStack(new RegisterFragmentPresenter(), "register_fragment");
    }

    public void forgottPasswordClick(View view){

        // TODO forgottPasswordClick()

    }

    @Override
    public void onLogin(String email, String password) {

        //TODO

        // IF EMAIL AND PASSWORD IS OK IN FIREBASE>AUTH == >
        // THEN
        addFragmentToStack(new MainFragmentPresenter(), "main_fragment");

    }


    public boolean verifyInputs(String email, String password){
        int errorCode;

        /// Check email:
        errorCode = isEmailFormatWrong(email);

        switch (errorCode){
            case 1:{
                mEmailEditText.setError("Please fill the email field.");
                return false;
            }
            case 2:{
                mPasswordEditText.setError("Email can't contain extra spaces.");
                return false;
            }
            case 3:{
                mEmailEditText.setError("Wrong email format.");
                return false;
            }
        }

        /// Check password:
        errorCode = isPasswordFormatWrong(password);

        switch (errorCode){
            case 1:{
                mPasswordEditText.setError("Please fill the password field");
                return false;
            }
            case 2:{
                mPasswordEditText.setError("Password has to be at least 6 characters.");
                return false;
            }
            case 3:{
                mPasswordEditText.setError("Password can't contain extra spaces.");
                return false;
            }
            case 4:{
                mPasswordEditText.setError("Password can contain only characters, numbers and underscore.");
                return false;
            }
        }

        return true;
    }

    /**
     * Check the email if is valid format.
     * Returns the error code:
     *  0 if email is Correct
     *  1 if email is empty
     *  2 if email contains extra spaces
     *  3 if email format is wrong
     * @param email to verify
     * @return error code
     */
    public int isEmailFormatWrong(String email){
        // Length:
        if( email.length() == 0 ){
            return 1;
        }
        // Extra space
        if( ! email.trim().equals(email) ){
            return 2;
        }
        // Content
        Pattern email_address_pattern = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+"
        );
        if( ! email_address_pattern.matcher(email).matches() ){
            return 3;
        }
        return 0;
    }

    /**
     * Check the email if is valid format.
     * Returns the error code:
     *  0 if password his Correct
     *  1 if password is empty
     *  2 if password is shorter then 6
     *  3 if password contains space
     *  4 if password format is wrong
     * @param password to verify
     * @return error code
     */
    public int isPasswordFormatWrong(String password){
        // Empty
        if( password.length() == 0 ){
            return 1;
        }
        // Length
        if( password.length() < 6 ){
            return 2;
        }
        // Extra space
        if( ! password.trim().equals(password) ){
            return 3;
        }
        // Content
        Pattern password_address_pattern = Pattern.compile(
                "[a-zA-Z0-9_]+"
        );
        if( ! password_address_pattern.matcher(password).matches() ){
            return 4;
        }
        return 0;
    }

    public void resetErrors(){
        mEmailEditText.setError(null);
        mPasswordEditText.setError(null);
    }


    public void addFragmentToStack(Fragment fragment, String fragment_tag){
        /*
        Fragment fragmentTwo = FragmentUtil.getFragmentByTagName(fragmentManager, "Fragment Two");

        // Because fragment two has been popup from the back stack, so it must be null.
        if(fragmentTwo==null)
        {
            fragmentTwo = new FragmentTwo();
        }
        */
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer, fragment, fragment_tag);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        //MainActivity.printActivityFragmentList(fragmentManager);
    }

}
