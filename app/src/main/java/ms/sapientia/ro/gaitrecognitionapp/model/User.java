package ms.sapientia.ro.gaitrecognitionapp.model;

import android.text.TextUtils;

public class User implements IUser{

    public String id = "";
    public String first_name = "";
    public String last_name = "";
    public String email = "";
    public long birth_date = 0;
    public String phone_number = "";

    public User(){
    }

    public User(String user_id){
        this.id = user_id;
    }


    public User(String first_name, String last_name){
        this.first_name = first_name;
        this.last_name = last_name;
    }

    @Override
    public boolean isValidData() {
        return !TextUtils.isEmpty(this.first_name) &&
               !TextUtils.isEmpty(this.last_name);
    }
}
