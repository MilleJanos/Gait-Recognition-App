package ms.sapientia.ro.gaitrecognitionapp.presenter;

import ms.sapientia.ro.gaitrecognitionapp.model.User;

public class MainFragmentPresenter {

    private User user;
    private View view;

    public MainFragmentPresenter(View view){
        this.view = view;
    }

    // Methods:
    public void updateEmail(String email){
        user.setEmail(email);
    }

    public void updateFirstName(String first_name){
        user.setFirstName(first_name);
    }

    public void updateLastName(String last_name){
        user.setLastName(last_name);
    }

    public void updatePassword(String password){
        user.setPassword(password);
    }


    // Interface:
    public interface View{
        void initProgressBar();
        void showProgressBar();
        void hideProgressBar();
    }

}
