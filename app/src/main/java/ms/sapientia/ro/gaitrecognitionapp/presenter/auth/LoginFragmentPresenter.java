package ms.sapientia.ro.gaitrecognitionapp.presenter.auth;

import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.SignInMethodQueryResult;

import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.logic.FirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.model.IAfter;
import ms.sapientia.ro.gaitrecognitionapp.model.ICallback;
import ms.sapientia.ro.gaitrecognitionapp.model.MyFirebaseUser;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;
import ms.sapientia.ro.gaitrecognitionapp.view.auth.LoginFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.auth.RegisterFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.HomeFragment;

/**
 * This class is the presenter of the LoginFragment class.
 *
 * @author MilleJános
 */
public class LoginFragmentPresenter {

    // Members
    private static final String TAG = "LoginFragmentPresenter";
    private View view;
    // Interface:
    public interface View{
        void showProgressBar(IAfter after);
        void showProgressBar();
        void hideProgressBar();
    }
    // Constructor
    public LoginFragmentPresenter(View view) {
        this.view = view;
    }
    // Methods:
    private boolean mLoginIsDismissed;

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
        errorCode = AppUtil.isEmailFormatWrong(email);

        switch (errorCode){
            case 1:{
                setErrors("Please fill the email field.",null);
                return false;
            }
            case 2:{
                setErrors("Email can't contain extra spaces.",null);
                return false;
            }
            case 3:{
                setErrors("Wrong email format.",null);
                return false;
            }
        }

        /// Check password:
        errorCode = AppUtil.isPasswordFormatWrong(password);

        switch (errorCode){
            case 1:{
                setErrors(null,"Please fill the password field");
                return false;
            }
            case 2:{
                setErrors(null,"Password has to be at least 6 characters.");
                return false;
            }
            case 3:{
                setErrors(null,"Password can't contain extra spaces.");
                return false;
            }
            case 4:{
                setErrors(null,"Password can contain only characters, numbers and underscore.");
                return false;
            }
        }

        return true;
    }

    /**
     * This method sets error on input fielsd
     * @param email_msg email input field error message
     * @param password_msg password input field error message
     */
    public void setErrors(String email_msg, String password_msg){
        // Set errors:
        LoginFragment.sInstance.mEmailEditText.setError(email_msg);
        LoginFragment.sInstance.mPasswordEditText.setError(password_msg);

        // Set focus:
        if( email_msg != null && (! email_msg.isEmpty()) ){

            LoginFragment.sInstance.mEmailEditText.requestFocus();

        }else{

            if( password_msg != null && (! password_msg.isEmpty() )) {
                LoginFragment.sInstance.mPasswordEditText.requestFocus();
            }
        }
    }

    /**
     * This method resets the error on the input EditText-s.
     * @param email_edit_text
     * @param password_edit_text
     */
    public void resetErrors(EditText email_edit_text, EditText password_edit_text){
        setErrors(null, null);
    }

    /**
     * This method adds the RegisterFragment to fragment stack.
     */
    public void goToRegisterPage() {
        MainActivity.sInstance.replaceFragment(new RegisterFragment(), "register_fragment"); // Without animation
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
        view.showProgressBar(new IAfter(){
            @Override
            public void Do() {
                mLoginIsDismissed = true;
                Toast.makeText(MainActivity.sContext,"Login cancelled.", Toast.LENGTH_SHORT).show();
            }
        });
        mLoginIsDismissed = false;

        // Check is email is correct or not:
        if( AppUtil.requireInternetConnection() ){

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
        AppUtil.sAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
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

        AppUtil.sAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
           if( task.isSuccessful() ) {

               // User now is logged in.

               if( mLoginIsDismissed ){
                   return;
               }

               // GerCreate user object in database if the user does not have:
               new FirebaseController().getUserObjectById( AppUtil.sAuth.getUid(), new ICallback<MyFirebaseUser>(){
                   @Override
                   public void Success(MyFirebaseUser user) {
                       // If user already has object in firebase:
                       AppUtil.sUser = user;
                       // Refresh navigation menu drawer userinfo:
                       MainActivity.sInstance.refreshNavigationMenuDraverNameAndEmail();
                       // Hide progress bar:
                       view.hideProgressBar();
                       // Open HomeFragment
                       MainActivity.sInstance.replaceFragment(new HomeFragment(), "main_fragment");
                   }
                   @Override
                   public void Failure() {
                       // If hasn't --> Create one !
                       String id = AppUtil.sAuth.getUid();
                       MyFirebaseUser user = new MyFirebaseUser(id);
                       // Update to firebase
                       FirebaseController.setUserObject( user );
                       // Set into member
                       AppUtil.sUser = user;
                       // Hide progress bar
                       view.hideProgressBar();
                       // Open HomeFragment
                       MainActivity.sInstance.replaceFragment(new HomeFragment(), "main_fragment");
                   }
                   @Override
                   public void Error(int error_code) {
                       Toast.makeText(MainActivity.sContext,"Error: Login",Toast.LENGTH_LONG).show();
                       // Hide progress bar
                       view.hideProgressBar();
                   }
               });

           }else{

               // Login attempt failed.

               if( mLoginIsDismissed ){
                   return;
               }

               setErrors(null,"Wrong password");

               // Hide progress bar
               view.hideProgressBar();
           }
        });

    }

    // public void animateViewItemsIn(ImageView logo, TextView title, EditText email, EditText password, TextView forgetPassword, Button login, TextView goToRegister){
    //
    //     Animator.Slide(logo, -300, 0, 0, 0, 500);
    //     Animator.Slide(title, -300, 0, 0, 0, 500);
    //     Animator.Slide(email, -300, 0, 0, 0, 500);
    //     Animator.Slide(password, -300, 0, 0, 0, 500);
    //     Animator.Slide(forgetPassword, -300, 0, 0, 0, 500);
    //     Animator.Slide(login, -300, 0, 0, 0, 500);
    //     Animator.Slide(goToRegister, -300, 0, 0, 0, 500);
    //     Animator.Slide(logo, -300, 0, 0, 0, 500);
    // }
    //
    // public void animateViewItemsOut(ImageView logo, TextView title, EditText email, EditText password, TextView forgetPassword, Button login, TextView goToRegister){
    //     Animator.Slide(logo, 0, -300, 0, 0, 500);
    //     Animator.Slide(title, 0, -300, 0, 0, 500);
    //     Animator.Slide(email, 0, -300, 0, 0, 500);
    //     Animator.Slide(password, 0, -300, 0, 0, 500);
    //     Animator.Slide(forgetPassword, 0, -300, 0, 0, 500);
    //     Animator.Slide(login, 0, -300, 0, 0, 500);
    //     Animator.Slide(goToRegister, 0, -300, 0, 0, 500);
    //     Animator.Slide(logo, 0, -300, 0, 0, 500);
    // }

}
