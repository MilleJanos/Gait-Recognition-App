package ms.sapientia.ro.gaitrecognitionapp.model;

import android.text.TextUtils;
import android.util.Patterns;

public class User implements IUser{

    private String email;
    private String firstName;
    private String lastName;
    private String password;

    public User(String email, String password){
        this.email = email;
        this.firstName = "";
        this.lastName = "";
        this.password = password;
    }

    public User(String email, String firstName, String lastName, String password){
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public String getLastName() {
        return this.lastName;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isValidData() {
        return !TextUtils.isEmpty(getEmail()) &&
                Patterns.EMAIL_ADDRESS.matcher(getEmail()).matches() &&
               !TextUtils.isEmpty(getFirstName()) &&
               !TextUtils.isEmpty(getLastName());
    }
}
