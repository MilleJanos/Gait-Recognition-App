package ms.sapientia.ro.gaitrecognitionapp.presenter.menu;

public class HelpFragmentPresenter {

    // Members
    private View view;

    // Interface:
    public interface View{
        void showProgressBar();
        void hideProgressBar();
    }

    // Constructor
    public HelpFragmentPresenter(View view){
        this.view = view;
    }



}
