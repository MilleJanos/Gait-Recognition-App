package ms.sapientia.ro.gaitrecognitionapp.presenter.menu;

public class HomeFragmentPresenter {

//    private User user;
    private View view;

    public HomeFragmentPresenter(View view){
        this.view = view;
    }

    // Methods:
//    public void updateEmail(String email){
//        user.setEmail(email);
//    }
//
//    public void updateFirstName(String first_name){
//        user.setFirst_name(first_name);
//    }
//
//    public void updateLastName(String last_name){
//        user.setLast_name(last_name);
//    }
//
//    public void updatePassword(String password){
//        user.setPassword(password);
//    }


    // Interface:
    public interface View{
        void showProgressBar();
        void hideProgressBar();
    }

}
