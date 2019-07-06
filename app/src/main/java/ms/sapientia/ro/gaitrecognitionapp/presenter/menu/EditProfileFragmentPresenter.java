package ms.sapientia.ro.gaitrecognitionapp.presenter.menu;

/**
 * This class is the presenter of the EditProfile class.
 *
 * @author MilleJanos
 */
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
