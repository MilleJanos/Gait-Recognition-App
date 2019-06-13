package ms.sapientia.ro.gaitrecognitionapp.model;

import android.text.TextUtils;
import android.util.Patterns;

public class User implements IUser{

    private String email;
    private String first_name;
    private String last_name;
    private String password;


    public User(String email, String password){
        this.email = email;
        this.first_name = "";
        this.last_name = "";
        this.password = password;
    }

    public User(String email, String firstName, String lastName, String password){
        this.email = email;
        this.first_name = firstName;
        this.last_name = lastName;
        this.password = password;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    @Override
    public String getFirst_name() {
        return this.first_name;
    }

    @Override
    public String getLast_name() {
        return this.last_name;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isValidData() {
        return !TextUtils.isEmpty(getEmail()) &&
                Patterns.EMAIL_ADDRESS.matcher(getEmail()).matches() &&
               !TextUtils.isEmpty(getFirst_name()) &&
               !TextUtils.isEmpty(getLast_name());
    }
}
