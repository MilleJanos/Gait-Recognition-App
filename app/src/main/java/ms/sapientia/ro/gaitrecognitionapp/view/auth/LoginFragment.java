package ms.sapientia.ro.gaitrecognitionapp.view.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.Animator;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.model.IAfter;
import ms.sapientia.ro.gaitrecognitionapp.presenter.auth.LoginFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

/**
 * This class is responsible for user login.
 *
 * @author MilleJanos
 */
public class LoginFragment extends Fragment implements LoginFragmentPresenter.View {

    // Static members:
    public static LoginFragment sInstance;
    // View members:
    private ImageView mIconImageView;
    private TextView mTitleTextView;
    public EditText mEmailEditText;
    public EditText mPasswordEditText;
    private Button mLoginButton;
    private TextView mRegisterTextViewButton;
    private TextView mForgottPasswordTextViewButton;
    private ConstraintLayout mStopServiceConstLayoutButton;
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

        if( ! MainActivity.sFirstRun ) {
            MainActivity.sFirstRun = true;
            Animator.LogoIntro(view.findViewById(R.id.ic_imageview));
            Animator.Slide(view.findViewById(R.id.login_title_textview), -1000, 0, 0, 0);
        }
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

    /**
     * This method initiates the view elements.
     * @param view fragment view;
     */
    private void initView(View view){
        mIconImageView = getView().findViewById(R.id.ic_imageview);
        mTitleTextView = getView().findViewById(R.id.login_title_textview);
        mEmailEditText = getView().findViewById(R.id.email_editText);
        mPasswordEditText = getView().findViewById(R.id.password_editText);
        mLoginButton = getView().findViewById(R.id.login_button);
        mRegisterTextViewButton = getView().findViewById(R.id.sign_up_textviewbutton);
        mForgottPasswordTextViewButton = getView().findViewById(R.id.forgot_password_textviewbutton);
        mStopServiceConstLayoutButton = getView().findViewById(R.id.on_login_stop_service_button);
    }

    /**
     * This method binds the view elements.
     */
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
        mStopServiceConstLayoutButton.setOnClickListener( v -> {
            BackgroundService.sInstance.StopService();
            mStopServiceConstLayoutButton.setVisibility( View.INVISIBLE );
        });
    }

    /**
     * This method verifies the inputs and tries to log in the user.
     * @param view fragment view;
     */
    public void loginButtonClick(View view){

        if( MainActivity.sIsProgressBarShown ){
            return;
        }

        // Reset errors:
        mPresenter.resetErrors(mEmailEditText, mPasswordEditText);

        // Get input values:
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        // Try to login:
        if( mPresenter.verifyInputs(email,password,mEmailEditText,mPasswordEditText) ) {

            mPresenter.Login(email,password);

        }
    }

    /**
     * This method verifies the inputs and tries to register the user.
     * @param view fragment view;
     */
    public void registerButtonClick(View view){

        if( MainActivity.sIsProgressBarShown ){
            return;
        }

        // mPresenter.animateViewItemsOut(mIconImageView, mTitleTextView, mEmailEditText, mPasswordEditText, mForgottPasswordTextViewButton, mLoginButton, mRegisterTextViewButton);

        mPresenter.goToRegisterPage();
    }

    /**
     * This method sends reset password request to user's email.
     * @param view fragment view;
     */
    public void forgottPasswordClick(View view){

        if( MainActivity.sIsProgressBarShown ){
            return;
        }

        String email = mEmailEditText.getText().toString();

        if( email.isEmpty() ){
            mPresenter.setErrors("Fill email first!",null);
            return;
        }

        AppUtil.requestPasswordReset( email );

    }

    /**
     * This method shows the progress bar.
     * @param after method which will be run if the progress bar is dismissed.
     */
    @Override
    public void showProgressBar(IAfter after) {
        MainActivity.sInstance.showProgressBar( after );
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

    /**
     * This method shows the stop service button, and hides if the service is not running.
     */
    @Override
    public void onResume() {
        super.onResume();

        if( BackgroundService.sIsRunning ){
            mStopServiceConstLayoutButton.setVisibility( View.VISIBLE );
        }else{
            mStopServiceConstLayoutButton.setVisibility( View.INVISIBLE );
        }

    }
}
