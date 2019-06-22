package ms.sapientia.ro.gaitrecognitionapp.presenter.menu;

public class ProfileFragmentPresenter {

    // Members
    private View view;

    public ProfileFragmentPresenter(View view){
        this.view = view;
    }

    // Interface:
    public interface View{
        void showProgressBar();
        void hideProgressBar();
    }
}
