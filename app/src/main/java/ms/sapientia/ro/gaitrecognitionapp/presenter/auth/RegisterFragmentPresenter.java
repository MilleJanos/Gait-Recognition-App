package ms.sapientia.ro.gaitrecognitionapp.presenter.auth;

import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.SignInMethodQueryResult;

import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.logic.FirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.model.IAfter;
import ms.sapientia.ro.gaitrecognitionapp.model.MyFirebaseUser;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;
import ms.sapientia.ro.gaitrecognitionapp.view.auth.LoginFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.auth.RegisterFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.HomeFragment;

/**
 * this class is the presenter of the RegisterFragment class.
 */
public class RegisterFragmentPresenter {

    // Constants
    private static final String TAG = "RegisterFragmentPresent";
    // Interface:
    private View view;
    public interface View{
        void showProgressBar(IAfter after);
        void showProgressBar();
        void hideProgressBar();
    }
    // Constructor:
    public RegisterFragmentPresenter(View view){
        this.view = view;
    }
    // Members:
    private static boolean mRegisterIsDismissed;

    /**
     * Register using email and password
     *
     * Steps:
     *  1. Checking inputs
     *  2. Checking email existance
     *  3. Register
     *
     * Methods called:
     *  CheckEmail_then_Register()
     *     |
     *     | RegisterWithEmailAndPassword()
     *
     * @param email to register it
     * @param password to register it
     */
    public void Register(String email, String password) {

        // Show progress bar:
        view.showProgressBar(new IAfter() {
            @Override
            public void Do() {
                mRegisterIsDismissed = true;
                Toast.makeText(MainActivity.sContext,"Registration cancelled.", Toast.LENGTH_SHORT).show();
            }
        });
        mRegisterIsDismissed = false;

        // Check is email is correct or not:
        if( AppUtil.requireInternetConnection() ){

            CheckEmail_then_Register(email,password);

        }else{

            Log.w(TAG, "No internet connection!");
            Toast.makeText(MainActivity.sContext, "No internet connection!", Toast.LENGTH_SHORT).show();

            // Hide progress bar
            view.hideProgressBar();

        }

    }

    /**
     * Checks email if is registrated.
     * If email is not registrated then calls the RegisterWithEmailAndPassword() method
     * with the input parameter.
     *
     * Steps:
     *  1. Checking email existance
     *  2. Register
     *
     * Methods called:
     *  CheckEmail_then_Register()
     *
     * @param email to register it
     * @param password to register it
     */
    private void CheckEmail_then_Register(String email, String password){
        AppUtil.sAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "Checking to see if email is registrated or not:");

                //region OLD - depricated
                //ProviderQueryResult result = task.getResult();    // deprecated
                //if (result != null && result.getProviders() != null && result.getProviders().size() > 0) {    // deprecated
                //endregion

                SignInMethodQueryResult result = task.getResult();

