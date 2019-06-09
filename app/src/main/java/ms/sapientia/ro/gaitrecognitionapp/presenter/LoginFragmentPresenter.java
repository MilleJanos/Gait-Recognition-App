package ms.sapientia.ro.gaitrecognitionapp.presenter;

import android.support.v4.app.FragmentManager;
import android.widget.EditText;

import java.util.regex.Pattern;

import ms.sapientia.ro.gaitrecognitionapp.MainActivity;
import ms.sapientia.ro.gaitrecognitionapp.view.MainFragment;

public class LoginFragmentPresenter {

    private View view;

    public LoginFragmentPresenter(View view) {
        this.view = view;
    }

    // Methods:
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


    public void Login(String email, String password) {

        //TODO

        // IF EMAIL AND PASSWORD IS OK IN FIREBASE>AUTH == >
        // THEN

        //MainActivity.Instance.showProgressBar();
        FragmentManager fragmentManager = MainActivity.Instance.getSupportFragmentManager();
        MainActivity.Instance.addFragmentToStack(new MainFragment(), fragmentManager,"main_fragment");

    }


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
                password_button.setError("Email can't contain extra spaces.");
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




    // Interface:
    public interface View{
        void initProgressBar();
        void showProgressBar();
        void hideProgressBar();
    }

}
