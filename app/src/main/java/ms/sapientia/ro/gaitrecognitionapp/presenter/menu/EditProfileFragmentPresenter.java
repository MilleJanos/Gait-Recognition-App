package ms.sapientia.ro.gaitrecognitionapp.presenter.menu;

public class EditProfileFragmentPresenter {

    // Members
    private View view;

    public EditProfileFragmentPresenter(View view){
        this.view = view;
    }

    // Interface:
    public interface View{
        void showProgressBar();
        void hideProgressBar();
    }
}
