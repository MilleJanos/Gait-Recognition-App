package ms.sapientia.ro.gaitrecognitionapp.Model;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;

import org.w3c.dom.Text;

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

    @Override
    public boolean isValidData() {
        return !TextUtils.isEmpty(getEmail()) &&
                Patterns.EMAIL_ADDRESS.matcher(getEmail()).matches() &&
               !TextUtils.isEmpty(getFirstName()) &&
               !TextUtils.isEmpty(getLastName());
    }
}