                if (result != null && result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {

                    Log.i(TAG, "This email is already registred!");
                    Toast.makeText(MainActivity.sContext, "This email is already registred!", Toast.LENGTH_SHORT).show();

                    setErrors("Already used",null,null);

                    // Hide progress bar
                    view.hideProgressBar();



                }else{

                    // Email not found in registrated
                    // users, so continure:
                    RegisterWithEmailAndPassword(email,password);

                }
            }else{
                Log.i(TAG, "Username check failed", task.getException());
                Toast.makeText(MainActivity.sContext, "Email check failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Registering new user using email and password
     * @param email to register it
     * @param password to register it
     */
    private void RegisterWithEmailAndPassword(String email, String password){
        AppUtil.sAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){

                        // Registration succeed:

                        if( mRegisterIsDismissed == true){
                            return;
                        }

                        String id = AppUtil.sAuth.getUid();
                        MyFirebaseUser user = new MyFirebaseUser( id );

                        Log.i(TAG, "Registration succeed", task.getException());
                        Toast.makeText(MainActivity.sContext, "Registration succeed", Toast.LENGTH_LONG).show();

                        // Remove Register fragment:
                        //goBackToLoginPage(); // Remove Register fragment before going to Main fragment

                        // Set user object as app member:
                        AppUtil.sUser = user;

                        // Save into app member:
                        FirebaseController.setUserObject( user );

                        // Refresh navigation menu drawer userinfo:
                        MainActivity.sInstance.refreshNavigationMenuDraverNameAndEmail();

                        // Hide progress bar:
                        view.hideProgressBar();

                        // Open HomeFragment
                        MainActivity.sInstance.replaceFragment(new HomeFragment(), "main_fragment");


                    }else{

                        // Registraiton failed:

                        if( mRegisterIsDismissed = true){
                            return;
                        }

                        Log.i(TAG, "Registration failed", task.getException());
                        Toast.makeText(MainActivity.sContext, "Registration failed", Toast.LENGTH_LONG).show();

                        // Hide progress bar
                        view.hideProgressBar();

                    }
                });
    }

    /**
     * Verifies email and password format and length.
     * @param email email to verify
     * @param password1 password 1 to verify
     * @param password2 password 2 to verify
     * @param email_button only to set error on the EditText
     * @param password_button_1 only to set error on the EditText
     * @param password_button_2 only to set error on the EditText
     * @return true if inputs are valid, and false if not
     */
    public boolean verifyInputs(String email, String password1, String password2, EditText email_button, EditText password_button_1, EditText password_button_2) {
        int errorCode;

        /// Check email:
        errorCode = AppUtil.isEmailFormatWrong(email);

        switch (errorCode){
            case 1:{
                setErrors("Please fill the email field.",null,null);
                return false;
            }
            case 2:{
                setErrors("Email can't contain extra spaces.",null,null);
                return false;
            }
            case 3:{
                setErrors("Wrong email format.",null,null);
                return false;
            }
        }

        /// Check password 1:
        errorCode = AppUtil.isPasswordFormatWrong(password1);

        switch (errorCode){
            case 1:{
                setErrors(null,"Please fill the password field",null);
                return false;
            }
            case 2:{
                setErrors(null,"Password has to be at least 6 characters.",null);
                return false;
            }
            case 3:{
                setErrors(null,"Password can't contain extra spaces.",null);
                return false;
            }
            case 4:{
                setErrors(null,"Password needs to contain lower and uppercase character and number.",null);
                return false;
            }
        }

        /// Check password 1:
        if( ! password1.equals(password2) ){
            setErrors(null,null,"Passwords don't match!");
            return false;
        }

        return true;
    }

    /**
     * This method removes the RegisterFragment from top of the LoginFragment on fragment stack.
     */
    public void goBackToLoginPage() {
        MainActivity.sInstance.replaceFragment(new LoginFragment(), "login_fragment"); // Without animation
    }

    /**
     * This method sets error on input fielsd
     * @param email_msg email input field error message
     * @param password_msg_1 first password input field error message
     * @param password_msg_2 second password input field error message
     */
    public void setErrors(String email_msg, String password_msg_1, String password_msg_2){
        // Set errors:
        RegisterFragment.sInstance.mEmailEditText.setError(email_msg);
        RegisterFragment.sInstance.mPasswordEditText1.setError(password_msg_1);
        RegisterFragment.sInstance.mPasswordEditText2.setError(password_msg_2);
        // Set focus:
        if( email_msg != null && ( ! email_msg.isEmpty()) ){

            RegisterFragment.sInstance.mEmailEditText.requestFocus();

        }else{

            if( password_msg_1 != null && ( ! password_msg_1.isEmpty()) ){

                RegisterFragment.sInstance.mPasswordEditText1.requestFocus();

            }else{

                if( password_msg_2 != null && ( ! password_msg_2.isEmpty()) ) {

                    RegisterFragment.sInstance.mPasswordEditText2.requestFocus();

                }
            }
        }
    }

    /**
     * This method resets the errors on EditText view elements.
     * @param email_edit_text
     * @param password_edit_text_1
     * @param password_edit_text_2
     */
    public void resetErrors(EditText email_edit_text, EditText password_edit_text_1, EditText password_edit_text_2){
        setErrors(null,null,null);
    }

}
