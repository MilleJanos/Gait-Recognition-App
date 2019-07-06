package ms.sapientia.ro.gaitrecognitionapp.presenter.menu;

/**
 * This class is the presenter of the HomeFragment class.
 *
 * @author MilleJanos
 */
public class HomeFragmentPresenter {

    // private User user;
    private View view;

    public HomeFragmentPresenter(View view){
        this.view = view;
    }

    // Interface:
    public interface View{
        void showProgressBar();
        void hideProgressBar();
    }

}
