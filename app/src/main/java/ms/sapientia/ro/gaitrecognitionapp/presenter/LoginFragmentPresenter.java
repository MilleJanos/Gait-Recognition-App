package ms.sapientia.ro.gaitrecognitionapp.presenter;

import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.regex.Pattern;

import ms.sapientia.ro.gaitrecognitionapp.MainActivity;
import ms.sapientia.ro.gaitrecognitionapp.common.Util;
import ms.sapientia.ro.gaitrecognitionapp.view.LoginFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.MainFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.RegisterFragment;

public class LoginFragmentPresenter {

    // Members
    private static final String TAG = "LoginFragmentPresenter";
    private View view;

    // Interface:
    public interface View{
        void showProgressBar();
        void hideProgressBar();
    }

    // Constructor
    public LoginFragmentPresenter(View view) {
        this.view = view;
    }

    // Methods:

    /**
     * Verifies email and password format and length.
     * @param email email to verify
     * @param password password to verify
     * @param email_button only to set error on the EditText
     * @param password_button only to set error on the EditText
     * @return true if inputs are valid, and false if not
     */
    public boolean verifyInputs(String email, String password, EditText email_button, EditText password_button){
        int errorCode;

        /// Check email:
        errorCode = isEmailFormatWrong(email);

        switch (errorCode){
            case 1:{
                email_button.setError("Please fill the email field.");
                return false;
            }
            case 2:{
                email_button.setError("Email can't contain extra spaces.");
                return false;
            }
            case 3:{
                email_button.setError("Wrong email format.");
                return false;
            }
        }

        /// Check password:
        errorCode = isPasswordFormatWrong(password);

        switch (errorCode){
            case 1:{
                password_button.setError("Please fill the password field");
                return false;
            }
            case 2:{
                password_button.setError("Password has to be at least 6 characters.");
                return false;
            }
            case 3:{
                password_button.setError("Password can't contain extra spaces.");
                return false;
            }
            case 4:{
                password_button.setError("Password can contain only characters, numbers and underscore.");
                return false;
            }
        }

        return true;
    }

    /**
     * This method resets the error on the input EditText-s.
     * @param email_edit_text
     * @param password_edit_text
     */
    public void resetErrors(EditText email_edit_text, EditText password_edit_text){
        email_edit_text.setError(null);
        password_edit_text.setError(null);
    }

    /**
     * This method adds the RegisterFragment to fragment stack.
     */
    public void goToRegisterPage() {
        MainActivity.sInstance.addFragmentToStack(new RegisterFragment(), "register_fragment"); // Without animation
    }

    /**
     * Login using email and password
     *
     * Steps:
     *  1. Checking inputs
     *  2. Checking email existance
     *  3. Login
     *
     *  Methods called:
     *   CheckEmail_then_Login()
     *      |
     *      | LoginWithEmailAndPassword()
     *
     * @param email to login with
     * @param password to login with
     */
    public void Login(String email, String password){

        // Show progress bar:
        view.showProgressBar();

        // Check is email is correct or not:
        if( Util.requireInternetConnection() ){

            CheckEmail_then_Login(email,password);

        }else{

            Log.w(TAG, "No internet connection!");
            Toast.makeText(MainActivity.sContext, "No internet connection!", Toast.LENGTH_SHORT).show();

            // Hide progress bar
            view.hideProgressBar();

        }

    }

    /**
     * Checks email if is registrated.
     * If email is registrated then calls the LoginWithEmailAndPassword() method
     * with the input parameter.
     *
     * Steps:
     * 1. Checking email existance
     * 2. Login
     *
     * Methods called:
     *  LoginWithEmailAndPassword()
     *
     * @param email to login with
     * @param password to login with
     */
    private void CheckEmail_then_Login(String email, String password){
        Util.sAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "Checking to see if email is registrated or not:");

                //region OLD - depricated
                //ProviderQueryResult result = task.getResult();    // deprecated
                //if (result != null && result.getProviders() != null && result.getProviders().size() > 0) {    // deprecated
                //endregion

                SignInMethodQueryResult result = task.getResult();

                if (result != null && result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {

                    Log.i(TAG, "Email exists!");

                    // Log in user using
                    // email and password:
                    LoginWithEmailAndPassword(email,password);

                }else{

                    Log.i(TAG, "Email not exists!");
                    Toast.makeText(MainActivity.sContext, "This email is not registrated!", Toast.LENGTH_SHORT).show();
                    LoginFragment.sInstance.mEmailEditText.setError("Not registred email");
                    LoginFragment.sInstance.mPasswordEditText.setError(null);

                    // Hide progress bar
                    view.hideProgressBar();

                }
            }else{
                Log.e(TAG, "Username check failed", task.getException());
                Toast.makeText(MainActivity.sContext, "Email check failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Login user using email and password
     * @param email to login with
     * @param password to login with
     */
    private void LoginWithEmailAndPassword(String email, String password){

        Util.sAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
           if( task.isSuccessful() ) {

               // User now is logged in.

               // Open MainFragment
               MainActivity.sInstance.addFragmentToStack(new MainFragment(), "main_fragment");

               // Hide progress bar
               view.hideProgressBar();

           }else{

               // Login attempt failed.
               LoginFragment.sInstance.mEmailEditText.setError(null);
               LoginFragment.sInstance.mPasswordEditText.setError("Wrong password");

               // Hide progress bar
               view.hideProgressBar();
           }
        });

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
    private int isEmailFormatWrong(String email){
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
    private int isPasswordFormatWrong(String password){
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

}
